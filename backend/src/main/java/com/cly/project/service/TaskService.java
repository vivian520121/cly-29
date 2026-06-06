package com.cly.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cly.project.dto.TaskQueryDTO;
import com.cly.project.dto.TaskSaveDTO;
import com.cly.project.dto.TaskStatusUpdateDTO;
import com.cly.project.entity.Project;
import com.cly.project.entity.Task;
import com.cly.project.entity.TaskLog;
import com.cly.project.entity.TaskTag;
import com.cly.project.entity.TaskTagRel;
import com.cly.project.entity.TaskWorklog;
import com.cly.project.entity.User;
import com.cly.project.enums.TaskActionTypeEnum;
import com.cly.project.enums.TaskStatusEnum;
import com.cly.project.exception.BusinessException;
import com.cly.project.mapper.ProjectMapper;
import com.cly.project.mapper.TaskLogMapper;
import com.cly.project.mapper.TaskMapper;
import com.cly.project.mapper.TaskTagMapper;
import com.cly.project.mapper.TaskTagRelMapper;
import com.cly.project.mapper.TaskWorklogMapper;
import com.cly.project.mapper.UserMapper;
import com.cly.project.util.TreeUtil;
import com.cly.project.util.UserContext;
import com.cly.project.vo.KanbanColumnVO;
import com.cly.project.vo.TaskDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService extends ServiceImpl<TaskMapper, Task> {

    private final UserMapper userMapper;
    private final ProjectMapper projectMapper;
    private final TaskTagMapper taskTagMapper;
    private final TaskTagRelMapper taskTagRelMapper;
    private final TaskWorklogMapper taskWorklogMapper;
    private final TaskLogService taskLogService;

    public IPage<Task> page(TaskQueryDTO query) {
        Page<Task> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Task> wrapper = buildQueryWrapper(query);
        wrapper.orderByAsc(Task::getSortOrder).orderByDesc(Task::getCreateTime);

        IPage<Task> taskPage = baseMapper.selectPage(page, wrapper);
        fillTaskExtraInfo(taskPage.getRecords());

        return taskPage;
    }

    public List<KanbanColumnVO> getKanbanData(TaskQueryDTO query) {
        List<KanbanColumnVO> columns = new ArrayList<>();

        for (TaskStatusEnum statusEnum : TaskStatusEnum.values()) {
            KanbanColumnVO column = new KanbanColumnVO();
            column.setStatus(statusEnum.getCode());
            column.setStatusName(statusEnum.getDesc());
            column.setStatusColor(statusEnum.getColor());

            LambdaQueryWrapper<Task> wrapper = buildQueryWrapper(query);
            wrapper.eq(Task::getStatus, statusEnum.getCode());
            wrapper.orderByAsc(Task::getSortOrder).orderByDesc(Task::getCreateTime);

            List<Task> tasks = baseMapper.selectList(wrapper);
            fillTaskExtraInfo(tasks);

            column.setTasks(tasks);
            column.setTotal((long) tasks.size());
            columns.add(column);
        }

        return columns;
    }

    public TaskDetailVO getDetailById(Long id) {
        Task task = baseMapper.selectById(id);
        if (task == null) {
            return null;
        }

        TaskDetailVO detailVO = new TaskDetailVO();
        BeanUtils.copyProperties(task, detailVO);

        fillTaskExtraInfo(List.of(task));

        LambdaQueryWrapper<Task> subtaskWrapper = new LambdaQueryWrapper<>();
        subtaskWrapper.eq(Task::getParentId, id);
        subtaskWrapper.orderByAsc(Task::getSortOrder);
        List<Task> subtasks = baseMapper.selectList(subtaskWrapper);
        fillTaskExtraInfo(subtasks);
        detailVO.setSubtasks(subtasks);

        List<TaskTag> tags = getTaskTags(id);
        detailVO.setTags(tags);

        LambdaQueryWrapper<TaskWorklog> worklogWrapper = new LambdaQueryWrapper<>();
        worklogWrapper.eq(TaskWorklog::getTaskId, id);
        worklogWrapper.orderByDesc(TaskWorklog::getWorkDate);
        List<TaskWorklog> worklogs = taskWorklogMapper.selectList(worklogWrapper);
        if (!worklogs.isEmpty()) {
            List<Long> userIds = worklogs.stream().map(TaskWorklog::getUserId).distinct().collect(Collectors.toList());
            List<User> users = userMapper.selectBatchIds(userIds);
            Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
            for (TaskWorklog worklog : worklogs) {
                User user = userMap.get(worklog.getUserId());
                if (user != null) {
                    worklog.setUsername(user.getUsername());
                    worklog.setRealName(user.getRealName());
                    worklog.setAvatar(user.getAvatar());
                }
            }
        }
        detailVO.setWorklogs(worklogs);

        List<TaskLog> logs = taskLogService.listByTaskId(id);
        detailVO.setLogs(logs);

        return detailVO;
    }

    @Transactional
    public void saveTask(TaskSaveDTO dto) {
        Task task = new Task();
        BeanUtils.copyProperties(dto, task);

        task.setTaskNo(generateTaskNo());
        task.setCreatorId(UserContext.getUserId());

        if (task.getStatus() == null) {
            task.setStatus(TaskStatusEnum.TODO.getCode());
        }

        if (task.getPriority() == null) {
            task.setPriority(2);
        }

        if (task.getSortOrder() == null) {
            task.setSortOrder(0);
        }

        if (task.getProgress() == null) {
            task.setProgress(0);
        }

        if (task.getActualHours() == null) {
            task.setActualHours(BigDecimal.ZERO);
        }

        baseMapper.insert(task);

        saveTaskTags(task.getId(), dto.getTagIds());

        taskLogService.logTaskChange(
                task.getId(),
                UserContext.getUserId(),
                TaskActionTypeEnum.CREATE.getCode(),
                null,
                null,
                null,
                "创建任务"
        );
    }

    @Transactional
    public void updateTask(TaskSaveDTO dto) {
        Task existingTask = baseMapper.selectById(dto.getId());
        if (existingTask == null) {
            throw new BusinessException("任务不存在");
        }

        Long currentUserId = UserContext.getUserId();

        if (!Objects.equals(existingTask.getTaskName(), dto.getTaskName())) {
            taskLogService.logTaskChange(
                    dto.getId(),
                    currentUserId,
                    TaskActionTypeEnum.UPDATE.getCode(),
                    "taskName",
                    existingTask.getTaskName(),
                    dto.getTaskName(),
                    "修改任务名称"
            );
        }

        if (!Objects.equals(existingTask.getDescription(), dto.getDescription())) {
            taskLogService.logTaskChange(
                    dto.getId(),
                    currentUserId,
                    TaskActionTypeEnum.UPDATE.getCode(),
                    "description",
                    existingTask.getDescription(),
                    dto.getDescription(),
                    "修改任务描述"
            );
        }

        if (!Objects.equals(existingTask.getTaskType(), dto.getTaskType())) {
            taskLogService.logTaskChange(
                    dto.getId(),
                    currentUserId,
                    TaskActionTypeEnum.UPDATE.getCode(),
                    "taskType",
                    existingTask.getTaskType() != null ? existingTask.getTaskType().toString() : null,
                    dto.getTaskType() != null ? dto.getTaskType().toString() : null,
                    "修改任务类型"
            );
        }

        if (!Objects.equals(existingTask.getPriority(), dto.getPriority())) {
            taskLogService.logTaskChange(
                    dto.getId(),
                    currentUserId,
                    TaskActionTypeEnum.PRIORITY_CHANGE.getCode(),
                    "priority",
                    existingTask.getPriority() != null ? existingTask.getPriority().toString() : null,
                    dto.getPriority() != null ? dto.getPriority().toString() : null,
                    "修改任务优先级"
            );
        }

        if (!Objects.equals(existingTask.getAssigneeId(), dto.getAssigneeId())) {
            String oldName = existingTask.getAssigneeId() != null ? getUserName(existingTask.getAssigneeId()) : null;
            String newName = dto.getAssigneeId() != null ? getUserName(dto.getAssigneeId()) : null;
            taskLogService.logTaskChange(
                    dto.getId(),
                    currentUserId,
                    TaskActionTypeEnum.ASSIGNEE_CHANGE.getCode(),
                    "assigneeId",
                    oldName,
                    newName,
                    "修改任务负责人"
            );
        }

        if (!Objects.equals(existingTask.getStartDate(), dto.getStartDate())) {
            taskLogService.logTaskChange(
                    dto.getId(),
                    currentUserId,
                    TaskActionTypeEnum.UPDATE.getCode(),
                    "startDate",
                    existingTask.getStartDate() != null ? existingTask.getStartDate().toString() : null,
                    dto.getStartDate() != null ? dto.getStartDate().toString() : null,
                    "修改开始日期"
            );
        }

        if (!Objects.equals(existingTask.getEndDate(), dto.getEndDate())) {
            taskLogService.logTaskChange(
                    dto.getId(),
                    currentUserId,
                    TaskActionTypeEnum.UPDATE.getCode(),
                    "endDate",
                    existingTask.getEndDate() != null ? existingTask.getEndDate().toString() : null,
                    dto.getEndDate() != null ? dto.getEndDate().toString() : null,
                    "修改结束日期"
            );
        }

        if (!Objects.equals(existingTask.getEstimateHours(), dto.getEstimateHours())) {
            taskLogService.logTaskChange(
                    dto.getId(),
                    currentUserId,
                    TaskActionTypeEnum.UPDATE.getCode(),
                    "estimateHours",
                    existingTask.getEstimateHours() != null ? existingTask.getEstimateHours().toString() : null,
                    dto.getEstimateHours() != null ? dto.getEstimateHours().toString() : null,
                    "修改预估工时"
            );
        }

        if (!Objects.equals(existingTask.getProgress(), dto.getProgress())) {
            taskLogService.logTaskChange(
                    dto.getId(),
                    currentUserId,
                    TaskActionTypeEnum.PROGRESS_CHANGE.getCode(),
                    "progress",
                    existingTask.getProgress() != null ? existingTask.getProgress().toString() : null,
                    dto.getProgress() != null ? dto.getProgress().toString() : null,
                    "修改任务进度"
            );
        }

        if (!Objects.equals(existingTask.getParentId(), dto.getParentId())) {
            taskLogService.logTaskChange(
                    dto.getId(),
                    currentUserId,
                    TaskActionTypeEnum.UPDATE.getCode(),
                    "parentId",
                    existingTask.getParentId() != null ? existingTask.getParentId().toString() : null,
                    dto.getParentId() != null ? dto.getParentId().toString() : null,
                    "修改父任务"
            );
        }

        if (!Objects.equals(existingTask.getPhaseId(), dto.getPhaseId())) {
            taskLogService.logTaskChange(
                    dto.getId(),
                    currentUserId,
                    TaskActionTypeEnum.UPDATE.getCode(),
                    "phaseId",
                    existingTask.getPhaseId() != null ? existingTask.getPhaseId().toString() : null,
                    dto.getPhaseId() != null ? dto.getPhaseId().toString() : null,
                    "修改阶段"
            );
        }

        LambdaUpdateWrapper<Task> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Task::getId, dto.getId());
        updateWrapper.set(Task::getTaskName, dto.getTaskName());
        updateWrapper.set(Task::getDescription, dto.getDescription());
        updateWrapper.set(Task::getTaskType, dto.getTaskType());
        updateWrapper.set(Task::getPriority, dto.getPriority());
        updateWrapper.set(Task::getAssigneeId, dto.getAssigneeId());
        updateWrapper.set(Task::getStartDate, dto.getStartDate());
        updateWrapper.set(Task::getEndDate, dto.getEndDate());
        updateWrapper.set(Task::getEstimateHours, dto.getEstimateHours());
        updateWrapper.set(Task::getProgress, dto.getProgress());
        updateWrapper.set(Task::getParentId, dto.getParentId());
        updateWrapper.set(Task::getPhaseId, dto.getPhaseId());
        if (dto.getSortOrder() != null) {
            updateWrapper.set(Task::getSortOrder, dto.getSortOrder());
        }
        baseMapper.update(null, updateWrapper);

        updateTaskTags(dto.getId(), dto.getTagIds());
    }

    @Transactional
    public void removeTask(Long id) {
        Task task = baseMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        LambdaQueryWrapper<Task> subtaskWrapper = new LambdaQueryWrapper<>();
        subtaskWrapper.eq(Task::getParentId, id);
        Long subtaskCount = baseMapper.selectCount(subtaskWrapper);
        if (subtaskCount > 0) {
            throw new BusinessException("该任务存在子任务，无法删除");
        }

        LambdaQueryWrapper<TaskTagRel> tagRelWrapper = new LambdaQueryWrapper<>();
        tagRelWrapper.eq(TaskTagRel::getTaskId, id);
        taskTagRelMapper.delete(tagRelWrapper);

        LambdaQueryWrapper<TaskWorklog> worklogWrapper = new LambdaQueryWrapper<>();
        worklogWrapper.eq(TaskWorklog::getTaskId, id);
        taskWorklogMapper.delete(worklogWrapper);

        LambdaQueryWrapper<TaskLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(TaskLog::getTaskId, id);

        baseMapper.deleteById(id);
    }

    @Transactional
    public void updateStatus(TaskStatusUpdateDTO dto) {
        Task task = baseMapper.selectById(dto.getTaskId());
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        if (Objects.equals(task.getStatus(), dto.getStatus())) {
            return;
        }

        String oldStatusName = TaskStatusEnum.getDescByCode(task.getStatus());
        String newStatusName = TaskStatusEnum.getDescByCode(dto.getStatus());

        LambdaUpdateWrapper<Task> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Task::getId, dto.getTaskId());
        updateWrapper.set(Task::getStatus, dto.getStatus());

        LocalDateTime now = LocalDateTime.now();
        if (dto.getStatus().equals(TaskStatusEnum.IN_PROGRESS.getCode()) && task.getActualStartDate() == null) {
            updateWrapper.set(Task::getActualStartDate, now);
        }

        if ((dto.getStatus().equals(TaskStatusEnum.DONE.getCode()) ||
                dto.getStatus().equals(TaskStatusEnum.CANCELLED.getCode())) &&
                task.getActualEndDate() == null) {
            updateWrapper.set(Task::getActualEndDate, now);
            if (dto.getStatus().equals(TaskStatusEnum.DONE.getCode())) {
                updateWrapper.set(Task::getProgress, 100);
            }
        }

        baseMapper.update(null, updateWrapper);

        taskLogService.logTaskChange(
                dto.getTaskId(),
                UserContext.getUserId(),
                TaskActionTypeEnum.STATUS_CHANGE.getCode(),
                "status",
                oldStatusName,
                newStatusName,
                dto.getRemark()
        );
    }

    public List<Task> getTree(Long projectId) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getProjectId, projectId);
        wrapper.orderByAsc(Task::getSortOrder).orderByDesc(Task::getCreateTime);
        List<Task> tasks = baseMapper.selectList(wrapper);
        fillTaskExtraInfo(tasks);
        return TreeUtil.buildTree(tasks, null);
    }

    public List<Task> getMyTasks() {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getAssigneeId, userId);
        wrapper.ne(Task::getStatus, TaskStatusEnum.DONE.getCode());
        wrapper.ne(Task::getStatus, TaskStatusEnum.CANCELLED.getCode());
        wrapper.orderByAsc(Task::getPriority).orderByDesc(Task::getCreateTime);
        List<Task> tasks = baseMapper.selectList(wrapper);
        fillTaskExtraInfo(tasks);
        return tasks;
    }

    @Transactional
    public void updateTaskOrder(Long taskId, Integer sortOrder) {
        Task task = baseMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        LambdaUpdateWrapper<Task> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Task::getId, taskId);
        updateWrapper.set(Task::getSortOrder, sortOrder);
        baseMapper.update(null, updateWrapper);
    }

    private LambdaQueryWrapper<Task> buildQueryWrapper(TaskQueryDTO query) {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Task::getTaskName, query.getKeyword())
                    .or()
                    .like(Task::getTaskNo, query.getKeyword())
                    .or()
                    .like(Task::getDescription, query.getKeyword()));
        }

        if (query.getProjectId() != null) {
            wrapper.eq(Task::getProjectId, query.getProjectId());
        }

        if (query.getPhaseId() != null) {
            wrapper.eq(Task::getPhaseId, query.getPhaseId());
        }

        if (query.getParentId() != null) {
            wrapper.eq(Task::getParentId, query.getParentId());
        }

        if (query.getTaskType() != null) {
            wrapper.eq(Task::getTaskType, query.getTaskType());
        }

        if (!CollectionUtils.isEmpty(query.getStatusList())) {
            wrapper.in(Task::getStatus, query.getStatusList());
        }

        if (!CollectionUtils.isEmpty(query.getPriorityList())) {
            wrapper.in(Task::getPriority, query.getPriorityList());
        }

        if (query.getAssigneeId() != null) {
            wrapper.eq(Task::getAssigneeId, query.getAssigneeId());
        }

        if (query.getCreatorId() != null) {
            wrapper.eq(Task::getCreatorId, query.getCreatorId());
        }

        if (!CollectionUtils.isEmpty(query.getTagIds())) {
            LambdaQueryWrapper<TaskTagRel> tagRelWrapper = new LambdaQueryWrapper<>();
            tagRelWrapper.in(TaskTagRel::getTagId, query.getTagIds());
            List<TaskTagRel> tagRels = taskTagRelMapper.selectList(tagRelWrapper);
            if (!tagRels.isEmpty()) {
                List<Long> taskIds = tagRels.stream()
                        .map(TaskTagRel::getTaskId)
                        .distinct()
                        .collect(Collectors.toList());
                wrapper.in(Task::getId, taskIds);
            } else {
                wrapper.in(Task::getId, List.of(-1L));
            }
        }

        return wrapper;
    }

    private void fillTaskExtraInfo(List<Task> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }

        List<Long> projectIds = tasks.stream()
                .map(Task::getProjectId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<Long> userIds = new ArrayList<>();

        tasks.forEach(task -> {
            if (task.getAssigneeId() != null) {
                userIds.add(task.getAssigneeId());
            }
            if (task.getCreatorId() != null) {
                userIds.add(task.getCreatorId());
            }
        });
        userIds = userIds.stream().distinct().collect(Collectors.toList());

        if (!projectIds.isEmpty()) {
            List<Project> projects = projectMapper.selectBatchIds(projectIds);
            Map<Long, Project> projectMap = projects.stream()
                    .collect(Collectors.toMap(Project::getId, p -> p));
            tasks.forEach(task -> {
                Project project = projectMap.get(task.getProjectId());
                if (project != null) {
                    task.setProjectName(project.getProjectName());
                }
            });
        }

        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            Map<Long, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
            tasks.forEach(task -> {
                User assignee = userMap.get(task.getAssigneeId());
                if (assignee != null) {
                    task.setAssigneeName(assignee.getRealName());
                    task.setAssigneeAvatar(assignee.getAvatar());
                }
                User creator = userMap.get(task.getCreatorId());
                if (creator != null) {
                    task.setCreatorName(creator.getRealName());
                }
            });
        }

        List<Long> taskIds = tasks.stream().map(Task::getId).collect(Collectors.toList());
        LambdaQueryWrapper<Task> subtaskWrapper = new LambdaQueryWrapper<>();
        subtaskWrapper.in(Task::getParentId, taskIds);
        List<Task> allSubtasks = baseMapper.selectList(subtaskWrapper);
        Map<Long, List<Task>> subtaskMap = allSubtasks.stream()
                .collect(Collectors.groupingBy(Task::getParentId));

        for (Task task : tasks) {
            List<Task> subtasks = subtaskMap.get(task.getId());
            if (subtasks != null) {
                task.setSubtaskCount(subtasks.size());
                long completedCount = subtasks.stream()
                        .filter(s -> TaskStatusEnum.DONE.getCode().equals(s.getStatus()))
                        .count();
                task.setCompletedSubtaskCount((int) completedCount);
            } else {
                task.setSubtaskCount(0);
                task.setCompletedSubtaskCount(0);
            }
        }
    }

    private List<TaskTag> getTaskTags(Long taskId) {
        LambdaQueryWrapper<TaskTagRel> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(TaskTagRel::getTaskId, taskId);
        List<TaskTagRel> tagRels = taskTagRelMapper.selectList(relWrapper);

        if (tagRels.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> tagIds = tagRels.stream()
                .map(TaskTagRel::getTagId)
                .collect(Collectors.toList());
        return taskTagMapper.selectBatchIds(tagIds);
    }

    private void saveTaskTags(Long taskId, List<Long> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }

        for (Long tagId : tagIds) {
            TaskTagRel rel = new TaskTagRel();
            rel.setTaskId(taskId);
            rel.setTagId(tagId);
            rel.setCreateTime(LocalDateTime.now());
            taskTagRelMapper.insert(rel);
        }

        if (!tagIds.isEmpty()) {
            taskLogService.logTaskChange(
                    taskId,
                    UserContext.getUserId(),
                    TaskActionTypeEnum.TAG_CHANGE.getCode(),
                    "tags",
                    null,
                    tagIds.toString(),
                    "设置任务标签"
            );
        }
    }

    private void updateTaskTags(Long taskId, List<Long> newTagIds) {
        List<TaskTag> oldTags = getTaskTags(taskId);
        List<Long> oldTagIds = oldTags.stream()
                .map(TaskTag::getId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<TaskTagRel> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(TaskTagRel::getTaskId, taskId);
        taskTagRelMapper.delete(deleteWrapper);

        saveTaskTags(taskId, newTagIds);

        if (!isEqualCollection(oldTagIds, newTagIds)) {
            String oldTagsStr = oldTags.stream()
                    .map(TaskTag::getTagName)
                    .collect(Collectors.joining(","));
            String newTagsStr = "";
            if (newTagIds != null && !newTagIds.isEmpty()) {
                List<TaskTag> newTagList = taskTagMapper.selectBatchIds(newTagIds);
                newTagsStr = newTagList.stream()
                        .map(TaskTag::getTagName)
                        .collect(Collectors.joining(","));
            }
            taskLogService.logTaskChange(
                    taskId,
                    UserContext.getUserId(),
                    TaskActionTypeEnum.TAG_CHANGE.getCode(),
                    "tags",
                    oldTagsStr,
                    newTagsStr,
                    "修改任务标签"
            );
        }
    }

    private synchronized String generateTaskNo() {
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Task::getTaskNo);
        wrapper.last("LIMIT 1");
        Task lastTask = baseMapper.selectOne(wrapper);

        int nextNo = 1;
        if (lastTask != null && lastTask.getTaskNo() != null) {
            String taskNo = lastTask.getTaskNo();
            String numStr = taskNo.replace("TASK_", "");
            try {
                nextNo = Integer.parseInt(numStr) + 1;
            } catch (NumberFormatException e) {
                nextNo = 1;
            }
        }

        return String.format("TASK_%06d", nextNo);
    }

    private String getUserName(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null ? user.getRealName() : userId.toString();
    }

    private boolean isEqualCollection(List<?> a, List<?> b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.size() != b.size()) {
            return false;
        }
        return a.containsAll(b) && b.containsAll(a);
    }
}
