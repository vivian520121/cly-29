package com.cly.project.vo;

import com.cly.project.entity.Milestone;
import com.cly.project.entity.ProjectPhase;
import com.cly.project.entity.Task;
import lombok.Data;

import java.util.List;

@Data
public class GanttDataVO {

    private List<ProjectPhase> phases;

    private List<Milestone> milestones;

    private List<Task> tasks;
}
