package com.cly.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cly.project.dto.ProjectQueryDTO;
import com.cly.project.dto.ProjectSaveDTO;
import com.cly.project.entity.*;
import com.cly.project.enums.ProjectRoleEnum;
import com.cly.project.enums.ProjectStatusEnum;
import com.cly.project.enums.TaskPriorityEnum;
import com.cly.project.enums.TaskStatusEnum;
import com.cly.project.exception.BusinessException;
import com.cly.project.mapper.*;
import com.cly.project.util.UserContext;
import com.cly.project.vo.GanttDataVO;
import com.cly.project.vo.ProjectOverviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService extends ServiceImpl<ProjectMapper, Project> {

    private final ProjectMemberMapper projectMemberMapper;
    private final UserMapper userMapper;
    private final TaskMapper taskMapper;
    private final ProjectPhaseMapper projectPhaseMapper;
    private final MilestoneMapper milestoneMapper;

    public IPage<Project> page(ProjectQueryDTO query) {
        Page<Project> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();

        Long currentUserId = UserContext.getUserId();
        boolean isAdmin = UserContext.isAdmin();

        if (!isAdmin) {
            LambdaQueryWrapper<ProjectMember> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(ProjectMember::getUserId, currentUserId);
            List<ProjectMember> members = projectMemberMapper.selectList(memberWrapper);
            if (members.isEmpty()) {
                return new Page<>(query.getPageNum(), query.getPageSize(), 0);
            }
            List<Long> projectIds = members.stream()
                    .map(ProjectMember::getProjectId)
                    .collect(Collectors.toList());
            wrapper.in(Project::getId, projectIds);
        }

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(new java.util.function.Consumer<LambdaQueryWrapper<Project>>() {
                @Override
                public void accept(LambdaQueryWrapper<Project> w) {
                    w.like(Project::getProjectName, query.getKeyword())
                            .or()
                            .like(Project::getProjectCode, query.getKeyword())
                            .or()
                            .like(Project::getDescription, query.getKeyword());
                }
            });
        }
        if (query.getStatus() != null) {
            wrapper.eq(Project::getStatus, query.getStatus());
        }
        if (query.getPriority() != null) {
            wrapper.eq(Project::getPriority, query.getPriority());
        }
        if (query.getManagerId() != null) {
            wrapper.eq(Project::getManagerId, query.getManagerId());
        }
        if (query.getMemberId() != null) {
            LambdaQueryWrapper<ProjectMember> memberWrapper = new LambdaQueryWrapper<>();
            memberWrapper.eq(ProjectMember::getUserId, query.getMemberId());
            List<ProjectMember> members = projectMemberMapper.selectList(memberWrapper);
            if (!members.isEmpty()) {
                List<Long> projectIds = members.stream()
                        .map(ProjectMember::getProjectId)
                        .collect(Collectors.toList());
                wrapper.in(Project::getId, projectIds);
            } else {
                wrapper.eq(Project::getId, -1);
            }
        }

        wrapper.orderByDesc(Project::getCreateTime);

        IPage<Project> projectPage = baseMapper.selectPage(page, wrapper);

        for (Project project : projectPage.getRecords()) {
            if (project.getManagerId() != null) {
                User manager = userMapper.selectById(project.getManagerId());
                if (manager != null) {
                    project.setManagerName(manager.getRealName());
                }
            }
        }

        return projectPage;
    }

    public Project getById(Long id) {
        Project project = baseMapper.selectById(id);
        if (project != null && project.getManagerId() != null) {
            User manager = userMapper.selectById(project.getManagerId());
            if (manager != null) {
                project.setManagerName(manager.getRealName());
            }
        }
        return project;
    }

    @Transactional
    public void saveProject(ProjectSaveDTO dto) {
        LambdaQueryWrapper<Project> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(Project::getProjectCode, dto.getProjectCode());
        Long count = baseMapper.selectCount(codeWrapper);
        if (count > 0) {
            throw new BusinessException("项目编码已存在");
        }

        Project project = new Project();
        BeanUtils.copyProperties(dto, project);
        baseMapper.insert(project);

        Long currentUserId = UserContext.getUserId();
        ProjectMember adminMember = new ProjectMember();
        adminMember.setProjectId(project.getId());
        adminMember.setUserId(currentUserId);
        adminMember.setRole(ProjectRoleEnum.ADMIN.getCode());
        adminMember.setJoinTime(LocalDateTime.now());
        adminMember.setCreateBy(currentUserId);
        adminMember.setCreateTime(LocalDateTime.now());
        projectMemberMapper.insert(adminMember);

        if (dto.getMemberIds() != null && !dto.getMemberIds().isEmpty()) {
            for (Long userId : dto.getMemberIds()) {
                if (!userId.equals(currentUserId)) {
                    ProjectMember member = new ProjectMember();
                    member.setProjectId(project.getId());
                    member.setUserId(userId);
                    member.setRole(ProjectRoleEnum.MEMBER.getCode());
                    member.setJoinTime(LocalDateTime.now());
                    member.setCreateBy(currentUserId);
                    member.setCreateTime(LocalDateTime.now());
                    projectMemberMapper.insert(member);
                }
            }
        }
    }

    @Transactional
    public void updateProject(ProjectSaveDTO dto) {
        Project existingProject = baseMapper.selectById(dto.getId());
        if (existingProject == null) {
            throw new BusinessException("项目不存在");
        }

        if (!existingProject.getProjectCode().equals(dto.getProjectCode())) {
            LambdaQueryWrapper<Project> codeWrapper = new LambdaQueryWrapper<>();
            codeWrapper.eq(Project::getProjectCode, dto.getProjectCode());
            Long count = baseMapper.selectCount(codeWrapper);
            if (count > 0) {
                throw new BusinessException("项目编码已存在");
            }
        }

        Project project = new Project();
        BeanUtils.copyProperties(dto, project);
        baseMapper.updateById(project);

        if (dto.getMemberIds() != null) {
            Long currentUserId = UserContext.getUserId();
            LambdaQueryWrapper<ProjectMember> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(ProjectMember::getProjectId, dto.getId());
            deleteWrapper.ne(ProjectMember::getUserId, currentUserId);
            projectMemberMapper.delete(deleteWrapper);

            for (Long userId : dto.getMemberIds()) {
                if (!userId.equals(currentUserId)) {
                    LambdaQueryWrapper<ProjectMember> existWrapper = new LambdaQueryWrapper<>();
                    existWrapper.eq(ProjectMember::getProjectId, dto.getId());
                    existWrapper.eq(ProjectMember::getUserId, userId);
                    Long existCount = projectMemberMapper.selectCount(existWrapper);
                    if (existCount == 0) {
                        ProjectMember member = new ProjectMember();
                        member.setProjectId(dto.getId());
                        member.setUserId(userId);
                        member.setRole(ProjectRoleEnum.MEMBER.getCode());
                        member.setJoinTime(LocalDateTime.now());
                        member.setCreateBy(currentUserId);
                        member.setCreateTime(LocalDateTime.now());
                        projectMemberMapper.insert(member);
                    }
                }
            }
        }
    }

    @Transactional
    public void removeProject(Long id) {
        Project existingProject = baseMapper.selectById(id);
        if (existingProject == null) {
            throw new BusinessException("项目不存在");
        }

        LambdaQueryWrapper<Task> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.eq(Task::getProjectId, id);
        Long taskCount = taskMapper.selectCount(taskWrapper);
        if (taskCount > 0) {
            throw new BusinessException("该项目下存在任务，无法删除");
        }

        LambdaQueryWrapper<ProjectMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(ProjectMember::getProjectId, id);
        projectMemberMapper.delete(memberWrapper);

        LambdaQueryWrapper<ProjectPhase> phaseWrapper = new LambdaQueryWrapper<>();
        phaseWrapper.eq(ProjectPhase::getProjectId, id);
        projectPhaseMapper.delete(phaseWrapper);

        LambdaQueryWrapper<Milestone> milestoneWrapper = new LambdaQueryWrapper<>();
        milestoneWrapper.eq(Milestone::getProjectId, id);
        milestoneMapper.delete(milestoneWrapper);

        baseMapper.deleteById(id);
    }

    public ProjectOverviewVO getOverview(Long projectId) {
        Project project = baseMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }

        ProjectOverviewVO vo = new ProjectOverviewVO();
        BeanUtils.copyProperties(project, vo);

        for (ProjectStatusEnum status : ProjectStatusEnum.values()) {
            if (status.getCode().equals(project.getStatus())) {
                vo.setStatusName(status.getDesc());
                break;
            }
        }

        for (TaskPriorityEnum priority : TaskPriorityEnum.values()) {
            if (priority.getCode().equals(project.getPriority())) {
                vo.setPriorityName(priority.getDesc());
                break;
            }
        }

        LambdaQueryWrapper<Task> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.eq(Task::getProjectId, projectId);
        List<Task> tasks = taskMapper.selectList(taskWrapper);

        int totalCount = tasks.size();
        int completedCount = 0;
        int inProgressCount = 0;
        int todoCount = 0;
        int cancelledCount = 0;

        for (Task task : tasks) {
            if (TaskStatusEnum.DONE.getCode().equals(task.getStatus())) {
                completedCount++;
            } else if (TaskStatusEnum.IN_PROGRESS.getCode().equals(task.getStatus())) {
                inProgressCount++;
            } else if (TaskStatusEnum.TODO.getCode().equals(task.getStatus())) {
                todoCount++;
            } else if (TaskStatusEnum.CANCELLED.getCode().equals(task.getStatus())) {
                cancelledCount++;
            }
        }

        vo.setTotalTaskCount(totalCount);
        vo.setCompletedTaskCount(completedCount);
        vo.setInProgressTaskCount(inProgressCount);
        vo.setTodoTaskCount(todoCount);
        vo.setCancelledTaskCount(cancelledCount);

        if (totalCount > 0) {
            int progress = (int) ((completedCount * 100.0) / totalCount);
            vo.setProgress(progress);
        } else {
            vo.setProgress(0);
        }

        LambdaQueryWrapper<ProjectMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(ProjectMember::getProjectId, projectId);
        vo.setMemberCount(projectMemberMapper.selectCount(memberWrapper).intValue());

        LambdaQueryWrapper<Milestone> milestoneWrapper = new LambdaQueryWrapper<>();
        milestoneWrapper.eq(Milestone::getProjectId, projectId);
        List<Milestone> milestones = milestoneMapper.selectList(milestoneWrapper);
        vo.setMilestoneCount(milestones.size());

        int completedMilestoneCount = 0;
        for (Milestone milestone : milestones) {
            if (milestone.getStatus() != null && milestone.getStatus() == 2) {
                completedMilestoneCount++;
            }
        }
        vo.setCompletedMilestoneCount(completedMilestoneCount);

        LambdaQueryWrapper<ProjectPhase> phaseWrapper = new LambdaQueryWrapper<>();
        phaseWrapper.eq(ProjectPhase::getProjectId, projectId);
        vo.setPhaseCount(projectPhaseMapper.selectCount(phaseWrapper).intValue());

        return vo;
    }

    public GanttDataVO getGanttData(Long projectId) {
        Project project = baseMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }

        GanttDataVO vo = new GanttDataVO();

        LambdaQueryWrapper<ProjectPhase> phaseWrapper = new LambdaQueryWrapper<>();
        phaseWrapper.eq(ProjectPhase::getProjectId, projectId);
        phaseWrapper.orderByAsc(ProjectPhase::getSortOrder);
        vo.setPhases(projectPhaseMapper.selectList(phaseWrapper));

        LambdaQueryWrapper<Milestone> milestoneWrapper = new LambdaQueryWrapper<>();
        milestoneWrapper.eq(Milestone::getProjectId, projectId);
        milestoneWrapper.orderByAsc(Milestone::getSortOrder);
        vo.setMilestones(milestoneMapper.selectList(milestoneWrapper));

        LambdaQueryWrapper<Task> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.eq(Task::getProjectId, projectId);
        taskWrapper.orderByAsc(Task::getSortOrder);
        List<Task> tasks = taskMapper.selectList(taskWrapper);

        for (Task task : tasks) {
            if (task.getAssigneeId() != null) {
                User assignee = userMapper.selectById(task.getAssigneeId());
                if (assignee != null) {
                    task.setAssigneeName(assignee.getRealName());
                    task.setAssigneeAvatar(assignee.getAvatar());
                }
            }
        }

        vo.setTasks(tasks);

        return vo;
    }

    public List<Project> getMyProjects() {
        Long currentUserId = UserContext.getUserId();
        LambdaQueryWrapper<ProjectMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(ProjectMember::getUserId, currentUserId);
        List<ProjectMember> members = projectMemberMapper.selectList(memberWrapper);
        if (members.isEmpty()) {
            return List.of();
        }

        List<Long> projectIds = members.stream()
                .map(ProjectMember::getProjectId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<Project> projectWrapper = new LambdaQueryWrapper<>();
        projectWrapper.in(Project::getId, projectIds);
        projectWrapper.orderByDesc(Project::getCreateTime);
        List<Project> projects = baseMapper.selectList(projectWrapper);

        for (Project project : projects) {
            if (project.getManagerId() != null) {
                User manager = userMapper.selectById(project.getManagerId());
                if (manager != null) {
                    project.setManagerName(manager.getRealName());
                }
            }
        }

        return projects;
    }
}
