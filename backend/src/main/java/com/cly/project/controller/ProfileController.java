package com.cly.project.controller;

import com.cly.project.annotation.OperationLog;
import com.cly.project.common.Result;
import com.cly.project.dto.UpdatePasswordDTO;
import com.cly.project.entity.Project;
import com.cly.project.entity.User;
import com.cly.project.service.DashboardService;
import com.cly.project.service.ProfileService;
import com.cly.project.service.ProjectService;
import com.cly.project.vo.StatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@Tag(name = "个人中心", description = "个人信息管理接口")
public class ProfileController {

    private final ProfileService profileService;
    private final ProjectService projectService;
    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "获取个人信息")
    @OperationLog(module = "个人中心", operation = "获取个人信息", businessType = "QUERY")
    public Result<User> getProfile() {
        return Result.success(profileService.getProfile());
    }

    @PutMapping
    @Operation(summary = "更新个人信息")
    @OperationLog(module = "个人中心", operation = "更新个人信息", businessType = "UPDATE")
    public Result<Void> updateProfile(@RequestBody User user) {
        profileService.updateProfile(user);
        return Result.success();
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    @OperationLog(module = "个人中心", operation = "修改密码", businessType = "UPDATE")
    public Result<Void> updatePassword(@RequestBody @Valid UpdatePasswordDTO dto) {
        profileService.updatePassword(dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }

    @GetMapping("/projects")
    @Operation(summary = "获取我的项目列表")
    @OperationLog(module = "个人中心", operation = "获取我的项目列表", businessType = "QUERY")
    public Result<List<Project>> getMyProjects() {
        return Result.success(projectService.getMyProjects());
    }

    @PostMapping("/avatar")
    @Operation(summary = "更新头像")
    @OperationLog(module = "个人中心", operation = "更新头像", businessType = "UPDATE")
    public Result<Void> updateAvatar(@RequestBody Map<String, String> body) {
        String avatar = body.get("avatar");
        User user = new User();
        user.setAvatar(avatar);
        profileService.updateProfile(user);
        return Result.success();
    }

    @GetMapping("/task-statistics")
    @Operation(summary = "获取我的任务统计")
    @OperationLog(module = "个人中心", operation = "获取我的任务统计", businessType = "QUERY")
    public Result<StatisticsVO> getMyTaskStatistics() {
        return Result.success(dashboardService.getStatistics());
    }
}
