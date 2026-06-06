package com.cly.project.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProjectOverviewVO {

    private Long id;

    private String projectName;

    private String projectCode;

    private Integer status;

    private String statusName;

    private Integer priority;

    private String priorityName;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDate actualStartDate;

    private LocalDate actualEndDate;

    private BigDecimal budget;

    private Integer progress;

    private Integer totalTaskCount;

    private Integer completedTaskCount;

    private Integer inProgressTaskCount;

    private Integer todoTaskCount;

    private Integer cancelledTaskCount;

    private Integer memberCount;

    private Integer milestoneCount;

    private Integer completedMilestoneCount;

    private Integer phaseCount;
}
