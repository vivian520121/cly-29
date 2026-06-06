package com.cly.project.dto;

import com.cly.project.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectQueryDTO extends PageQuery {

    private String keyword;

    private Integer status;

    private Integer priority;

    private Long managerId;

    private Long memberId;
}
