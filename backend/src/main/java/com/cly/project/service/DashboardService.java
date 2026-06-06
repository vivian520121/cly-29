package com.cly.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cly.project.entity.Project;
import com.cly.project.entity.Task;
import com.cly.project.entity.TaskLog;
import com.cly.project.entity.User;
import com.cly.project.enums.ProjectStatusEnum;
import com.cly.project.enums.TaskActionTypeEnum;
import com.cly.project.enums.TaskStatusEnum;
import com.cly.project.mapper.ProjectMapper;
import com.cly.project.mapper.TaskLogMapper;
import com.cly.project.mapper.TaskMapper;
import com.cly.project.mapper.UserMapper;
import com.cly.project.util.UserContext;
import com.cly.project.vo.DashboardVO;
import com.cly.project.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TaskMapper taskMapper;
    private final ProjectMapper projectMapper;
    private final TaskLogMapper taskLogMapper;
    private final UserMapper userMapper;

    public DashboardVO getDashboard() {
        DashboardVO vo = new DashboardVO();
        vo.setStatistics(getStatistics());
        vo.setTodoTasks(getTodoTasks());
        vo.setDynamics(getProjectDynamics());
        return vo;
    }

    public List<Task> getTodoTasks() {
        Long currentUserId = UserContext.getUserId();
        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Task::getAssigneeId, currentUserId)
                .in(Task::getStatus, Arrays.asList(
                        TaskStatusEnum.TODO.getCode(),
                        TaskStatusEnum.IN_PROGRESS.getCode(),
                        TaskStatusEnum.REVIEW.getCode()
                ))
                .orderByAsc(Task::getPriority)
                .orderByDesc(Task::getCreateTime);

        List<Task> tasks = taskMapper.selectList(wrapper);
        fillTaskExtraInfo(tasks);
        return tasks;
    }

    public List<TaskLog> getProjectDynamics() {
        LambdaQueryWrapper<TaskLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(TaskLog::getCreateTime)
                .last("LIMIT 20");

        List<TaskLog> logs = taskLogMapper.selectList(wrapper);
        fillTaskLogExtraInfo(logs);
        return logs;
    }

    public StatisticsVO getStatistics() {
        Long currentUserId = UserContext.getUserId();
        StatisticsVO vo = new StatisticsVO();

        LambdaQueryWrapper<Task> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.eq(Task::getAssigneeId, currentUserId);
        List<Task> allTasks = taskMapper.selectList(taskWrapper);

        int totalTasks = allTasks.size();
        int completedTasks = (int) allTasks.stream()
                .filter(t -> TaskStatusEnum.DONE.getCode().equals(t.getStatus()))
                .count();
        int inProgressTasks = (int) allTasks.stream()
                .filter(t -> TaskStatusEnum.IN_PROGRESS.getCode().equals(t.getStatus()))
                .count();
        int todoTasks = (int) allTasks.stream()
                .filter(t -> TaskStatusEnum.TODO.getCode().equals(t.getStatus()))
                .count();
        int reviewTasks = (int) allTasks.stream()
                .filter(t -> TaskStatusEnum.REVIEW.getCode().equals(t.getStatus()))
                .count();
        int cancelledTasks = (int) allTasks.stream()
                .filter(t -> TaskStatusEnum.CANCELLED.getCode().equals(t.getStatus()))
                .count();

        vo.setTotalTasks(totalTasks);
        vo.setCompletedTasks(completedTasks);
        vo.setInProgressTasks(inProgressTasks);
        vo.setTodoTasks(todoTasks);
        vo.setReviewTasks(reviewTasks);
        vo.setCancelledTasks(cancelledTasks);

        if (totalTasks > 0) {
            double completionRate = (double) completedTasks / totalTasks * 100;
            vo.setCompletionRate(Math.round(completionRate * 100) / 100.0);
        } else {
            vo.setCompletionRate(0.0);
        }

        LambdaQueryWrapper<Project> projectWrapper = new LambdaQueryWrapper<>();
        projectWrapper.eq(Project::getManagerId, currentUserId);
        List<Project> projects = projectMapper.selectList(projectWrapper);

        int totalProjects = projects.size();
        int activeProjects = (int) projects.stream()
                .filter(p -> ProjectStatusEnum.IN_PROGRESS.getCode().equals(p.getStatus()))
                .count();
        int completedProjects = (int) projects.stream()
                .filter(p -> ProjectStatusEnum.COMPLETED.getCode().equals(p.getStatus()))
                .count();

        vo.setTotalProjects(totalProjects);
        vo.setActiveProjects(activeProjects);
        vo.setCompletedProjects(completedProjects);

        return vo;
    }

    private void fillTaskExtraInfo(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return;
        }

        Set<Long> userIds = new HashSet<>();
        Set<Long> projectIds = new HashSet<>();

        for (Task task : tasks) {
            if (task.getAssigneeId() != null) {
                userIds.add(task.getAssigneeId());
            }
            if (task.getCreatorId() != null) {
                userIds.add(task.getCreatorId());
            }
            if (task.getProjectId() != null) {
                projectIds.add(task.getProjectId());
            }
        }

        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
        }

        Map<Long, Project> projectMap = new HashMap<>();
        if (!projectIds.isEmpty()) {
            List<Project> projects = projectMapper.selectBatchIds(projectIds);
            projectMap = projects.stream()
                    .collect(Collectors.toMap(Project::getId, p -> p));
        }

        for (Task task : tasks) {
            User assignee = userMap.get(task.getAssigneeId());
            if (assignee != null) {
                task.setAssigneeName(assignee.getRealName() != null ? assignee.getRealName() : assignee.getUsername());
                task.setAssigneeAvatar(assignee.getAvatar());
            }

            User creator = userMap.get(task.getCreatorId());
            if (creator != null) {
                task.setCreatorName(creator.getRealName() != null ? creator.getRealName() : creator.getUsername());
            }

            Project project = projectMap.get(task.getProjectId());
            if (project != null) {
                task.setProjectName(project.getProjectName());
            }
        }
    }

    private void fillTaskLogExtraInfo(List<TaskLog> logs) {
        if (logs.isEmpty()) {
            return;
        }

        Set<Long> userIds = logs.stream()
                .map(TaskLog::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds);
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
        }

        for (TaskLog log : logs) {
            User user = userMap.get(log.getUserId());
            if (user != null) {
                log.setUsername(user.getUsername());
                log.setRealName(user.getRealName() != null ? user.getRealName() : user.getUsername());
                log.setAvatar(user.getAvatar());
            }

            for (TaskActionTypeEnum type : TaskActionTypeEnum.values()) {
                if (type.getCode().equals(log.getActionType())) {
                    log.setActionTypeName(type.getDesc());
                    break;
                }
            }
        }
    }
}
