package com.cly.project.enums;

import lombok.Getter;

@Getter
public enum TaskStatusEnum {

    TODO(1, "待办", "#f5222d"),
    IN_PROGRESS(2, "进行中", "#1890ff"),
    REVIEW(3, "审核中", "#fa8c16"),
    DONE(4, "已完成", "#52c41a"),
    CANCELLED(5, "已取消", "#8c8c8c");

    private final Integer code;
    private final String desc;
    private final String color;

    TaskStatusEnum(Integer code, String desc, String color) {
        this.code = code;
        this.desc = desc;
        this.color = color;
    }

    public static String getColorByCode(Integer code) {
        for (TaskStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status.getColor();
            }
        }
        return "#1890ff";
    }

    public static String getDescByCode(Integer code) {
        for (TaskStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status.getDesc();
            }
        }
        return "未知";
    }
}
