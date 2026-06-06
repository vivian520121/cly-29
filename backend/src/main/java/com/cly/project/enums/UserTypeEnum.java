package com.cly.project.enums;

import lombok.Getter;

@Getter
public enum UserTypeEnum {

    ADMIN(1, "管理员"),
    USER(2, "普通用户");

    private final Integer code;
    private final String desc;

    UserTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
