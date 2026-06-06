package com.cly.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cly.project.entity.ProjectMember;
import com.cly.project.entity.User;
import com.cly.project.enums.ProjectRoleEnum;
import com.cly.project.exception.BusinessException;
import com.cly.project.mapper.ProjectMemberMapper;
import com.cly.project.mapper.UserMapper;
import com.cly.project.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectMemberService extends ServiceImpl<ProjectMemberMapper, ProjectMember> {

    private final UserMapper userMapper;

    public List<ProjectMember> listByProjectId(Long projectId) {
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId);
        wrapper.orderByAsc(ProjectMember::getRole);
        List<ProjectMember> members = baseMapper.selectList(wrapper);

        for (ProjectMember member : members) {
            if (member.getUserId() != null) {
                User user = userMapper.selectById(member.getUserId());
                if (user != null) {
                    member.setUsername(user.getUsername());
                    member.setRealName(user.getRealName());
                    member.setAvatar(user.getAvatar());
                }
            }
            for (ProjectRoleEnum role : ProjectRoleEnum.values()) {
                if (role.getCode().equals(member.getRole())) {
                    member.setRoleName(role.getDesc());
                    break;
                }
            }
        }

        return members;
    }

    @Transactional
    public void addMember(Long projectId, Long userId, Integer role) {
        LambdaQueryWrapper<ProjectMember> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(ProjectMember::getProjectId, projectId);
        existWrapper.eq(ProjectMember::getUserId, userId);
        Long count = baseMapper.selectCount(existWrapper);
        if (count > 0) {
            throw new BusinessException("该用户已是项目成员");
        }

        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole(role);
        member.setJoinTime(LocalDateTime.now());
        member.setCreateBy(UserContext.getUserId());
        member.setCreateTime(LocalDateTime.now());
        baseMapper.insert(member);
    }

    @Transactional
    public void updateMemberRole(Long projectId, Long userId, Integer role) {
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId);
        wrapper.eq(ProjectMember::getUserId, userId);
        ProjectMember member = baseMapper.selectOne(wrapper);
        if (member == null) {
            throw new BusinessException("该用户不是项目成员");
        }

        member.setRole(role);
        baseMapper.updateById(member);
    }

    @Transactional
    public void removeMember(Long projectId, Long userId) {
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId);
        wrapper.eq(ProjectMember::getUserId, userId);
        baseMapper.delete(wrapper);
    }

    public boolean checkProjectPermission(Long projectId, Long userId, Integer minRole) {
        if (UserContext.isAdmin()) {
            return true;
        }

        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId);
        wrapper.eq(ProjectMember::getUserId, userId);
        ProjectMember member = baseMapper.selectOne(wrapper);
        if (member == null) {
            return false;
        }

        return member.getRole() <= minRole;
    }
}
