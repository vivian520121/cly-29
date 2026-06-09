package com.cly.project.controller;

import com.cly.project.annotation.OperationLog;
import com.cly.project.common.Result;
import com.cly.project.dto.DeptSaveDTO;
import com.cly.project.entity.Department;
import com.cly.project.entity.User;
import com.cly.project.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dept")
@RequiredArgsConstructor
@Tag(name = "部门管理", description = "部门信息管理接口")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/tree")
    @Operation(summary = "查询部门树形结构")
    @OperationLog(module = "部门管理", operation = "查询部门树形结构", businessType = "QUERY")
    public Result<List<Department>> getTree() {
        return Result.success(departmentService.getTree());
    }

    @GetMapping("/list")
    @Operation(summary = "查询部门列表")
    @OperationLog(module = "部门管理", operation = "查询部门列表", businessType = "QUERY")
    public Result<List<Department>> list() {
        return Result.success(departmentService.listAll());
    }

    @GetMapping("/parent/{parentId}")
    @Operation(summary = "根据父ID查询子部门")
    @OperationLog(module = "部门管理", operation = "查询子部门", businessType = "QUERY", businessIdIndex = 0)
    public Result<List<Department>> getByParentId(@PathVariable Long parentId) {
        return Result.success(departmentService.getByParentId(parentId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询部门详情")
    @OperationLog(module = "部门管理", operation = "查询部门详情", businessType = "QUERY", businessIdIndex = 0)
    public Result<Department> getById(@PathVariable Long id) {
        return Result.success(departmentService.getDeptById(id));
    }

    @PostMapping
    @Operation(summary = "新增部门")
    @OperationLog(module = "部门管理", operation = "新增部门", businessType = "INSERT")
    public Result<Void> save(@RequestBody @Valid DeptSaveDTO dto) {
        departmentService.saveDept(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改部门")
    @OperationLog(module = "部门管理", operation = "修改部门", businessType = "UPDATE")
    public Result<Void> update(@RequestBody @Valid DeptSaveDTO dto) {
        departmentService.updateDept(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门")
    @OperationLog(module = "部门管理", operation = "删除部门", businessType = "DELETE", businessIdIndex = 0)
    public Result<Void> delete(@PathVariable Long id) {
        departmentService.removeDept(id);
        return Result.success();
    }

    @GetMapping("/{id}/users")
    @Operation(summary = "查询部门下的用户")
    @OperationLog(module = "部门管理", operation = "查询部门用户", businessType = "QUERY", businessIdIndex = 0)
    public Result<List<User>> getDeptUsers(@PathVariable Long id) {
        return Result.success(departmentService.getDeptUsers(id));
    }
}
