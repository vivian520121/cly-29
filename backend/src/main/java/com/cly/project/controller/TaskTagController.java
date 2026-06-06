package com.cly.project.controller;

import com.cly.project.annotation.OperationLog;
import com.cly.project.common.Result;
import com.cly.project.entity.TaskTag;
import com.cly.project.service.TaskTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task-tag")
@RequiredArgsConstructor
@Tag(name = "任务标签管理", description = "任务标签管理接口")
public class TaskTagController {

    private final TaskTagService taskTagService;

    @GetMapping("/list/{projectId}")
    @Operation(summary = "查询项目标签列表")
    @OperationLog(module = "任务标签管理", operation = "查询项目标签列表", businessType = "QUERY", businessIdIndex = 0)
    public Result<List<TaskTag>> listByProjectId(@PathVariable Long projectId) {
        return Result.success(taskTagService.listByProjectId(projectId));
    }

    @PostMapping
    @Operation(summary = "新增标签")
    @OperationLog(module = "任务标签管理", operation = "新增标签", businessType = "INSERT")
    public Result<Void> save(@RequestBody TaskTag tag) {
        taskTagService.saveTag(tag);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改标签")
    @OperationLog(module = "任务标签管理", operation = "修改标签", businessType = "UPDATE")
    public Result<Void> update(@RequestBody TaskTag tag) {
        taskTagService.updateTag(tag);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除标签")
    @OperationLog(module = "任务标签管理", operation = "删除标签", businessType = "DELETE", businessIdIndex = 0)
    public Result<Void> delete(@PathVariable Long id) {
        taskTagService.removeTag(id);
        return Result.success();
    }
}
