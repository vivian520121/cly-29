import request from '@/utils/request'

export const getDepartmentTree = () => {
  return request({
    url: '/department/tree',
    method: 'get'
  })
}

export const getDepartmentList = (params) => {
  return request({
    url: '/department/list',
    method: 'get',
    params
  })
}

export const createDepartment = (data) => {
  return request({
    url: '/department',
    method: 'post',
    data
  })
}

export const updateDepartment = (id, data) => {
  return request({
    url: `/department/${id}`,
    method: 'put',
    data
  })
}

export const deleteDepartment = (id) => {
  return request({
    url: `/department/${id}`,
    method: 'delete'
  })
}

export const getUserList = (params) => {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}

export const getUserDetail = (id) => {
  return request({
    url: `/user/${id}`,
    method: 'get'
  })
}

export const createUser = (data) => {
  return request({
    url: '/user',
    method: 'post',
    data
  })
}

export const updateUser = (id, data) => {
  return request({
    url: `/user/${id}`,
    method: 'put',
    data
  })
}

export const deleteUser = (id) => {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}

export const getUserDepartments = (userId) => {
  return request({
    url: `/user/${userId}/departments`,
    method: 'get'
  })
}

export const updateUserDepartments = (userId, data) => {
  return request({
    url: `/user/${userId}/departments`,
    method: 'put',
    data
  })
}
