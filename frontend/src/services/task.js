import request from '@/utils/request'

export const getTaskList = (params) => {
  return request({
    url: '/task/list',
    method: 'get',
    params
  })
}

export const getTaskKanban = (params) => {
  return request({
    url: '/task/kanban',
    method: 'get',
    params
  })
}

export const getTaskDetail = (id) => {
  return request({
    url: `/task/${id}`,
    method: 'get'
  })
}

export const createTask = (data) => {
  return request({
    url: '/task',
    method: 'post',
    data
  })
}

export const updateTask = (id, data) => {
  return request({
    url: `/task/${id}`,
    method: 'put',
    data
  })
}

export const updateTaskStatus = (id, data) => {
  return request({
    url: `/task/${id}/status`,
    method: 'put',
    data
  })
}

export const deleteTask = (id) => {
  return request({
    url: `/task/${id}`,
    method: 'delete'
  })
}

export const getTaskSubtasks = (taskId) => {
  return request({
    url: `/task/${taskId}/subtasks`,
    method: 'get'
  })
}

export const getTaskLogs = (taskId) => {
  return request({
    url: `/task/${taskId}/logs`,
    method: 'get'
  })
}

export const getTaskWorklogs = (taskId) => {
  return request({
    url: `/task/${taskId}/worklogs`,
    method: 'get'
  })
}

export const addWorklog = (taskId, data) => {
  return request({
    url: `/task/${taskId}/worklogs`,
    method: 'post',
    data
  })
}

export const getTaskAttachments = (taskId) => {
  return request({
    url: `/task/${taskId}/attachments`,
    method: 'get'
  })
}

export const getTaskStatistics = (params) => {
  return request({
    url: '/task/statistics',
    method: 'get',
    params
  })
}
