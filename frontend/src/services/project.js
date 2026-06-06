import request from '@/utils/request'

export const getProjectList = (params) => {
  return request({
    url: '/project/list',
    method: 'get',
    params
  })
}

export const getProjectDetail = (id) => {
  return request({
    url: `/project/${id}`,
    method: 'get'
  })
}

export const getProjectOverview = (id) => {
  return request({
    url: `/project/${id}/overview`,
    method: 'get'
  })
}

export const createProject = (data) => {
  return request({
    url: '/project',
    method: 'post',
    data
  })
}

export const updateProject = (id, data) => {
  return request({
    url: `/project/${id}`,
    method: 'put',
    data
  })
}

export const deleteProject = (id) => {
  return request({
    url: `/project/${id}`,
    method: 'delete'
  })
}

export const getProjectMembers = (projectId) => {
  return request({
    url: `/project/${projectId}/members`,
    method: 'get'
  })
}

export const addProjectMember = (projectId, data) => {
  return request({
    url: `/project/${projectId}/members`,
    method: 'post',
    data
  })
}

export const updateMemberRole = (projectId, memberId, data) => {
  return request({
    url: `/project/${projectId}/members/${memberId}/role`,
    method: 'put',
    data
  })
}

export const removeProjectMember = (projectId, memberId) => {
  return request({
    url: `/project/${projectId}/members/${memberId}`,
    method: 'delete'
  })
}

export const getProjectMilestones = (projectId) => {
  return request({
    url: `/project/${projectId}/milestones`,
    method: 'get'
  })
}

export const createMilestone = (projectId, data) => {
  return request({
    url: `/project/${projectId}/milestones`,
    method: 'post',
    data
  })
}

export const updateMilestone = (projectId, milestoneId, data) => {
  return request({
    url: `/project/${projectId}/milestones/${milestoneId}`,
    method: 'put',
    data
  })
}

export const deleteMilestone = (projectId, milestoneId) => {
  return request({
    url: `/project/${projectId}/milestones/${milestoneId}`,
    method: 'delete'
  })
}
