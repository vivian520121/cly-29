package com.cly.project.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pm_project_member")
public class ProjectMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long userId;

    private Integer role;

    private LocalDateTime joinTime;

    private Long createBy;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String realName;

    @TableField(exist = false)
    private String avatar;

    @TableField(exist = false)
    private String roleName;
}
