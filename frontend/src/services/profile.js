import request from '@/utils/request'

export const getProfile = () => {
  return request({
    url: '/profile',
    method: 'get'
  })
}

export const updateProfile = (data) => {
  return request({
    url: '/profile',
    method: 'put',
    data
  })
}

export const updateAvatar = (data) => {
  return request({
    url: '/profile/avatar',
    method: 'post',
    data
  })
}

export const getMyProjects = (params) => {
  return request({
    url: '/profile/projects',
    method: 'get',
    params
  })
}

export const getMyTaskStatistics = () => {
  return request({
    url: '/profile/task-statistics',
    method: 'get'
  })
}
