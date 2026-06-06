package com.cly.project.controller;

import com.cly.project.annotation.OperationLog;
import com.cly.project.common.Result;
import com.cly.project.entity.TaskLog;
import com.cly.project.service.TaskLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task-log")
@RequiredArgsConstructor
@Tag(name = "任务日志管理", description = "任务流转日志管理接口")
public class TaskLogController {

    private final TaskLogService taskLogService;

    @GetMapping("/list/{taskId}")
    @Operation(summary = "查询任务流转日志列表")
    @OperationLog(module = "任务日志管理", operation = "查询任务日志列表", businessType = "QUERY", businessIdIndex = 0)
    public Result<List<TaskLog>> listByTaskId(@PathVariable Long taskId) {
        return Result.success(taskLogService.listByTaskId(taskId));
    }
}
