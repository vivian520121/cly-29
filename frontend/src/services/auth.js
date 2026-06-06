import request from '@/utils/request'

export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export const logout = () => {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

export const getUserInfo = () => {
  return request({
    url: '/auth/user-info',
    method: 'get'
  })
}

export const updatePassword = (data) => {
  return request({
    url: '/auth/update-password',
    method: 'post',
    data
  })
}

export const resetPassword = (data) => {
  return request({
    url: '/auth/reset-password',
    method: 'post',
    data
  })
}
