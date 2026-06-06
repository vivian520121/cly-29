package com.cly.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cly.project.dto.WorklogSaveDTO;
import com.cly.project.entity.Task;
import com.cly.project.entity.TaskWorklog;
import com.cly.project.entity.User;
import com.cly.project.enums.TaskActionTypeEnum;
import com.cly.project.exception.BusinessException;
import com.cly.project.mapper.TaskMapper;
import com.cly.project.mapper.TaskWorklogMapper;
import com.cly.project.mapper.UserMapper;
import com.cly.project.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskWorklogService extends ServiceImpl<TaskWorklogMapper, TaskWorklog> {

    private final UserMapper userMapper;
    private final TaskMapper taskMapper;
    private final TaskLogService taskLogService;

    public List<TaskWorklog> listByTaskId(Long taskId) {
        LambdaQueryWrapper<TaskWorklog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskWorklog::getTaskId, taskId);
        wrapper.orderByDesc(TaskWorklog::getWorkDate);
        List<TaskWorklog> worklogs = baseMapper.selectList(wrapper);

        if (!worklogs.isEmpty()) {
            List<Long> userIds = worklogs.stream()
                    .map(TaskWorklog::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            List<User> users = userMapper.selectBatchIds(userIds);
            Map<Long, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));

            for (TaskWorklog worklog : worklogs) {
                User user = userMap.get(worklog.getUserId());
                if (user != null) {
                    worklog.setUsername(user.getUsername());
                    worklog.setRealName(user.getRealName());
                    worklog.setAvatar(user.getAvatar());
                }
            }
        }

        return worklogs;
    }

    @Transactional
    public void saveWorklog(WorklogSaveDTO dto) {
        Task task = taskMapper.selectById(dto.getTaskId());
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        TaskWorklog worklog = new TaskWorklog();
        BeanUtils.copyProperties(dto, worklog);
        worklog.setUserId(UserContext.getUserId());
        baseMapper.insert(worklog);

        updateTaskActualHours(dto.getTaskId());

        taskLogService.logTaskChange(
                dto.getTaskId(),
                UserContext.getUserId(),
                TaskActionTypeEnum.WORKLOG_ADD.getCode(),
                "worklog",
                null,
                dto.getHours() + "小时",
                dto.getDescription()
        );
    }

    @Transactional
    public void updateWorklog(WorklogSaveDTO dto) {
        TaskWorklog existingWorklog = baseMapper.selectById(dto.getId());
        if (existingWorklog == null) {
            throw new BusinessException("工时记录不存在");
        }

        BigDecimal oldHours = existingWorklog.getHours();

        BeanUtils.copyProperties(dto, existingWorklog);
        baseMapper.updateById(existingWorklog);

        updateTaskActualHours(existingWorklog.getTaskId());

        if (!oldHours.equals(dto.getHours())) {
            taskLogService.logTaskChange(
                    existingWorklog.getTaskId(),
                    UserContext.getUserId(),
                    TaskActionTypeEnum.WORKLOG_ADD.getCode(),
                    "worklog",
                    oldHours + "小时",
                    dto.getHours() + "小时",
                    dto.getDescription()
            );
        }
    }

    @Transactional
    public void removeWorklog(Long id) {
        TaskWorklog worklog = baseMapper.selectById(id);
        if (worklog == null) {
            throw new BusinessException("工时记录不存在");
        }

        baseMapper.deleteById(id);

        updateTaskActualHours(worklog.getTaskId());

        taskLogService.logTaskChange(
                worklog.getTaskId(),
                UserContext.getUserId(),
                TaskActionTypeEnum.WORKLOG_ADD.getCode(),
                "worklog",
                worklog.getHours() + "小时",
                null,
                "删除工时记录"
        );
    }

    private void updateTaskActualHours(Long taskId) {
        LambdaQueryWrapper<TaskWorklog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskWorklog::getTaskId, taskId);
        List<TaskWorklog> worklogs = baseMapper.selectList(wrapper);

        BigDecimal totalHours = worklogs.stream()
                .map(TaskWorklog::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Task task = new Task();
        task.setId(taskId);
        task.setActualHours(totalHours);
        taskMapper.updateById(task);
    }
}
