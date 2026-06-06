package com.cly.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class WorklogSaveDTO {

    private Long id;

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @NotNull(message = "工作日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;

    @NotNull(message = "工时不能为空")
    @DecimalMin(value = "0.1", message = "工时最小为0.1小时")
    private BigDecimal hours;

    private String description;
}
