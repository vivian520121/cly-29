package com.cly.project.controller;

import com.cly.project.annotation.OperationLog;
import com.cly.project.common.Result;
import com.cly.project.dto.WorklogSaveDTO;
import com.cly.project.entity.TaskWorklog;
import com.cly.project.service.TaskWorklogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/worklog")
@RequiredArgsConstructor
@Tag(name = "工时管理", description = "任务工时管理接口")
public class TaskWorklogController {

    private final TaskWorklogService taskWorklogService;

    @GetMapping("/list/{taskId}")
    @Operation(summary = "查询任务工时列表")
    @OperationLog(module = "工时管理", operation = "查询任务工时列表", businessType = "QUERY", businessIdIndex = 0)
    public Result<List<TaskWorklog>> listByTaskId(@PathVariable Long taskId) {
        return Result.success(taskWorklogService.listByTaskId(taskId));
    }

    @PostMapping
    @Operation(summary = "新增工时")
    @OperationLog(module = "工时管理", operation = "新增工时", businessType = "INSERT")
    public Result<Void> save(@RequestBody @Valid WorklogSaveDTO dto) {
        taskWorklogService.saveWorklog(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改工时")
    @OperationLog(module = "工时管理", operation = "修改工时", businessType = "UPDATE")
    public Result<Void> update(@RequestBody @Valid WorklogSaveDTO dto) {
        taskWorklogService.updateWorklog(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除工时")
    @OperationLog(module = "工时管理", operation = "删除工时", businessType = "DELETE", businessIdIndex = 0)
    public Result<Void> delete(@PathVariable Long id) {
        taskWorklogService.removeWorklog(id);
        return Result.success();
    }
}
