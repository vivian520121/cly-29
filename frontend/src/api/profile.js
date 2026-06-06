import request from '../utils/request';

export const getProfile = () => {
  return request({
    url: '/profile',
    method: 'get',
  });
};

export const updateProfile = (data) => {
  return request({
    url: '/profile',
    method: 'put',
    data,
  });
};

export const updatePassword = (data) => {
  return request({
    url: '/profile/password',
    method: 'put',
    data,
  });
};
