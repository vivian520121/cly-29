package com.cly.project.common;

import lombok.Data;
import java.io.Serializable;

@Data
public class Result<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;
    private long timestamp = System.currentTimeMillis();

    public static final Integer SUCCESS = 200;
    public static final Integer ERROR = 500;
    public static final Integer UNAUTHORIZED = 401;
    public static final Integer FORBIDDEN = 403;
    public static final Integer NOT_FOUND = 404;
    public static final Integer VALIDATE_ERROR = 400;

    public Result() {}

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(SUCCESS, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS, "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(SUCCESS, message, data);
    }

    public static <T> Result<T> error() {
        return new Result<>(ERROR, "操作失败", null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(ERROR, message, null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error(String message, T data) {
        return new Result<>(ERROR, message, data);
    }
}
