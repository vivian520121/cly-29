import request from '../utils/request';

export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data,
  });
};

export const logout = () => {
  return request({
    url: '/auth/logout',
    method: 'post',
  });
};

export const getUserInfo = () => {
  return request({
    url: '/auth/userInfo',
    method: 'get',
  });
};

export const refreshToken = () => {
  return request({
    url: '/auth/refreshToken',
    method: 'post',
  });
};
