package com.cly.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cly.project.dto.SearchResultVO;
import com.cly.project.entity.Project;
import com.cly.project.entity.Task;
import com.cly.project.mapper.ProjectMapper;
import com.cly.project.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ProjectMapper projectMapper;
    private final TaskMapper taskMapper;

    public List<SearchResultVO<?>> globalSearch(String keyword) {
        List<SearchResultVO<?>> results = new ArrayList<>();

        if (!StringUtils.hasText(keyword)) {
            return results;
        }

        results.addAll(searchProjects(keyword));
        results.addAll(searchTasks(keyword));

        return results;
    }

    public List<SearchResultVO<Project>> searchProjects(String keyword) {
        List<SearchResultVO<Project>> results = new ArrayList<>();

        if (!StringUtils.hasText(keyword)) {
            return results;
        }

        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Project::getProjectName, keyword)
                .or()
                .like(Project::getProjectCode, keyword)
                .or()
                .like(Project::getDescription, keyword);
        wrapper.orderByDesc(Project::getCreateTime);

        List<Project> projects = projectMapper.selectList(wrapper);

        return projects.stream()
                .map(project -> {
                    SearchResultVO<Project> vo = new SearchResultVO<>();
                    vo.setType("project");
                    vo.setTypeName("项目");
                    vo.setTitle(project.getProjectName());
                    vo.setDescription(project.getDescription());
                    vo.setUrl("/project/" + project.getId());
                    vo.setData(project);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    public List<SearchResultVO<Task>> searchTasks(String keyword) {
        List<SearchResultVO<Task>> results = new ArrayList<>();

        if (!StringUtils.hasText(keyword)) {
            return results;
        }

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Task::getTaskName, keyword)
                .or()
                .like(Task::getTaskNo, keyword)
                .or()
                .like(Task::getDescription, keyword);
        wrapper.orderByDesc(Task::getCreateTime);

        List<Task> tasks = taskMapper.selectList(wrapper);

        return tasks.stream()
                .map(task -> {
                    SearchResultVO<Task> vo = new SearchResultVO<>();
                    vo.setType("task");
                    vo.setTypeName("任务");
                    vo.setTitle(task.getTaskName());
                    vo.setDescription(task.getDescription());
                    vo.setUrl("/task/" + task.getId());
                    vo.setData(task);
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
