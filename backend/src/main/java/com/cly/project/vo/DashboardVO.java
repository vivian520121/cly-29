package com.cly.project.vo;

import com.cly.project.entity.Task;
import com.cly.project.entity.TaskLog;
import lombok.Data;

import java.util.List;

@Data
public class DashboardVO {

    private StatisticsVO statistics;

    private List<Task> todoTasks;

    private List<TaskLog> dynamics;
}
