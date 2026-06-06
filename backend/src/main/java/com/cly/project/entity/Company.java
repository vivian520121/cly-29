package com.cly.project.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.cly.project.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_company")
public class Company extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String companyName;

    private String companyCode;

    private String legalPerson;

    private String contactPhone;

    private String address;

    private String logoUrl;

    private String description;

    private Integer status;

    private Integer sortOrder;
}
