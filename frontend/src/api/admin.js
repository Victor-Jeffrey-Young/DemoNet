import request from './request'

// ========== Item CRUD ==========

export function getAdminItems(params) {
  return request.get('/admin/items', { params })
}

export function getAdminItem(id) {
  return request.get(`/admin/items/${id}`)
}

export function createItem(data) {
  return request.post('/admin/items', data)
}

export function updateItem(id, data) {
  return request.put(`/admin/items/${id}`, data)
}

export function deleteItem(id) {
  return request.delete(`/admin/items/${id}`)
}

export function updateItemStatus(id, status) {
  return request.put(`/admin/items/${id}/status`, { status })
}

export function toggleFeatured(id) {
  return request.put(`/admin/items/${id}/featured`)
}

// ========== Carousel Management ==========

export function getCarouselItems(type) {
  return request.get(`/admin/carousel/${type}`)
}

export function saveCarouselOrder(type, itemIds) {
  return request.post(`/admin/carousel/${type}`, { itemIds })
}

export function removeFromCarousel(type, itemId) {
  return request.delete(`/admin/carousel/${type}/${itemId}`)
}

// ========== Image Upload ==========

export function uploadImage(id, formData) {
  return request.post(`/admin/upload/${id}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// ========== Tag Management ==========

export function getAdminTags() {
  return request.get('/admin/tags')
}

export function getAdminTagsPaged(params) {
  return request.get('/admin/tags/paged', { params })
}

export function createAdminTag(name) {
  return request.post('/admin/tags', { name })
}

export function deleteAdminTag(id) {
  return request.delete(`/admin/tags/${id}`)
}

export function associateItemTags(itemId, tagIds) {
  return request.post(`/admin/items/${itemId}/tags`, tagIds)
}

export function removeItemTag(itemId, tagId) {
  return request.delete(`/admin/items/${itemId}/tags/${tagId}`)
}

// ========== Stats ==========

export function getAdminStats() {
  return request.get('/admin/stats')
}

// ========== Fetch & Pending (existing) ==========

export function triggerSteamFetch(appIds, targetType = 'game') {
  return request.post('/admin/fetch/steam', { appIds, targetType })
}

export function triggerTMDBFetch(query, targetType = 'movie') {
  return request.post('/admin/fetch/tmdb', { query, targetType })
}

export function triggerAniListFetch(query, targetType = 'anime') {
  return request.post('/admin/fetch/anilist', { query, targetType })
}

export function triggerBangumiFetch(query, targetType = 'anime') {
  return request.post('/admin/fetch/bangumi', { query, targetType })
}

export function triggerTMDBTVFetch(query, targetType = 'anime') {
  return request.post('/admin/fetch/tmdb-tv', { query, targetType })
}

export function getPendingItems(params) {
  return request.get('/admin/pending', { params })
}

export function approveItem(id) {
  return request.put(`/admin/approve/${id}`)
}

export function rejectItem(id) {
  return request.put(`/admin/reject/${id}`)
}
