package com.cly.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cly.project.annotation.OperationLog;
import com.cly.project.common.Result;
import com.cly.project.dto.MemberRoleUpdateDTO;
import com.cly.project.dto.ProjectQueryDTO;
import com.cly.project.dto.ProjectSaveDTO;
import com.cly.project.entity.Milestone;
import com.cly.project.entity.Project;
import com.cly.project.entity.ProjectMember;
import com.cly.project.service.MilestoneService;
import com.cly.project.service.ProjectMemberService;
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
    private final MilestoneService milestoneService;
    private final ProjectMemberService projectMemberService;

    @GetMapping("/page")
    @Operation(summary = "分页查询项目列表")
    @OperationLog(module = "项目管理", operation = "分页查询项目列表", businessType = "QUERY")
    public Result<IPage<Project>> page(ProjectQueryDTO query) {
        return Result.success(projectService.page(query));
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询项目列表")
    @OperationLog(module = "项目管理", operation = "分页查询项目列表", businessType = "QUERY")
    public Result<IPage<Project>> list(ProjectQueryDTO query) {
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
        projectService.saveProject(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改项目")
    @OperationLog(module = "项目管理", operation = "修改项目", businessType = "UPDATE")
    public Result<Void> update(@RequestBody @Valid ProjectSaveDTO dto) {
        projectService.updateProject(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除项目")
    @OperationLog(module = "项目管理", operation = "删除项目", businessType = "DELETE", businessIdIndex = 0)
    public Result<Void> delete(@PathVariable Long id) {
        projectService.removeProject(id);
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

    @GetMapping("/{id}/milestones")
    @Operation(summary = "查询项目里程碑列表")
    @OperationLog(module = "项目管理", operation = "查询项目里程碑列表", businessType = "QUERY", businessIdIndex = 0)
    public Result<List<Milestone>> getMilestones(@PathVariable Long id) {
        return Result.success(milestoneService.listByProjectId(id));
    }

    @PostMapping("/{id}/milestones")
    @Operation(summary = "新增项目里程碑")
    @OperationLog(module = "项目管理", operation = "新增项目里程碑", businessType = "INSERT", businessIdIndex = 0)
    public Result<Void> saveMilestone(@PathVariable Long id, @RequestBody Milestone milestone) {
        milestone.setProjectId(id);
        milestoneService.saveMilestone(milestone);
        return Result.success();
    }

    @PutMapping("/{id}/milestones/{milestoneId}")
    @Operation(summary = "修改项目里程碑")
    @OperationLog(module = "项目管理", operation = "修改项目里程碑", businessType = "UPDATE", businessIdIndex = 0)
    public Result<Void> updateMilestone(@PathVariable Long id, @PathVariable Long milestoneId, @RequestBody Milestone milestone) {
        milestone.setId(milestoneId);
        milestone.setProjectId(id);
        milestoneService.updateMilestone(milestone);
        return Result.success();
    }

    @DeleteMapping("/{id}/milestones/{milestoneId}")
    @Operation(summary = "删除项目里程碑")
    @OperationLog(module = "项目管理", operation = "删除项目里程碑", businessType = "DELETE", businessIdIndex = 0)
    public Result<Void> deleteMilestone(@PathVariable Long id, @PathVariable Long milestoneId) {
        milestoneService.removeMilestone(milestoneId);
        return Result.success();
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "查询项目成员列表")
    @OperationLog(module = "项目管理", operation = "查询项目成员列表", businessType = "QUERY", businessIdIndex = 0)
    public Result<List<ProjectMember>> getMembers(@PathVariable Long id) {
        return Result.success(projectMemberService.listByProjectId(id));
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "添加项目成员")
    @OperationLog(module = "项目管理", operation = "添加项目成员", businessType = "INSERT", businessIdIndex = 0)
    public Result<Void> addMember(@PathVariable Long id, @RequestBody com.cly.project.dto.MemberAddDTO dto) {
        projectMemberService.addMember(id, dto.getUserId(), dto.getRole());
        return Result.success();
    }

    @PutMapping("/{id}/members/{memberId}/role")
    @Operation(summary = "修改成员角色")
    @OperationLog(module = "项目管理", operation = "修改成员角色", businessType = "UPDATE", businessIdIndex = 0)
    public Result<Void> updateMemberRole(@PathVariable Long id, @PathVariable Long memberId, @RequestBody MemberRoleUpdateDTO dto) {
        projectMemberService.updateMemberRole(id, memberId, dto.getRole());
        return Result.success();
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @Operation(summary = "移除项目成员")
    @OperationLog(module = "项目管理", operation = "移除项目成员", businessType = "DELETE", businessIdIndex = 0)
    public Result<Void> removeMember(@PathVariable Long id, @PathVariable Long memberId) {
        projectMemberService.removeMember(id, memberId);
        return Result.success();
    }
}
