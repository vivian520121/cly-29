package com.cly.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cly.project.dto.TaskQueryDTO;
import com.cly.project.dto.TaskSaveDTO;
import com.cly.project.entity.Task;
import com.cly.project.enums.TaskPriorityEnum;
import com.cly.project.enums.TaskStatusEnum;
import com.cly.project.mapper.TaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("任务服务单元测试")
class TaskServiceTest {

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
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
        testTask.setCreatorId(1L);
        testTask.setStartDate(LocalDate.now());
        testTask.setEndDate(LocalDate.now().plusDays(7));
        testTask.setEstimateHours(new BigDecimal("40"));
        testTask.setProgress(0);
        testTask.setSortOrder(1);

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
        taskSaveDTO.setProgress(0);
        taskSaveDTO.setSortOrder(1);
    }

    @Test
    @DisplayName("根据ID获取任务详情")
    void testGetTaskById() {
        when(taskMapper.selectById(1L)).thenReturn(testTask);

        Task result = taskService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试任务", result.getTaskName());
        assertEquals(TaskStatusEnum.TODO.getCode(), result.getStatus());
        verify(taskMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("创建任务")
    void testCreateTask() {
        when(taskMapper.insert(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(2L);
            return 1;
        });

        boolean result = taskService.save(taskSaveDTO);

        assertTrue(result);
        verify(taskMapper, times(1)).insert(any(Task.class));
    }

    @Test
    @DisplayName("更新任务")
    void testUpdateTask() {
        taskSaveDTO.setId(1L);
        taskSaveDTO.setTaskName("更新后的任务名称");
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        boolean result = taskService.update(taskSaveDTO);

        assertTrue(result);
        verify(taskMapper, times(1)).updateById(any(Task.class));
    }

    @Test
    @DisplayName("删除任务")
    void testDeleteTask() {
        when(taskMapper.deleteById(1L)).thenReturn(1);

        boolean result = taskService.removeById(1L);

        assertTrue(result);
        verify(taskMapper, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("分页查询任务列表")
    void testPageQuery() {
        TaskQueryDTO queryDTO = new TaskQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);
        queryDTO.setProjectId(1L);
        queryDTO.setStatus(TaskStatusEnum.TODO.getCode());

        Page<Task> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testTask));
        page.setTotal(1L);

        when(taskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        Page<Task> result = taskService.page(queryDTO);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals(1L, result.getTotal());
        verify(taskMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("更新任务状态")
    void testUpdateTaskStatus() {
        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        boolean result = taskService.updateStatus(1L, TaskStatusEnum.IN_PROGRESS.getCode());

        assertTrue(result);
        assertEquals(TaskStatusEnum.IN_PROGRESS.getCode(), testTask.getStatus());
        verify(taskMapper, times(1)).updateById(any(Task.class));
    }

    @Test
    @DisplayName("更新任务进度")
    void testUpdateTaskProgress() {
        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        boolean result = taskService.updateProgress(1L, 50);

        assertTrue(result);
        assertEquals(50, testTask.getProgress());
        verify(taskMapper, times(1)).updateById(any(Task.class));
    }

    @Test
    @DisplayName("获取项目下的任务列表")
    void testGetTasksByProjectId() {
        when(taskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(testTask));

        List<Task> result = taskService.getByProjectId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getProjectId());
        verify(taskMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取用户的任务列表")
    void testGetTasksByUserId() {
        when(taskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(testTask));

        List<Task> result = taskService.getByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(taskMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("批量删除任务")
    void testBatchDeleteTasks() {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        when(taskMapper.deleteBatchIds(ids)).thenReturn(3);

        boolean result = taskService.removeByIds(ids);

        assertTrue(result);
        verify(taskMapper, times(1)).deleteBatchIds(ids);
    }

    @Test
    @DisplayName("任务名称不能为空")
    void testTaskNameCannotBeNull() {
        taskSaveDTO.setTaskName(null);

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.save(taskSaveDTO);
        });
    }

    @Test
    @DisplayName("项目ID不能为空")
    void testProjectIdCannotBeNull() {
        taskSaveDTO.setProjectId(null);

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.save(taskSaveDTO);
        });
    }

    @Test
    @DisplayName("结束日期必须大于开始日期")
    void testEndDateMustBeAfterStartDate() {
        taskSaveDTO.setStartDate(LocalDate.now());
        taskSaveDTO.setEndDate(LocalDate.now().minusDays(1));

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.save(taskSaveDTO);
        });
    }

    @Test
    @DisplayName("进度必须在0-100之间")
    void testProgressMustBeBetweenZeroAndHundred() {
        when(taskMapper.selectById(1L)).thenReturn(testTask);

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateProgress(1L, 150);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateProgress(1L, -10);
        });
    }
}
