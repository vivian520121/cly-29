package com.cly.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cly.project.entity.TaskTag;
import com.cly.project.entity.TaskTagRel;
import com.cly.project.exception.BusinessException;
import com.cly.project.mapper.TaskTagMapper;
import com.cly.project.mapper.TaskTagRelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskTagService extends ServiceImpl<TaskTagMapper, TaskTag> {

    private final TaskTagRelMapper taskTagRelMapper;

    public List<TaskTag> listByProjectId(Long projectId) {
        LambdaQueryWrapper<TaskTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskTag::getProjectId, projectId);
        wrapper.orderByAsc(TaskTag::getCreateTime);
        return baseMapper.selectList(wrapper);
    }

    @Transactional
    public void saveTag(TaskTag tag) {
        LambdaQueryWrapper<TaskTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskTag::getProjectId, tag.getProjectId());
        wrapper.eq(TaskTag::getTagName, tag.getTagName());
        Long count = baseMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("标签名称已存在");
        }
        baseMapper.insert(tag);
    }

    @Transactional
    public void updateTag(TaskTag tag) {
        TaskTag existingTag = baseMapper.selectById(tag.getId());
        if (existingTag == null) {
            throw new BusinessException("标签不存在");
        }

        if (!existingTag.getTagName().equals(tag.getTagName())) {
            LambdaQueryWrapper<TaskTag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TaskTag::getProjectId, tag.getProjectId());
            wrapper.eq(TaskTag::getTagName, tag.getTagName());
            Long count = baseMapper.selectCount(wrapper);
            if (count > 0) {
                throw new BusinessException("标签名称已存在");
            }
        }

        baseMapper.updateById(tag);
    }

    @Transactional
    public void removeTag(Long id) {
        TaskTag tag = baseMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }

        LambdaQueryWrapper<TaskTagRel> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(TaskTagRel::getTagId, id);
        taskTagRelMapper.delete(relWrapper);

        baseMapper.deleteById(id);
    }
}
