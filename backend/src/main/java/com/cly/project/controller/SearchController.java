package com.cly.project.controller;

import com.cly.project.annotation.OperationLog;
import com.cly.project.common.Result;
import com.cly.project.dto.SearchResultVO;
import com.cly.project.entity.Project;
import com.cly.project.entity.Task;
import com.cly.project.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Tag(name = "全局搜索", description = "项目和任务搜索接口")
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/global")
    @Operation(summary = "全局搜索")
    @OperationLog(module = "全局搜索", operation = "全局搜索", businessType = "QUERY")
    public Result<List<SearchResultVO<?>>> globalSearch(@RequestParam String keyword) {
        return Result.success(searchService.globalSearch(keyword));
    }

    @GetMapping("/project")
    @Operation(summary = "搜索项目")
    @OperationLog(module = "全局搜索", operation = "搜索项目", businessType = "QUERY")
    public Result<List<SearchResultVO<Project>>> searchProjects(@RequestParam String keyword) {
        return Result.success(searchService.searchProjects(keyword));
    }

    @GetMapping("/task")
    @Operation(summary = "搜索任务")
    @OperationLog(module = "全局搜索", operation = "搜索任务", businessType = "QUERY")
    public Result<List<SearchResultVO<Task>>> searchTasks(@RequestParam String keyword) {
        return Result.success(searchService.searchTasks(keyword));
    }
}
