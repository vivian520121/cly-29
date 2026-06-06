package com.cly.project.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pm_task_tag_rel")
public class TaskTagRel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long tagId;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private String tagName;

    @TableField(exist = false)
    private String tagColor;
}
