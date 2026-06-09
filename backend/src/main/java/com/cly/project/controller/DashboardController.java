package com.cly.project.controller;

import com.cly.project.annotation.OperationLog;
import com.cly.project.common.Result;
import com.cly.project.entity.Task;
import com.cly.project.entity.TaskLog;
import com.cly.project.service.DashboardService;
import com.cly.project.vo.DashboardVO;
import com.cly.project.vo.StatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "工作台", description = "工作台数据接口")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "工作台概览")
    @OperationLog(module = "工作台", operation = "获取工作台概览", businessType = "QUERY")
    public Result<DashboardVO> getDashboard() {
        return Result.success(dashboardService.getDashboard());
    }

    @GetMapping("/todo")
    @Operation(summary = "待办任务")
    @OperationLog(module = "工作台", operation = "获取待办任务", businessType = "QUERY")
    public Result<List<Task>> getTodoTasks() {
        return Result.success(dashboardService.getTodoTasks());
    }

    @GetMapping("/todo-tasks")
    @Operation(summary = "待办任务")
    @OperationLog(module = "工作台", operation = "获取待办任务", businessType = "QUERY")
    public Result<List<Task>> getTodoTasksAlias() {
        return Result.success(dashboardService.getTodoTasks());
    }

    @GetMapping("/dynamics")
    @Operation(summary = "项目动态")
    @OperationLog(module = "工作台", operation = "获取项目动态", businessType = "QUERY")
    public Result<List<TaskLog>> getProjectDynamics() {
        return Result.success(dashboardService.getProjectDynamics());
    }

    @GetMapping("/activities")
    @Operation(summary = "项目动态")
    @OperationLog(module = "工作台", operation = "获取项目动态", businessType = "QUERY")
    public Result<List<TaskLog>> getProjectActivities() {
        return Result.success(dashboardService.getProjectDynamics());
    }

    @GetMapping("/statistics")
    @Operation(summary = "统计数据")
    @OperationLog(module = "工作台", operation = "获取统计数据", businessType = "QUERY")
    public Result<StatisticsVO> getStatistics() {
        return Result.success(dashboardService.getStatistics());
    }

    @GetMapping("/task-statistics")
    @Operation(summary = "统计数据")
    @OperationLog(module = "工作台", operation = "获取统计数据", businessType = "QUERY")
    public Result<StatisticsVO> getTaskStatistics() {
        return Result.success(dashboardService.getStatistics());
    }
}
