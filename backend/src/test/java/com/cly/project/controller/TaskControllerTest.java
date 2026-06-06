package com.cly.project.controller;

import com.cly.project.common.Result;
import com.cly.project.dto.TaskQueryDTO;
import com.cly.project.dto.TaskSaveDTO;
import com.cly.project.dto.TaskStatusUpdateDTO;
import com.cly.project.entity.Task;
import com.cly.project.enums.TaskPriorityEnum;
import com.cly.project.enums.TaskStatusEnum;
import com.cly.project.service.TaskService;
import com.cly.project.vo.KanbanColumnVO;
import com.cly.project.vo.TaskDetailVO;
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
    private TaskDetailVO testTaskDetail;

    @BeforeEach
    void setUp() {
        testTask = new Task();
        testTask.setId(1L);
        testTask.setProjectId(1L);
        testTask.setTaskName("测试任务");
        testTask.setTaskNo("TASK_000001");
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

        testTaskDetail = new TaskDetailVO();
        testTaskDetail.setId(1L);
        testTaskDetail.setProjectId(1L);
        testTaskDetail.setTaskName("测试任务");
        testTaskDetail.setTaskNo("TASK_000001");
        testTaskDetail.setDescription("这是一个测试任务");
        testTaskDetail.setStatus(TaskStatusEnum.TODO.getCode());

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
        when(taskService.getDetailById(1L)).thenReturn(testTaskDetail);

        mockMvc.perform(MockMvcRequestBuilders.get("/task/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.taskName").value("测试任务"))
                .andExpect(jsonPath("$.data.status").value(TaskStatusEnum.TODO.getCode()));

        verify(taskService, times(1)).getDetailById(1L);
    }

    @Test
    @DisplayName("创建任务")
    void testCreateTask() throws Exception {
        doNothing().when(taskService).saveTask(any(TaskSaveDTO.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskSaveDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(taskService, times(1)).saveTask(any(TaskSaveDTO.class));
    }

    @Test
    @DisplayName("更新任务")
    void testUpdateTask() throws Exception {
        taskSaveDTO.setId(1L);
        taskSaveDTO.setTaskName("更新后的任务");
        doNothing().when(taskService).updateTask(any(TaskSaveDTO.class));

        mockMvc.perform(MockMvcRequestBuilders.put("/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskSaveDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(taskService, times(1)).updateTask(any(TaskSaveDTO.class));
    }

    @Test
    @DisplayName("删除任务")
    void testDeleteTask() throws Exception {
        doNothing().when(taskService).removeTask(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/task/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(taskService, times(1)).removeTask(1L);
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
        TaskStatusUpdateDTO statusDTO = new TaskStatusUpdateDTO();
        statusDTO.setTaskId(1L);
        statusDTO.setStatus(TaskStatusEnum.IN_PROGRESS.getCode());
        statusDTO.setRemark("开始处理");

        doNothing().when(taskService).updateStatus(any(TaskStatusUpdateDTO.class));

        mockMvc.perform(MockMvcRequestBuilders.put("/task/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(taskService, times(1)).updateStatus(any(TaskStatusUpdateDTO.class));
    }

    @Test
    @DisplayName("更新任务排序")
    void testUpdateTaskOrder() throws Exception {
        doNothing().when(taskService).updateTaskOrder(1L, 5);

        mockMvc.perform(MockMvcRequestBuilders.put("/task/order")
                .param("taskId", "1")
                .param("sortOrder", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(taskService, times(1)).updateTaskOrder(1L, 5);
    }

    @Test
    @DisplayName("获取看板数据")
    void testGetKanbanData() throws Exception {
        KanbanColumnVO columnVO = new KanbanColumnVO();
        columnVO.setStatus(TaskStatusEnum.TODO.getCode());
        columnVO.setStatusName("待办");
        columnVO.setStatusColor("blue");
        columnVO.setTasks(Arrays.asList(testTask));
        columnVO.setTotal(1L);

        when(taskService.getKanbanData(any(TaskQueryDTO.class))).thenReturn(Arrays.asList(columnVO));

        mockMvc.perform(MockMvcRequestBuilders.get("/task/kanban")
                .param("projectId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].status").value(TaskStatusEnum.TODO.getCode()));

        verify(taskService, times(1)).getKanbanData(any(TaskQueryDTO.class));
    }

    @Test
    @DisplayName("获取任务树")
    void testGetTaskTree() throws Exception {
        when(taskService.getTree(1L)).thenReturn(Arrays.asList(testTask));

        mockMvc.perform(MockMvcRequestBuilders.get("/task/tree/{projectId}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].taskName").value("测试任务"));

        verify(taskService, times(1)).getTree(1L);
    }

    @Test
    @DisplayName("获取我的任务")
    void testGetMyTasks() throws Exception {
        when(taskService.getMyTasks()).thenReturn(Arrays.asList(testTask));

        mockMvc.perform(MockMvcRequestBuilders.get("/task/my")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].taskName").value("测试任务"));

        verify(taskService, times(1)).getMyTasks();
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
