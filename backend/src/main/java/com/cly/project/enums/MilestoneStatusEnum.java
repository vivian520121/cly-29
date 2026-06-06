package com.cly.project.enums;

import lombok.Getter;

@Getter
public enum MilestoneStatusEnum {

    NOT_STARTED(1, "未开始"),
    IN_PROGRESS(2, "进行中"),
    COMPLETED(3, "已完成"),
    DELAYED(4, "已延期");

    private final Integer code;
    private final String desc;

    MilestoneStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
