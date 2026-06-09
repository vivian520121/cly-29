package com.cly.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cly.project.annotation.OperationLog;
import com.cly.project.common.Result;
import com.cly.project.dto.TaskQueryDTO;
import com.cly.project.dto.TaskSaveDTO;
import com.cly.project.dto.TaskStatusUpdateDTO;
import com.cly.project.entity.Task;
import com.cly.project.service.TaskService;
import com.cly.project.vo.KanbanColumnVO;
import com.cly.project.vo.TaskDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
@Tag(name = "任务管理", description = "任务管理接口")
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/page")
    @Operation(summary = "分页查询任务列表")
    @OperationLog(module = "任务管理", operation = "查询任务列表", businessType = "QUERY")
    public Result<IPage<Task>> page(TaskQueryDTO query) {
        return Result.success(taskService.page(query));
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询任务列表")
    @OperationLog(module = "任务管理", operation = "查询任务列表", businessType = "QUERY")
    public Result<IPage<Task>> list(TaskQueryDTO query) {
        return Result.success(taskService.page(query));
    }

    @GetMapping("/kanban")
    @Operation(summary = "获取看板数据")
    @OperationLog(module = "任务管理", operation = "获取看板数据", businessType = "QUERY")
    public Result<List<KanbanColumnVO>> getKanbanData(TaskQueryDTO query) {
        return Result.success(taskService.getKanbanData(query));
    }

    @GetMapping("/tree/{projectId}")
    @Operation(summary = "获取任务树")
    @OperationLog(module = "任务管理", operation = "获取任务树", businessType = "QUERY", businessIdIndex = 0)
    public Result<List<Task>> getTree(@PathVariable Long projectId) {
        return Result.success(taskService.getTree(projectId));
    }

    @GetMapping("/my")
    @Operation(summary = "获取我的任务")
    @OperationLog(module = "任务管理", operation = "获取我的任务", businessType = "QUERY")
    public Result<List<Task>> getMyTasks() {
        return Result.success(taskService.getMyTasks());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询任务详情")
    @OperationLog(module = "任务管理", operation = "查询任务详情", businessType = "QUERY", businessIdIndex = 0)
    public Result<TaskDetailVO> getById(@PathVariable Long id) {
        return Result.success(taskService.getDetailById(id));
    }

    @PostMapping
    @Operation(summary = "新增任务")
    @OperationLog(module = "任务管理", operation = "新增任务", businessType = "INSERT")
    public Result<Void> save(@RequestBody @Valid TaskSaveDTO dto) {
        taskService.saveTask(dto);
        return Result.success();
    }

    @PutMapping
    @Operation(summary = "修改任务")
    @OperationLog(module = "任务管理", operation = "修改任务", businessType = "UPDATE")
    public Result<Void> update(@RequestBody @Valid TaskSaveDTO dto) {
        taskService.updateTask(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除任务")
    @OperationLog(module = "任务管理", operation = "删除任务", businessType = "DELETE", businessIdIndex = 0)
    public Result<Void> delete(@PathVariable Long id) {
        taskService.removeTask(id);
        return Result.success();
    }

    @PutMapping("/status")
    @Operation(summary = "更新任务状态")
    @OperationLog(module = "任务管理", operation = "更新任务状态", businessType = "UPDATE")
    public Result<Void> updateStatus(@RequestBody @Valid TaskStatusUpdateDTO dto) {
        taskService.updateStatus(dto);
        return Result.success();
    }

    @PutMapping("/order")
    @Operation(summary = "更新任务排序")
    @OperationLog(module = "任务管理", operation = "更新任务排序", businessType = "UPDATE")
    public Result<Void> updateTaskOrder(@RequestParam Long taskId, @RequestParam Integer sortOrder) {
        taskService.updateTaskOrder(taskId, sortOrder);
        return Result.success();
    }
}
