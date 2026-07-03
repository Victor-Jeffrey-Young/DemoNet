import request from './request'

// ========== 条目 CRUD ==========

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

// ========== 轮播图管理 ==========

export function getCarouselItems(type) {
  return request.get(`/admin/carousel/${type}`)
}

export function saveCarouselOrder(type, itemIds) {
  return request.post(`/admin/carousel/${type}`, { itemIds })
}

export function removeFromCarousel(type, itemId) {
  return request.delete(`/admin/carousel/${type}/${itemId}`)
}

// ========== 图片上传 ==========

export function uploadImage(id, formData) {
  return request.post(`/admin/upload/${id}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// ========== 标签管理 ==========

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

// ========== 数据统计 ==========

export function getAdminStats() {
  return request.get('/admin/stats')
}

// ========== 抓取与待审核 (现有) ==========

export function triggerSteamFetch(appIds, targetType = 'game') {
  return request.post('/admin/fetch/steam', { appIds, targetType })
}

export function searchSteamGames(query) {
  return request.get('/admin/steam/search', { params: { q: query } })
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

export function triggerItunesFetch(query, targetType = 'music') {
  return request.post('/admin/fetch/itunes', { query, targetType })
}

export function triggerIGDBFetch(endpoint, { query, limit, targetType } = {}) {
  return request.post('/admin/fetch/igdb', { endpoint, query, limit, targetType })
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

export function rejectBatch(ids) {
  return request.put('/admin/reject/batch', ids)
}

export function getCategorySettings() {
  return request.get('/admin/categories/settings')
}

export function batchDeleteItems(ids) {
  return request.post('/admin/items/batch-delete', { ids })
}

export function batchUpdateStatus(ids, status) {
  return request.post('/admin/items/batch-status', { ids, status })
}

export function updateCategorySettings(settings) {
  return request.put('/admin/categories/settings', settings)
}

// ── App Settings (API keys) ──
export function getAppSettings() {
  return request.get('/admin/settings')
}
export function updateAppSetting(key, value) {
  return request.put(`/admin/settings/${key}`, { value })
}

// ── SteamGridDB fetch ──
export function fetchSgdbPoster(id) {
  return request.post(`/admin/items/${id}/fetch-sgdb-poster`)
}

// ── Invite codes ──
export function getInviteCodes() {
  return request.get('/admin/invite-codes')
}
export function generateInviteCodes(count) {
  return request.post('/admin/invite-codes/generate', { count })
}

// ── User management ──
export function getAdminUsers() {
  return request.get('/admin/users')
}
export function toggleUserBan(id, enabled) {
  return request.put(`/admin/users/${id}/ban`, { enabled })
}
export function resetUserPassword(id) {
  return request.put(`/admin/users/${id}/reset-password`)
}
