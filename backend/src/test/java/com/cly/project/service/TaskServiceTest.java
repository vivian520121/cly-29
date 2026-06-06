package com.cly.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cly.project.dto.TaskQueryDTO;
import com.cly.project.dto.TaskSaveDTO;
import com.cly.project.dto.TaskStatusUpdateDTO;
import com.cly.project.entity.*;
import com.cly.project.enums.TaskPriorityEnum;
import com.cly.project.enums.TaskStatusEnum;
import com.cly.project.exception.BusinessException;
import com.cly.project.mapper.*;
import com.cly.project.vo.TaskDetailVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("任务服务单元测试")
class TaskServiceTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private TaskTagMapper taskTagMapper;

    @Mock
    private TaskTagRelMapper taskTagRelMapper;

    @Mock
    private TaskWorklogMapper taskWorklogMapper;

    @Mock
    private TaskLogService taskLogService;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;
    private TaskSaveDTO taskSaveDTO;
    private User testUser;
    private Project testProject;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setRealName("张三");
        testUser.setAvatar("avatar.jpg");

        testProject = new Project();
        testProject.setId(1L);
        testProject.setProjectName("测试项目");

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
        testTask.setCreatorId(1L);
        testTask.setStartDate(LocalDate.now());
        testTask.setEndDate(LocalDate.now().plusDays(7));
        testTask.setEstimateHours(new BigDecimal("40"));
        testTask.setProgress(0);
        testTask.setSortOrder(1);
        testTask.setActualHours(BigDecimal.ZERO);

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
    void testGetDetailById() {
        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(userMapper.selectBatchIds(anyList())).thenReturn(Arrays.asList(testUser));
        when(projectMapper.selectBatchIds(anyList())).thenReturn(Arrays.asList(testProject));
        when(taskTagRelMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(taskWorklogMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(taskLogService.listByTaskId(1L)).thenReturn(Collections.emptyList());

        TaskDetailVO result = taskService.getDetailById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试任务", result.getTaskName());
        verify(taskMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("根据ID获取任务详情 - 任务不存在")
    void testGetDetailByIdNotFound() {
        when(taskMapper.selectById(999L)).thenReturn(null);

        TaskDetailVO result = taskService.getDetailById(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("创建任务")
    void testSaveTask() {
        try (MockedStatic<com.cly.project.util.UserContext> mocked = mockStatic(com.cly.project.util.UserContext.class)) {
            mocked.when(com.cly.project.util.UserContext::getUserId).thenReturn(1L);

            when(taskMapper.insert(any(Task.class))).thenAnswer(invocation -> {
                Task task = invocation.getArgument(0);
                task.setId(2L);
                return 1;
            });

            assertDoesNotThrow(() -> taskService.saveTask(taskSaveDTO));

            verify(taskMapper, times(1)).insert(any(Task.class));
            verify(taskLogService, times(1)).logTaskChange(
                eq(2L),
                eq(1L),
                anyString(),
                isNull(),
                isNull(),
                isNull(),
                eq("创建任务")
            );
        }
    }

    @Test
    @DisplayName("创建任务 - 带标签")
    void testSaveTaskWithTags() {
        try (MockedStatic<com.cly.project.util.UserContext> mocked = mockStatic(com.cly.project.util.UserContext.class)) {
            mocked.when(com.cly.project.util.UserContext::getUserId).thenReturn(1L);

            taskSaveDTO.setTagIds(Arrays.asList(1L, 2L));

            when(taskMapper.insert(any(Task.class))).thenAnswer(invocation -> {
                Task task = invocation.getArgument(0);
                task.setId(2L);
                return 1;
            });
            when(taskTagRelMapper.insert(any(TaskTagRel.class))).thenReturn(1);

            assertDoesNotThrow(() -> taskService.saveTask(taskSaveDTO));

            verify(taskMapper, times(1)).insert(any(Task.class));
            verify(taskTagRelMapper, times(2)).insert(any(TaskTagRel.class));
        }
    }

    @Test
    @DisplayName("更新任务")
    void testUpdateTask() {
        try (MockedStatic<com.cly.project.util.UserContext> mocked = mockStatic(com.cly.project.util.UserContext.class)) {
            mocked.when(com.cly.project.util.UserContext::getUserId).thenReturn(1L);

            taskSaveDTO.setId(1L);
            taskSaveDTO.setTaskName("更新后的任务名称");

            when(taskMapper.selectById(1L)).thenReturn(testTask);
            when(taskMapper.update(any(), any(LambdaQueryWrapper.class))).thenReturn(1);
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(taskTagRelMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            assertDoesNotThrow(() -> taskService.updateTask(taskSaveDTO));

            verify(taskMapper, times(1)).update(any(), any(LambdaQueryWrapper.class));
        }
    }

    @Test
    @DisplayName("更新任务 - 任务不存在")
    void testUpdateTaskNotFound() {
        taskSaveDTO.setId(999L);
        when(taskMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> taskService.updateTask(taskSaveDTO));
    }

    @Test
    @DisplayName("删除任务")
    void testRemoveTask() {
        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(taskMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(taskTagRelMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);
        when(taskWorklogMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);
        when(taskMapper.deleteById(1L)).thenReturn(1);

        assertDoesNotThrow(() -> taskService.removeTask(1L));

        verify(taskMapper, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("删除任务 - 任务不存在")
    void testRemoveTaskNotFound() {
        when(taskMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> taskService.removeTask(999L));
    }

    @Test
    @DisplayName("删除任务 - 存在子任务")
    void testRemoveTaskWithSubtasks() {
        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(taskMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        assertThrows(BusinessException.class, () -> taskService.removeTask(1L));
    }

    @Test
    @DisplayName("分页查询任务列表")
    void testPageQuery() {
        TaskQueryDTO queryDTO = new TaskQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);
        queryDTO.setProjectId(1L);

        Page<Task> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testTask));
        page.setTotal(1L);

        when(taskMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(userMapper.selectBatchIds(anyList())).thenReturn(Arrays.asList(testUser));
        when(projectMapper.selectBatchIds(anyList())).thenReturn(Arrays.asList(testProject));

        IPage<Task> result = taskService.page(queryDTO);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals(1L, result.getTotal());
        verify(taskMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("更新任务状态")
    void testUpdateStatus() {
        try (MockedStatic<com.cly.project.util.UserContext> mocked = mockStatic(com.cly.project.util.UserContext.class)) {
            mocked.when(com.cly.project.util.UserContext::getUserId).thenReturn(1L);

            TaskStatusUpdateDTO statusDTO = new TaskStatusUpdateDTO();
            statusDTO.setTaskId(1L);
            statusDTO.setStatus(TaskStatusEnum.IN_PROGRESS.getCode());
            statusDTO.setRemark("开始处理");

            when(taskMapper.selectById(1L)).thenReturn(testTask);
            when(taskMapper.update(any(), any(LambdaQueryWrapper.class))).thenReturn(1);

            assertDoesNotThrow(() -> taskService.updateStatus(statusDTO));

            verify(taskMapper, times(1)).update(any(), any(LambdaQueryWrapper.class));
            verify(taskLogService, times(1)).logTaskChange(
                eq(1L),
                eq(1L),
                anyString(),
                eq("status"),
                anyString(),
                anyString(),
                eq("开始处理")
            );
        }
    }

    @Test
    @DisplayName("更新任务状态 - 状态未变化")
    void testUpdateStatusNoChange() {
        TaskStatusUpdateDTO statusDTO = new TaskStatusUpdateDTO();
        statusDTO.setTaskId(1L);
        statusDTO.setStatus(TaskStatusEnum.TODO.getCode());

        when(taskMapper.selectById(1L)).thenReturn(testTask);

        assertDoesNotThrow(() -> taskService.updateStatus(statusDTO));

        verify(taskMapper, never()).update(any(), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("更新任务排序")
    void testUpdateTaskOrder() {
        when(taskMapper.selectById(1L)).thenReturn(testTask);
        when(taskMapper.update(any(), any(LambdaQueryWrapper.class))).thenReturn(1);

        assertDoesNotThrow(() -> taskService.updateTaskOrder(1L, 5));

        verify(taskMapper, times(1)).update(any(), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取任务树")
    void testGetTree() {
        Task subtask = new Task();
        subtask.setId(2L);
        subtask.setParentId(1L);
        subtask.setTaskName("子任务");
        subtask.setProjectId(1L);
        subtask.setStatus(TaskStatusEnum.TODO.getCode());
        subtask.setPriority(TaskPriorityEnum.MEDIUM.getCode());

        when(taskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(testTask, subtask));
        when(userMapper.selectBatchIds(anyList())).thenReturn(Arrays.asList(testUser));
        when(projectMapper.selectBatchIds(anyList())).thenReturn(Arrays.asList(testProject));

        List<Task> result = taskService.getTree(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("获取我的任务")
    void testGetMyTasks() {
        try (MockedStatic<com.cly.project.util.UserContext> mocked = mockStatic(com.cly.project.util.UserContext.class)) {
            mocked.when(com.cly.project.util.UserContext::getUserId).thenReturn(1L);

            when(taskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(testTask));
            when(userMapper.selectBatchIds(anyList())).thenReturn(Arrays.asList(testUser));
            when(projectMapper.selectBatchIds(anyList())).thenReturn(Arrays.asList(testProject));

            List<Task> result = taskService.getMyTasks();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("测试任务", result.get(0).getTaskName());
        }
    }

    @Test
    @DisplayName("获取看板数据")
    void testGetKanbanData() {
        TaskQueryDTO queryDTO = new TaskQueryDTO();
        queryDTO.setProjectId(1L);

        when(taskMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(testTask));
        when(userMapper.selectBatchIds(anyList())).thenReturn(Arrays.asList(testUser));
        when(projectMapper.selectBatchIds(anyList())).thenReturn(Arrays.asList(testProject));

        var result = taskService.getKanbanData(queryDTO);

        assertNotNull(result);
        assertEquals(TaskStatusEnum.values().length, result.size());
    }

    @Test
    @DisplayName("更新任务状态为完成")
    void testUpdateStatusToDone() {
        try (MockedStatic<com.cly.project.util.UserContext> mocked = mockStatic(com.cly.project.util.UserContext.class)) {
            mocked.when(com.cly.project.util.UserContext::getUserId).thenReturn(1L);

            TaskStatusUpdateDTO statusDTO = new TaskStatusUpdateDTO();
            statusDTO.setTaskId(1L);
            statusDTO.setStatus(TaskStatusEnum.DONE.getCode());

            when(taskMapper.selectById(1L)).thenReturn(testTask);
            when(taskMapper.update(any(), any(LambdaQueryWrapper.class))).thenReturn(1);

            assertDoesNotThrow(() -> taskService.updateStatus(statusDTO));

            verify(taskMapper, times(1)).update(any(), any(LambdaQueryWrapper.class));
        }
    }
}
