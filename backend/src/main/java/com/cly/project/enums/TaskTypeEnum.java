package com.cly.project.enums;

import lombok.Getter;

@Getter
public enum TaskTypeEnum {

    REQUIREMENT(1, "需求", "#1890ff"),
    BUG(2, "缺陷", "#f5222d"),
    OPTIMIZATION(3, "优化", "#52c41a"),
    OTHER(4, "其他", "#8c8c8c");

    private final Integer code;
    private final String desc;
    private final String color;

    TaskTypeEnum(Integer code, String desc, String color) {
        this.code = code;
        this.desc = desc;
        this.color = color;
    }
}
