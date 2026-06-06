package com.cly.project.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("任务状态枚举测试")
class TaskStatusEnumTest {

    @Test
    @DisplayName("测试所有枚举值存在")
    void testAllEnumValuesExist() {
        assertNotNull(TaskStatusEnum.TODO);
        assertNotNull(TaskStatusEnum.IN_PROGRESS);
        assertNotNull(TaskStatusEnum.REVIEW);
        assertNotNull(TaskStatusEnum.DONE);
        assertNotNull(TaskStatusEnum.CANCELLED);
    }

    @Test
    @DisplayName("测试获取枚举属性")
    void testGetEnumProperties() {
        assertEquals(1, TaskStatusEnum.TODO.getCode());
        assertEquals("待办", TaskStatusEnum.TODO.getDesc());
        assertEquals("#f5222d", TaskStatusEnum.TODO.getColor());

        assertEquals(2, TaskStatusEnum.IN_PROGRESS.getCode());
        assertEquals("进行中", TaskStatusEnum.IN_PROGRESS.getDesc());
        assertEquals("#1890ff", TaskStatusEnum.IN_PROGRESS.getColor());

        assertEquals(3, TaskStatusEnum.REVIEW.getCode());
        assertEquals("审核中", TaskStatusEnum.REVIEW.getDesc());
        assertEquals("#fa8c16", TaskStatusEnum.REVIEW.getColor());

        assertEquals(4, TaskStatusEnum.DONE.getCode());
        assertEquals("已完成", TaskStatusEnum.DONE.getDesc());
        assertEquals("#52c41a", TaskStatusEnum.DONE.getColor());

        assertEquals(5, TaskStatusEnum.CANCELLED.getCode());
        assertEquals("已取消", TaskStatusEnum.CANCELLED.getDesc());
        assertEquals("#8c8c8c", TaskStatusEnum.CANCELLED.getColor());
    }

    @Test
    @DisplayName("测试根据Code获取颜色")
    void testGetColorByCode() {
        assertEquals("#f5222d", TaskStatusEnum.getColorByCode(1));
        assertEquals("#1890ff", TaskStatusEnum.getColorByCode(2));
        assertEquals("#fa8c16", TaskStatusEnum.getColorByCode(3));
        assertEquals("#52c41a", TaskStatusEnum.getColorByCode(4));
        assertEquals("#8c8c8c", TaskStatusEnum.getColorByCode(5));
    }

    @Test
    @DisplayName("测试根据Code获取描述")
    void testGetDescByCode() {
        assertEquals("待办", TaskStatusEnum.getDescByCode(1));
        assertEquals("进行中", TaskStatusEnum.getDescByCode(2));
        assertEquals("审核中", TaskStatusEnum.getDescByCode(3));
        assertEquals("已完成", TaskStatusEnum.getDescByCode(4));
        assertEquals("已取消", TaskStatusEnum.getDescByCode(5));
    }

    @Test
    @DisplayName("测试不存在的Code返回默认值")
    void testInvalidCodeReturnsDefault() {
        assertEquals("#1890ff", TaskStatusEnum.getColorByCode(999));
        assertEquals("未知", TaskStatusEnum.getDescByCode(999));
        assertEquals("#1890ff", TaskStatusEnum.getColorByCode(null));
        assertEquals("未知", TaskStatusEnum.getDescByCode(null));
    }

    @Test
    @DisplayName("测试枚举顺序")
    void testEnumOrder() {
        TaskStatusEnum[] values = TaskStatusEnum.values();
        assertEquals(5, values.length);
        assertEquals(TaskStatusEnum.TODO, values[0]);
        assertEquals(TaskStatusEnum.IN_PROGRESS, values[1]);
        assertEquals(TaskStatusEnum.REVIEW, values[2]);
        assertEquals(TaskStatusEnum.DONE, values[3]);
        assertEquals(TaskStatusEnum.CANCELLED, values[4]);
    }

    @Test
    @DisplayName("测试valueOf方法")
    void testValueOf() {
        assertEquals(TaskStatusEnum.TODO, TaskStatusEnum.valueOf("TODO"));
        assertEquals(TaskStatusEnum.IN_PROGRESS, TaskStatusEnum.valueOf("IN_PROGRESS"));
        assertEquals(TaskStatusEnum.REVIEW, TaskStatusEnum.valueOf("REVIEW"));
        assertEquals(TaskStatusEnum.DONE, TaskStatusEnum.valueOf("DONE"));
        assertEquals(TaskStatusEnum.CANCELLED, TaskStatusEnum.valueOf("CANCELLED"));
    }

    @Test
    @DisplayName("测试valueOf抛出异常")
    void testValueOfThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            TaskStatusEnum.valueOf("INVALID");
        });
    }
}
