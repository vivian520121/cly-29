package com.cly.project.enums;

import lombok.Getter;

@Getter
public enum DeptTypeEnum {

    COMPANY(1, "公司"),
    DEPARTMENT(2, "部门"),
    TEAM(3, "小组");

    private final Integer code;
    private final String desc;

    DeptTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
