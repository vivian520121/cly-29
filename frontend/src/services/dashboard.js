import request from '@/utils/request'

export const getDashboardData = () => {
  return request({
    url: '/dashboard',
    method: 'get'
  })
}

export const getTodoTasks = (params) => {
  return request({
    url: '/dashboard/todo-tasks',
    method: 'get',
    params
  })
}

export const getRecentActivities = (params) => {
  return request({
    url: '/dashboard/activities',
    method: 'get',
    params
  })
}

export const getTaskStatistics = () => {
  return request({
    url: '/dashboard/task-statistics',
    method: 'get'
  })
}
