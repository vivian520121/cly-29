package com.cly.project.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskStatusUpdateDTO {

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private String remark;
}
