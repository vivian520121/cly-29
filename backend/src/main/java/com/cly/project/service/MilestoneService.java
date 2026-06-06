package com.cly.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cly.project.entity.Milestone;
import com.cly.project.exception.BusinessException;
import com.cly.project.mapper.MilestoneMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MilestoneService extends ServiceImpl<MilestoneMapper, Milestone> {

    public List<Milestone> listByProjectId(Long projectId) {
        LambdaQueryWrapper<Milestone> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Milestone::getProjectId, projectId);
        wrapper.orderByAsc(Milestone::getSortOrder);
        return baseMapper.selectList(wrapper);
    }

    @Transactional
    public void save(Milestone milestone) {
        baseMapper.insert(milestone);
    }

    @Transactional
    public void update(Milestone milestone) {
        Milestone existingMilestone = baseMapper.selectById(milestone.getId());
        if (existingMilestone == null) {
            throw new BusinessException("里程碑不存在");
        }
        baseMapper.updateById(milestone);
    }

    @Transactional
    public void remove(Long id) {
        Milestone existingMilestone = baseMapper.selectById(id);
        if (existingMilestone == null) {
            throw new BusinessException("里程碑不存在");
        }
        baseMapper.deleteById(id);
    }
}
