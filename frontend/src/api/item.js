import request from './request'

export function getItems(params) {
  return request.get('/items', { params })
}

export function getItemBySlug(slug) {
  return request.get(`/items/${slug}`)
}

export function createItem(data) {
  return request.post('/items', data)
}

export function updateItem(id, data) {
  return request.put(`/items/${id}`, data)
}

export function deleteItem(id) {
  return request.delete(`/items/${id}`)
}

export function getHotItems(params) {
  return request.get('/items/hot', { params })
}

export function getItemsByType(type) {
  return request.get('/items/types', { params: { type } })
}

export function getRecommended(params) {
  return request.get('/items/recommended', { params })
}

export function getFeatured(params) {
  return request.get('/items/featured', { params })
}
