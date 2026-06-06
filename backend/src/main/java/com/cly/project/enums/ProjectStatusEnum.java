package com.cly.project.enums;

import lombok.Getter;

@Getter
public enum ProjectStatusEnum {

    NOT_STARTED(1, "未开始"),
    IN_PROGRESS(2, "进行中"),
    PAUSED(3, "已暂停"),
    COMPLETED(4, "已完成"),
    CANCELLED(5, "已取消");

    private final Integer code;
    private final String desc;

    ProjectStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
