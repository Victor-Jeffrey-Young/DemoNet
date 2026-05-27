import request from './request'

export function getTags() {
  return request.get('/tags')
}

export function createTag(name) {
  return request.post('/tags', { name })
}

export function deleteTag(id) {
  return request.delete(`/tags/${id}`)
}

export function getItemTags(itemId) {
  return request.get(`/tags/items/${itemId}`)
}

export function associateTags(itemId, tagIds) {
  return request.post(`/tags/items/${itemId}`, { tagIds })
}
