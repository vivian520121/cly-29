package com.cly.project.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.cly.project.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_task_worklog")
public class TaskWorklog extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long userId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;

    private BigDecimal hours;

    private String description;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String realName;

    @TableField(exist = false)
    private String avatar;

    @TableField(exist = false)
    private String taskName;
}
