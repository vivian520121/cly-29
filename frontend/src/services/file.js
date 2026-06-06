import request from '@/utils/request'

export const getFileList = (params) => {
  return request({
    url: '/file/list',
    method: 'get',
    params
  })
}

export const uploadFile = (file, onProgress) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: onProgress
  })
}

export const checkChunk = (data) => {
  return request({
    url: '/file/check-chunk',
    method: 'post',
    data
  })
}

export const uploadChunk = (data, onProgress) => {
  const formData = new FormData()
  formData.append('chunk', data.chunk)
  formData.append('chunkIndex', data.chunkIndex)
  formData.append('totalChunks', data.totalChunks)
  formData.append('fileName', data.fileName)
  formData.append('fileMd5', data.fileMd5)
  return request({
    url: '/file/upload-chunk',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: onProgress
  })
}

export const mergeChunks = (data) => {
  return request({
    url: '/file/merge-chunks',
    method: 'post',
    data
  })
}

export const deleteFile = (id) => {
  return request({
    url: `/file/${id}`,
    method: 'delete'
  })
}

export const downloadFile = (id) => {
  return request({
    url: `/file/${id}/download`,
    method: 'get',
    responseType: 'blob'
  })
}

export const previewFile = (id) => {
  return request({
    url: `/file/${id}/preview`,
    method: 'get'
  })
}
