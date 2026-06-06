import request from '../utils/request';

export const getTaskList = (params) => {
  return request({
    url: '/task/list',
    method: 'get',
    params,
  });
};

export const getTaskKanban = (params) => {
  return request({
    url: '/task/kanban',
    method: 'get',
    params,
  });
};

export const getTask = (id) => {
  return request({
    url: `/task/${id}`,
    method: 'get',
  });
};

export const createTask = (data) => {
  return request({
    url: '/task',
    method: 'post',
    data,
  });
};

export const updateTask = (data) => {
  return request({
    url: '/task',
    method: 'put',
    data,
  });
};

export const updateTaskStatus = (data) => {
  return request({
    url: '/task/status',
    method: 'put',
    data,
  });
};

export const getTaskTree = (projectId) => {
  return request({
    url: `/task/tree/${projectId}`,
    method: 'get',
  });
};

export const getMyTasks = () => {
  return request({
    url: '/task/my',
    method: 'get',
  });
};

export const getWorklogs = (taskId) => {
  return request({
    url: `/task/${taskId}/worklogs`,
    method: 'get',
  });
};

export const addWorklog = (data) => {
  return request({
    url: '/task/worklog',
    method: 'post',
    data,
  });
};

export const updateWorklog = (data) => {
  return request({
    url: '/task/worklog',
    method: 'put',
    data,
  });
};

export const deleteWorklog = (id) => {
  return request({
    url: `/task/worklog/${id}`,
    method: 'delete',
  });
};

export const getTaskLogs = (taskId) => {
  return request({
    url: `/task/${taskId}/logs`,
    method: 'get',
  });
};
