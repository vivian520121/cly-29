package com.cly.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProjectSaveDTO {

    private Long id;

    @NotNull(message = "企业ID不能为空")
    private Long companyId;

    @NotBlank(message = "项目名称不能为空")
    private String projectName;

    @NotBlank(message = "项目编码不能为空")
    private String projectCode;

    private Integer projectType;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private Integer status;

    private Integer priority;

    private Long managerId;

    private BigDecimal budget;

    private String color;

    private List<Long> memberIds;
}
