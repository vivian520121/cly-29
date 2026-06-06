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
@TableName("pm_project")
public class Project extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long companyId;

    private String projectName;

    private String projectCode;

    private Integer projectType;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualEndDate;

    private Integer status;

    private Integer progress;

    private Integer priority;

    private Long managerId;

    private BigDecimal budget;

    private String color;

    @TableField(exist = false)
    private String managerName;

    @TableField(exist = false)
    private Integer taskCount;

    @TableField(exist = false)
    private Integer completedTaskCount;
}
