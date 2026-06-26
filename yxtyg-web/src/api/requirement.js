import request from '@/utils/request'

export function getRequirementList(params) {
  return request({
    url: '/requirement/list',
    method: 'get',
    params
  })
}

export function getRequirementDetail(id) {
  return request({
    url: `/requirement/detail/${id}`,
    method: 'get'
  })
}

export function createRequirement(data) {
  return request({
    url: '/requirement',
    method: 'post',
    data
  })
}

export function updateRequirement(id, data) {
  return request({
    url: `/requirement/${id}`,
    method: 'put',
    data
  })
}

export function deleteRequirement(id) {
  return request({
    url: `/requirement/${id}`,
    method: 'delete'
  })
}

export function importRequirement(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/requirement/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function downloadTemplate() {
  return request({
    url: '/requirement/template',
    method: 'get',
    responseType: 'blob'
  })
}

export function fillFinalWorkload(id, finalWorkload) {
  return request({
    url: `/requirement/${id}/fill`,
    method: 'post',
    data: { finalWorkload }
  })
}
