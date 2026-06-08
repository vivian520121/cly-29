import request from '@/utils/request'

export const getCompanyList = (params) => {
  return request({
    url: '/company/list',
    method: 'get',
    params
  })
}

export const getCompanyListAll = () => {
  return request({
    url: '/company/list-all',
    method: 'get'
  })
}

export const getCompanyDetail = (id) => {
  return request({
    url: `/company/${id}`,
    method: 'get'
  })
}

export const createCompany = (data) => {
  return request({
    url: '/company',
    method: 'post',
    data
  })
}

export const updateCompany = (data) => {
  return request({
    url: '/company',
    method: 'put',
    data
  })
}

export const deleteCompany = (id) => {
  return request({
    url: `/company/${id}`,
    method: 'delete'
  })
}
