package com.cly.project.vo;

import com.cly.project.entity.Task;
import com.cly.project.entity.TaskLog;
import com.cly.project.entity.TaskTag;
import com.cly.project.entity.TaskWorklog;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class TaskDetailVO extends Task {

    private List<Task> subtasks;

    private List<TaskTag> tags;

    private List<TaskWorklog> worklogs;

    private List<TaskLog> logs;
}
