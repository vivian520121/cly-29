package com.cly.project.enums;

import lombok.Getter;

@Getter
public enum TaskPriorityEnum {

    URGENT(1, "紧急", "#f5222d"),
    HIGH(2, "高", "#fa8c16"),
    MEDIUM(3, "中", "#1890ff"),
    LOW(4, "低", "#52c41a");

    private final Integer code;
    private final String desc;
    private final String color;

    TaskPriorityEnum(Integer code, String desc, String color) {
        this.code = code;
        this.desc = desc;
        this.color = color;
    }

    public static String getColorByCode(Integer code) {
        for (TaskPriorityEnum priority : values()) {
            if (priority.getCode().equals(code)) {
                return priority.getColor();
            }
        }
        return "#1890ff";
    }
}
