package com.cly.project.controller;

import com.cly.project.annotation.OperationLog;
import com.cly.project.common.Result;
import com.cly.project.entity.Milestone;
import com.cly.project.service.MilestoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/milestone")
@RequiredArgsConstructor
@Tag(name = "里程碑管理", description = "项目里程碑管理接口")
public class MilestoneController {

    private final MilestoneService milestoneService;

    @GetMapping("/list/{projectId}")
    @Operation(summary = "查询项目里程碑列表")
    @OperationLog(module = "里程碑管理", operation = "查询项目里程碑列表", businessType = "QUERY", businessIdIndex = 0)
    public Result<List<Milestone>> listByProjectId(@PathVariable Long projectId) {
        return Result.success(milestoneService.listByProjectId(projectId));
    }

    @PostMapping
    @Operation(summary = "新增里程碑")
    @OperationLog(module = "里程碑管理", operation = "新增里程碑", businessType = "INSERT")
    public Result<Void> save(@RequestBody Milestone milestone) {
        milestoneService.save(milestone);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改里程碑")
    @OperationLog(module = "里程碑管理", operation = "修改里程碑", businessType = "UPDATE")
    public Result<Void> update(@RequestBody Milestone milestone) {
        milestoneService.update(milestone);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除里程碑")
    @OperationLog(module = "里程碑管理", operation = "删除里程碑", businessType = "DELETE", businessIdIndex = 0)
    public Result<Void> delete(@PathVariable Long id) {
        milestoneService.remove(id);
        return Result.success();
    }
}
