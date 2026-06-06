package com.cly.project.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.cly.project.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class Department extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long companyId;

    private Long parentId;

    private String deptName;

    private String deptCode;

    private Integer deptType;

    private Long leaderId;

    private String phone;

    private String email;

    private String description;

    private Integer status;

    private Integer sortOrder;

    private String treePath;

    @TableField(exist = false)
    private java.util.List<Department> children;
}
