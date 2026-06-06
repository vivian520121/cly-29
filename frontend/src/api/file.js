import request from '../utils/request';

export const uploadFile = (formData, onProgress) => {
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    onUploadProgress: onProgress,
  });
};

export const initChunkUpload = (data) => {
  return request({
    url: '/file/chunk/init',
    method: 'post',
    data,
  });
};

export const uploadChunk = (formData) => {
  return request({
    url: '/file/chunk/upload',
    method: 'post',
    data: formData,
  });
};

export const completeChunkUpload = (data) => {
  return request({
    url: '/file/chunk/complete',
    method: 'post',
    data,
  });
};

export const getFileList = (params) => {
  return request({
    url: '/file/list',
    method: 'get',
    params,
  });
};

export const getFile = (id) => {
  return request({
    url: `/file/${id}`,
    method: 'get',
  });
};

export const deleteFile = (id) => {
  return request({
    url: `/file/${id}`,
    method: 'delete',
  });
};
