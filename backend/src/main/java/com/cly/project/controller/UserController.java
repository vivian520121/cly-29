package com.cly.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cly.project.annotation.OperationLog;
import com.cly.project.common.Result;
import com.cly.project.dto.ResetPasswordDTO;
import com.cly.project.dto.UserQueryDTO;
import com.cly.project.dto.UserSaveDTO;
import com.cly.project.entity.User;
import com.cly.project.service.UserService;
import com.cly.project.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户信息管理接口")
public class UserController {

    private final UserService userService;

    @GetMapping("/page")
    @Operation(summary = "分页查询用户列表")
    @OperationLog(module = "用户管理", operation = "查询用户列表", businessType = "QUERY")
    public Result<IPage<UserVO>> page(UserQueryDTO query) {
        return Result.success(userService.page(query));
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询用户列表")
    @OperationLog(module = "用户管理", operation = "查询用户列表", businessType = "QUERY")
    public Result<IPage<UserVO>> list(UserQueryDTO query) {
        return Result.success(userService.page(query));
    }

    @GetMapping("/list-all")
    @Operation(summary = "查询所有用户列表")
    @OperationLog(module = "用户管理", operation = "查询所有用户", businessType = "QUERY")
    public Result<List<User>> listAll() {
        return Result.success(userService.listAll());
    }

    @GetMapping("/dept/{deptId}")
    @Operation(summary = "根据部门ID查询用户")
    @OperationLog(module = "用户管理", operation = "根据部门查询用户", businessType = "QUERY", businessIdIndex = 0)
    public Result<List<User>> getUsersByDeptId(@PathVariable Long deptId) {
        return Result.success(userService.getUsersByDeptId(deptId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询用户详情")
    @OperationLog(module = "用户管理", operation = "查询用户详情", businessType = "QUERY", businessIdIndex = 0)
    public Result<UserVO> getById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @PostMapping
    @Operation(summary = "新增用户")
    @OperationLog(module = "用户管理", operation = "新增用户", businessType = "INSERT")
    public Result<Void> save(@RequestBody @Valid UserSaveDTO dto) {
        userService.saveUser(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改用户")
    @OperationLog(module = "用户管理", operation = "修改用户", businessType = "UPDATE")
    public Result<Void> update(@RequestBody @Valid UserSaveDTO dto) {
        userService.updateUser(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @OperationLog(module = "用户管理", operation = "删除用户", businessType = "DELETE", businessIdIndex = 0)
    public Result<Void> delete(@PathVariable Long id) {
        userService.removeUser(id);
        return Result.success();
    }

    @PutMapping("/reset-password")
    @Operation(summary = "重置用户密码")
    @OperationLog(module = "用户管理", operation = "重置密码", businessType = "UPDATE")
    public Result<Void> resetPassword(@RequestBody @Valid ResetPasswordDTO dto) {
        userService.resetPassword(dto.getId(), dto.getNewPassword());
        return Result.success();
    }
}
