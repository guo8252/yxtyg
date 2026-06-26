import request from '@/utils/request'

export function urgeRequirement(requirementId) {
  return request({
    url: `/urge/${requirementId}`,
    method: 'post'
  })
}

export function getUrgeList(params) {
  return request({
    url: '/urge/list',
    method: 'get',
    params
  })
}
