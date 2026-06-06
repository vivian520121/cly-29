package com.cly.project.controller;

import com.cly.project.annotation.OperationLog;
import com.cly.project.common.Result;
import com.cly.project.dto.MemberAddDTO;
import com.cly.project.dto.MemberRoleUpdateDTO;
import com.cly.project.entity.ProjectMember;
import com.cly.project.service.ProjectMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
@Tag(name = "项目成员管理", description = "项目成员管理接口")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @GetMapping("/list/{projectId}")
    @Operation(summary = "查询项目成员列表")
    @OperationLog(module = "项目成员管理", operation = "查询项目成员列表", businessType = "QUERY", businessIdIndex = 0)
    public Result<List<ProjectMember>> listByProjectId(@PathVariable Long projectId) {
        return Result.success(projectMemberService.listByProjectId(projectId));
    }

    @PostMapping("/add")
    @Operation(summary = "添加项目成员")
    @OperationLog(module = "项目成员管理", operation = "添加项目成员", businessType = "INSERT")
    public Result<Void> addMember(@RequestBody @Valid MemberAddDTO dto) {
        projectMemberService.addMember(dto.getProjectId(), dto.getUserId(), dto.getRole());
        return Result.success();
    }

    @PutMapping("/role")
    @Operation(summary = "修改成员角色")
    @OperationLog(module = "项目成员管理", operation = "修改成员角色", businessType = "UPDATE")
    public Result<Void> updateMemberRole(@RequestBody @Valid MemberRoleUpdateDTO dto) {
        projectMemberService.updateMemberRole(dto.getProjectId(), dto.getUserId(), dto.getRole());
        return Result.success();
    }

    @DeleteMapping("/remove")
    @Operation(summary = "移除项目成员")
    @OperationLog(module = "项目成员管理", operation = "移除项目成员", businessType = "DELETE")
    public Result<Void> removeMember(@RequestParam Long projectId, @RequestParam Long userId) {
        projectMemberService.removeMember(projectId, userId);
        return Result.success();
    }
}
