package com.cly.project.controller;

import com.cly.project.annotation.OperationLog;
import com.cly.project.common.Result;
import com.cly.project.entity.ProjectPhase;
import com.cly.project.service.ProjectPhaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/phase")
@RequiredArgsConstructor
@Tag(name = "项目阶段管理", description = "项目阶段管理接口")
public class ProjectPhaseController {

    private final ProjectPhaseService projectPhaseService;

    @GetMapping("/list/{projectId}")
    @Operation(summary = "查询项目阶段列表")
    @OperationLog(module = "项目阶段管理", operation = "查询项目阶段列表", businessType = "QUERY", businessIdIndex = 0)
    public Result<List<ProjectPhase>> listByProjectId(@PathVariable Long projectId) {
        return Result.success(projectPhaseService.listByProjectId(projectId));
    }

    @PostMapping
    @Operation(summary = "新增项目阶段")
    @OperationLog(module = "项目阶段管理", operation = "新增项目阶段", businessType = "INSERT")
    public Result<Void> save(@RequestBody ProjectPhase phase) {
        projectPhaseService.save(phase);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改项目阶段")
    @OperationLog(module = "项目阶段管理", operation = "修改项目阶段", businessType = "UPDATE")
    public Result<Void> update(@RequestBody ProjectPhase phase) {
        projectPhaseService.update(phase);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除项目阶段")
    @OperationLog(module = "项目阶段管理", operation = "删除项目阶段", businessType = "DELETE", businessIdIndex = 0)
    public Result<Void> delete(@PathVariable Long id) {
        projectPhaseService.remove(id);
        return Result.success();
    }
}
