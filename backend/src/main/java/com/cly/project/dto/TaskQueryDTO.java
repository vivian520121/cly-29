package com.cly.project.dto;

import com.cly.project.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class TaskQueryDTO extends PageQuery {

    private String keyword;

    private Long projectId;

    private Long phaseId;

    private Long parentId;

    private Integer taskType;

    private List<Integer> statusList;

    private List<Integer> priorityList;

    private Long assigneeId;

    private Long creatorId;

    private List<Long> tagIds;

    private String viewType = "list";
}
