package com.cly.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cly.project.annotation.OperationLog;
import com.cly.project.common.PageQuery;
import com.cly.project.common.Result;
import com.cly.project.entity.Company;
import com.cly.project.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
@Tag(name = "公司管理", description = "公司信息管理接口")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/list")
    @Operation(summary = "分页查询公司列表")
    @OperationLog(module = "公司管理", operation = "查询公司列表", businessType = "QUERY")
    public Result<IPage<Company>> list(PageQuery query, @RequestParam(required = false) String keyword) {
        return Result.success(companyService.pageList(query, keyword));
    }

    @GetMapping("/list-all")
    @Operation(summary = "查询所有公司列表")
    @OperationLog(module = "公司管理", operation = "查询所有公司", businessType = "QUERY")
    public Result<java.util.List<Company>> listAll() {
        return Result.success(companyService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询公司详情")
    @OperationLog(module = "公司管理", operation = "查询公司详情", businessType = "QUERY", businessIdIndex = 0)
    public Result<Company> getById(@PathVariable Long id) {
        return Result.success(companyService.getCompanyById(id));
    }

    @PostMapping
    @Operation(summary = "新增公司")
    @OperationLog(module = "公司管理", operation = "新增公司", businessType = "INSERT")
    public Result<Void> save(@RequestBody Company company) {
        companyService.saveCompany(company);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改公司")
    @OperationLog(module = "公司管理", operation = "修改公司", businessType = "UPDATE")
    public Result<Void> update(@RequestBody Company company) {
        companyService.updateCompany(company);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除公司")
    @OperationLog(module = "公司管理", operation = "删除公司", businessType = "DELETE", businessIdIndex = 0)
    public Result<Void> delete(@PathVariable Long id) {
        companyService.removeCompany(id);
        return Result.success();
    }
}
