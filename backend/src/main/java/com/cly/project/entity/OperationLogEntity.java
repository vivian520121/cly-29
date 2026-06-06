package com.cly.project.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class OperationLogEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String module;

    private String operation;

    private String businessType;

    private Long businessId;

    private String method;

    private String requestMethod;

    private String requestUrl;

    private String requestParam;

    private String responseResult;

    private Long userId;

    private String username;

    private String ipAddress;

    private String location;

    private String os;

    private String browser;

    private Long costTime;

    private Integer status;

    private String errorMsg;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
