package com.cly.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cly.project.entity.TaskLog;
import com.cly.project.entity.User;
import com.cly.project.enums.TaskActionTypeEnum;
import com.cly.project.mapper.TaskLogMapper;
import com.cly.project.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskLogService extends ServiceImpl<TaskLogMapper, TaskLog> {

    private final UserMapper userMapper;

    public List<TaskLog> listByTaskId(Long taskId) {
        LambdaQueryWrapper<TaskLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskLog::getTaskId, taskId);
        wrapper.orderByDesc(TaskLog::getCreateTime);
        List<TaskLog> logs = baseMapper.selectList(wrapper);

        if (!logs.isEmpty()) {
            List<Long> userIds = logs.stream()
                    .map(TaskLog::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            List<User> users = userMapper.selectBatchIds(userIds);
            Map<Long, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u));

            for (TaskLog log : logs) {
                User user = userMap.get(log.getUserId());
                if (user != null) {
                    log.setUsername(user.getUsername());
                    log.setRealName(user.getRealName());
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

        return logs;
    }

    public void logTaskChange(Long taskId, Long userId, String actionType, String fieldName,
                              String oldValue, String newValue, String remark) {
        TaskLog log = new TaskLog();
        log.setTaskId(taskId);
        log.setUserId(userId);
        log.setActionType(actionType);
        log.setFieldName(fieldName);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        baseMapper.insert(log);
    }
}
