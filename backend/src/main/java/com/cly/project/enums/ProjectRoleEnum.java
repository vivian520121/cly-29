package com.cly.project.enums;

import lombok.Getter;

@Getter
public enum ProjectRoleEnum {

    ADMIN(1, "管理员"),
    MANAGER(2, "项目经理"),
    MEMBER(3, "成员"),
    VIEWER(4, "查看者");

    private final Integer code;
    private final String desc;

    ProjectRoleEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
