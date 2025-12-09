// 获取单个宠物详情
import request from '@/utils/request'
export function getPetById (petId) {
  return request.get(`/pet/${petId}`)
}
export function getPetPage (pageNum, pageSize, userId) {
  return request.get('/pet/page', { params: { pageNum, pageSize, userId } })
}
// 更新宠物信息
export function updatePet (petData) {
  return request.put('/pet/update', petData)
}

// 删除宠物
export function deletePet (petId) {
  return request.delete(`/pet/${petId}`)
}

// 上传图片
export function uploadImg (file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/pet/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 关联图片
export function bindImg (petId, imgUrl) {
  return request.put('/pet/update-img', null, {
    params: { petId, imgUrl }
  })
}
