import request from '@/utils/request'

export const globalSearch = (params) => {
  return request({
    url: '/search',
    method: 'get',
    params
  })
}

export const searchProjects = (params) => {
  return request({
    url: '/search/projects',
    method: 'get',
    params
  })
}

export const searchTasks = (params) => {
  return request({
    url: '/search/tasks',
    method: 'get',
    params
  })
}

export const quickSearch = (keyword) => {
  return request({
    url: '/search/quick',
    method: 'get',
    params: { keyword }
  })
}
