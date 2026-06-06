package com.cly.project.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pm_task_log")
public class TaskLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long userId;

    private String actionType;

    private String fieldName;

    private String oldValue;

    private String newValue;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String realName;

    @TableField(exist = false)
    private String avatar;

    @TableField(exist = false)
    private String actionTypeName;
}
