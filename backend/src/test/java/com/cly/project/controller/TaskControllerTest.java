package com.cly.project.controller;

import com.cly.project.common.Result;
import com.cly.project.dto.TaskQueryDTO;
import com.cly.project.dto.TaskSaveDTO;
import com.cly.project.entity.Task;
import com.cly.project.enums.TaskPriorityEnum;
import com.cly.project.enums.TaskStatusEnum;
import com.cly.project.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@DisplayName("任务控制器单元测试")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    private Task testTask;
    private TaskSaveDTO taskSaveDTO;

    @BeforeEach
    void setUp() {
        testTask = new Task();
        testTask.setId(1L);
        testTask.setProjectId(1L);
        testTask.setTaskName("测试任务");
        testTask.setTaskNo("TASK-001");
        testTask.setDescription("这是一个测试任务");
        testTask.setTaskType(2);
        testTask.setStatus(TaskStatusEnum.TODO.getCode());
        testTask.setPriority(TaskPriorityEnum.MEDIUM.getCode());
        testTask.setAssigneeId(1L);
        testTask.setAssigneeName("张三");
        testTask.setStartDate(LocalDate.now());
        testTask.setEndDate(LocalDate.now().plusDays(7));
        testTask.setEstimateHours(new BigDecimal("40"));
        testTask.setProgress(0);

        taskSaveDTO = new TaskSaveDTO();
        taskSaveDTO.setProjectId(1L);
        taskSaveDTO.setTaskName("新建测试任务");
        taskSaveDTO.setDescription("新建任务描述");
        taskSaveDTO.setTaskType(2);
        taskSaveDTO.setStatus(TaskStatusEnum.TODO.getCode());
        taskSaveDTO.setPriority(TaskPriorityEnum.HIGH.getCode());
        taskSaveDTO.setAssigneeId(1L);
        taskSaveDTO.setStartDate(LocalDate.now());
        taskSaveDTO.setEndDate(LocalDate.now().plusDays(5));
        taskSaveDTO.setEstimateHours(new BigDecimal("20"));
    }

    @Test
    @DisplayName("获取任务详情")
    void testGetTaskDetail() throws Exception {
        when(taskService.getDetail(1L)).thenReturn(testTask);

        mockMvc.perform(MockMvcRequestBuilders.get("/task/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.taskName").value("测试任务"))
                .andExpect(jsonPath("$.data.status").value(TaskStatusEnum.TODO.getCode()));

        verify(taskService, times(1)).getDetail(1L);
    }

    @Test
    @DisplayName("创建任务")
    void testCreateTask() throws Exception {
        when(taskService.save(any(TaskSaveDTO.class))).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskSaveDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(taskService, times(1)).save(any(TaskSaveDTO.class));
    }

    @Test
    @DisplayName("更新任务")
    void testUpdateTask() throws Exception {
        taskSaveDTO.setId(1L);
        taskSaveDTO.setTaskName("更新后的任务");
        when(taskService.update(any(TaskSaveDTO.class))).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.put("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskSaveDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(taskService, times(1)).update(any(TaskSaveDTO.class));
    }

    @Test
    @DisplayName("删除任务")
    void testDeleteTask() throws Exception {
        when(taskService.removeById(1L)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.delete("/task/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(taskService, times(1)).removeById(1L);
    }

    @Test
    @DisplayName("分页查询任务列表")
    void testPageTaskList() throws Exception {
        TaskQueryDTO queryDTO = new TaskQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);
        queryDTO.setProjectId(1L);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Task> page = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
        page.setRecords(Arrays.asList(testTask));
        page.setTotal(1L);

        when(taskService.page(any(TaskQueryDTO.class))).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/task/page")
                .param("pageNum", "1")
                .param("pageSize", "10")
                .param("projectId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].taskName").value("测试任务"));

        verify(taskService, times(1)).page(any(TaskQueryDTO.class));
    }

    @Test
    @DisplayName("更新任务状态")
    void testUpdateTaskStatus() throws Exception {
        when(taskService.updateStatus(1L, TaskStatusEnum.IN_PROGRESS.getCode())).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.put("/task/{id}/status", 1L)
                .param("status", String.valueOf(TaskStatusEnum.IN_PROGRESS.getCode()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(taskService, times(1)).updateStatus(1L, TaskStatusEnum.IN_PROGRESS.getCode());
    }

    @Test
    @DisplayName("更新任务进度")
    void testUpdateTaskProgress() throws Exception {
        when(taskService.updateProgress(1L, 50)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.put("/task/{id}/progress", 1L)
                .param("progress", "50")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(taskService, times(1)).updateProgress(1L, 50);
    }

    @Test
    @DisplayName("获取项目任务列表")
    void testGetProjectTasks() throws Exception {
        List<Task> tasks = Arrays.asList(testTask);
        when(taskService.getByProjectId(1L)).thenReturn(tasks);

        mockMvc.perform(MockMvcRequestBuilders.get("/task/list")
                .param("projectId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].taskName").value("测试任务"));

        verify(taskService, times(1)).getByProjectId(1L);
    }

    @Test
    @DisplayName("批量删除任务")
    void testBatchDeleteTasks() throws Exception {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        when(taskService.removeByIds(ids)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.delete("/task/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(taskService, times(1)).removeByIds(ids);
    }

    @Test
    @DisplayName("创建任务 - 任务名称不能为空")
    void testCreateTaskWithEmptyName() throws Exception {
        taskSaveDTO.setTaskName("");

        mockMvc.perform(MockMvcRequestBuilders.post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskSaveDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("创建任务 - 项目ID不能为空")
    void testCreateTaskWithoutProjectId() throws Exception {
        taskSaveDTO.setProjectId(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskSaveDTO)))
                .andExpect(status().isBadRequest());
    }
}
