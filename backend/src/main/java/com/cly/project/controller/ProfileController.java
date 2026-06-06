package com.cly.project.controller;

import com.cly.project.annotation.OperationLog;
import com.cly.project.common.Result;
import com.cly.project.dto.UpdatePasswordDTO;
import com.cly.project.entity.User;
import com.cly.project.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@Tag(name = "个人中心", description = "个人信息管理接口")
public class ProfileController {

    private final ProfileService profileService;

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
}
