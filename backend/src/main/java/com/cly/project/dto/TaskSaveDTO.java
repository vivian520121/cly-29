package com.cly.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class TaskSaveDTO {

    private Long id;

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    private Long phaseId;

    private Long parentId;

    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    private String description;

    private Integer taskType;

    private Integer status;

    private Integer priority;

    private Long assigneeId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private BigDecimal estimateHours;

    private Integer progress;

    private Integer sortOrder;

    private List<Long> tagIds;
}
