package com.cly.project.vo;

import com.cly.project.entity.TaskWorklog;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class WorklogVO extends TaskWorklog {

    private String taskNo;

    private String projectName;
}
