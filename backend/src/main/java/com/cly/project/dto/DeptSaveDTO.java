package com.cly.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeptSaveDTO {

    private Long id;

    @NotNull(message = "企业ID不能为空")
    private Long companyId;

    private Long parentId;

    @NotBlank(message = "部门名称不能为空")
    private String deptName;

    @NotBlank(message = "部门编码不能为空")
    private String deptCode;

    private Integer deptType;

    private Long leaderId;

    private String phone;

    private String email;

    private String description;

    private Integer status;

    private Integer sortOrder;
}
