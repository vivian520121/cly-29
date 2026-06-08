package com.cly.project.controller;

import com.cly.project.annotation.OperationLog;
import com.cly.project.common.Result;
import com.cly.project.dto.LoginDTO;
import com.cly.project.dto.LoginVO;
import com.cly.project.entity.User;
import com.cly.project.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    @OperationLog(module = "认证管理", operation = "用户登录", businessType = "LOGIN")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success(loginVO);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    @OperationLog(module = "认证管理", operation = "用户登出", businessType = "LOGOUT")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    @GetMapping("/user-info")
    @Operation(summary = "获取当前用户信息")
    @OperationLog(module = "认证管理", operation = "获取当前用户信息", businessType = "QUERY")
    public Result<User> getCurrentUser() {
        User user = authService.getCurrentUser();
        return Result.success(user);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "刷新token")
    @OperationLog(module = "认证管理", operation = "刷新token", businessType = "UPDATE")
    public Result<Map<String, String>> refreshToken() {
        String newToken = authService.refreshToken();
        Map<String, String> data = new HashMap<>();
        data.put("token", newToken);
        return Result.success(data);
    }
}
