package com.cly.project.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.cly.project.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_task")
public class Task extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long phaseId;

    private Long parentId;

    private String taskName;

    private String taskNo;

    private String description;

    private Integer taskType;

    private Integer status;

    private Integer priority;

    private Long assigneeId;

    private Long creatorId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualEndDate;

    private BigDecimal estimateHours;

    private BigDecimal actualHours;

    private Integer progress;

    private Integer sortOrder;

    @TableField(exist = false)
    private String projectName;

    @TableField(exist = false)
    private String phaseName;

    @TableField(exist = false)
    private String assigneeName;

    @TableField(exist = false)
    private String assigneeAvatar;

    @TableField(exist = false)
    private String creatorName;

    @TableField(exist = false)
    private List<Long> tagIds;

    @TableField(exist = false)
    private List<Task> children;

    @TableField(exist = false)
    private Integer subtaskCount;

    @TableField(exist = false)
    private Integer completedSubtaskCount;
}
