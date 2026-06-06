package com.cly.project.vo;

import com.cly.project.entity.Task;
import lombok.Data;

import java.util.List;

@Data
public class KanbanColumnVO {

    private Integer status;

    private String statusName;

    private String statusColor;

    private List<Task> tasks;

    private Long total;
}
