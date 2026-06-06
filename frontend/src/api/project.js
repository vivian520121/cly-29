import request from '../utils/request';

export const getProjectList = (params) => {
  return request({
    url: '/project/list',
    method: 'get',
    params,
  });
};

export const getProject = (id) => {
  return request({
    url: `/project/${id}`,
    method: 'get',
  });
};

export const createProject = (data) => {
  return request({
    url: '/project',
    method: 'post',
    data,
  });
};

export const updateProject = (data) => {
  return request({
    url: '/project',
    method: 'put',
    data,
  });
};

export const deleteProject = (id) => {
  return request({
    url: `/project/${id}`,
    method: 'delete',
  });
};

export const getProjectOverview = (id) => {
  return request({
    url: `/project/${id}/overview`,
    method: 'get',
  });
};

export const getProjectGantt = (id) => {
  return request({
    url: `/project/${id}/gantt`,
    method: 'get',
  });
};

export const getMyProjects = () => {
  return request({
    url: '/project/my',
    method: 'get',
  });
};
