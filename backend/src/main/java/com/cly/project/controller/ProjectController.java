package com.cly.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cly.project.annotation.OperationLog;
import com.cly.project.common.Result;
import com.cly.project.dto.ProjectQueryDTO;
import com.cly.project.dto.ProjectSaveDTO;
import com.cly.project.entity.Project;
import com.cly.project.service.ProjectService;
import com.cly.project.vo.GanttDataVO;
import com.cly.project.vo.ProjectOverviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
@Tag(name = "项目管理", description = "项目信息管理接口")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/page")
    @Operation(summary = "分页查询项目列表")
    @OperationLog(module = "项目管理", operation = "分页查询项目列表", businessType = "QUERY")
    public Result<IPage<Project>> page(ProjectQueryDTO query) {
        return Result.success(projectService.page(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询项目详情")
    @OperationLog(module = "项目管理", operation = "查询项目详情", businessType = "QUERY", businessIdIndex = 0)
    public Result<Project> getById(@PathVariable Long id) {
        return Result.success(projectService.getById(id));
    }

    @PostMapping
    @Operation(summary = "新增项目")
    @OperationLog(module = "项目管理", operation = "新增项目", businessType = "INSERT")
    public Result<Void> save(@RequestBody @Valid ProjectSaveDTO dto) {
        projectService.save(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改项目")
    @OperationLog(module = "项目管理", operation = "修改项目", businessType = "UPDATE")
    public Result<Void> update(@RequestBody @Valid ProjectSaveDTO dto) {
        projectService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除项目")
    @OperationLog(module = "项目管理", operation = "删除项目", businessType = "DELETE", businessIdIndex = 0)
    public Result<Void> delete(@PathVariable Long id) {
        projectService.remove(id);
        return Result.success();
    }

    @GetMapping("/{id}/overview")
    @Operation(summary = "获取项目概览")
    @OperationLog(module = "项目管理", operation = "获取项目概览", businessType = "QUERY", businessIdIndex = 0)
    public Result<ProjectOverviewVO> getOverview(@PathVariable Long id) {
        return Result.success(projectService.getOverview(id));
    }

    @GetMapping("/{id}/gantt")
    @Operation(summary = "获取项目甘特图数据")
    @OperationLog(module = "项目管理", operation = "获取项目甘特图数据", businessType = "QUERY", businessIdIndex = 0)
    public Result<GanttDataVO> getGanttData(@PathVariable Long id) {
        return Result.success(projectService.getGanttData(id));
    }

    @GetMapping("/my")
    @Operation(summary = "获取我参与的项目列表")
    @OperationLog(module = "项目管理", operation = "获取我参与的项目列表", businessType = "QUERY")
    public Result<List<Project>> getMyProjects() {
        return Result.success(projectService.getMyProjects());
    }
}
