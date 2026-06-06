package com.cly.project.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user_dept")
public class UserDept {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long deptId;

    private Integer isMain;

    private String position;

    private LocalDateTime createTime;
}
