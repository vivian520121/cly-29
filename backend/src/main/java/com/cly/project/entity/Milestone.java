package com.cly.project.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.cly.project.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pm_milestone")
public class Milestone extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private String milestoneName;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualDate;

    private Integer status;

    private Integer sortOrder;
}
