import request from '../utils/request';

export const getDeptTree = () => {
  return request({
    url: '/organization/dept/tree',
    method: 'get',
  });
};

export const getDeptList = () => {
  return request({
    url: '/organization/dept/list',
    method: 'get',
  });
};

export const createDept = (data) => {
  return request({
    url: '/organization/dept',
    method: 'post',
    data,
  });
};

export const updateDept = (data) => {
  return request({
    url: '/organization/dept',
    method: 'put',
    data,
  });
};

export const deleteDept = (id) => {
  return request({
    url: `/organization/dept/${id}`,
    method: 'delete',
  });
};

export const getUserPage = (params) => {
  return request({
    url: '/organization/user/page',
    method: 'get',
    params,
  });
};

export const getUser = (id) => {
  return request({
    url: `/organization/user/${id}`,
    method: 'get',
  });
};

export const createUser = (data) => {
  return request({
    url: '/organization/user',
    method: 'post',
    data,
  });
};

export const updateUser = (data) => {
  return request({
    url: '/organization/user',
    method: 'put',
    data,
  });
};

export const deleteUser = (id) => {
  return request({
    url: `/organization/user/${id}`,
    method: 'delete',
  });
};
