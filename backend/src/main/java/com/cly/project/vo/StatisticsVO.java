package com.cly.project.vo;

import lombok.Data;

@Data
public class StatisticsVO {

    private Integer totalTasks;

    private Integer completedTasks;

    private Integer inProgressTasks;

    private Integer todoTasks;

    private Integer reviewTasks;

    private Integer cancelledTasks;

    private Double completionRate;

    private Integer totalProjects;

    private Integer activeProjects;

    private Integer completedProjects;
}
