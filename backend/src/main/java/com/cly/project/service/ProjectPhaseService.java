package com.cly.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cly.project.entity.ProjectPhase;
import com.cly.project.entity.Task;
import com.cly.project.exception.BusinessException;
import com.cly.project.mapper.ProjectPhaseMapper;
import com.cly.project.mapper.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectPhaseService extends ServiceImpl<ProjectPhaseMapper, ProjectPhase> {

    private final TaskMapper taskMapper;

    public List<ProjectPhase> listByProjectId(Long projectId) {
        LambdaQueryWrapper<ProjectPhase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectPhase::getProjectId, projectId);
        wrapper.orderByAsc(ProjectPhase::getSortOrder);
        return baseMapper.selectList(wrapper);
    }

    @Transactional
    public void savePhase(ProjectPhase phase) {
        baseMapper.insert(phase);
    }

    @Transactional
    public void updatePhase(ProjectPhase phase) {
        ProjectPhase existingPhase = baseMapper.selectById(phase.getId());
        if (existingPhase == null) {
            throw new BusinessException("项目阶段不存在");
        }
        baseMapper.updateById(phase);
    }

    @Transactional
    public void removePhase(Long id) {
        ProjectPhase existingPhase = baseMapper.selectById(id);
        if (existingPhase == null) {
            throw new BusinessException("项目阶段不存在");
        }

        LambdaQueryWrapper<Task> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.eq(Task::getPhaseId, id);
        Long taskCount = taskMapper.selectCount(taskWrapper);
        if (taskCount > 0) {
            throw new BusinessException("该阶段下存在任务，无法删除");
        }

        baseMapper.deleteById(id);
    }
}
