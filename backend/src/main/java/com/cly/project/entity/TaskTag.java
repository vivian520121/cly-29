package com.cly.project.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.cly.project.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_task_tag")
public class TaskTag extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String tagName;

    private String tagColor;
}
