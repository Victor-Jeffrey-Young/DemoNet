import request from './request'

export function triggerSteamFetch(appIds) {
  return request.post('/admin/fetch/steam', { appIds })
}

export function triggerTMDBFetch(query) {
  return request.post('/admin/fetch/tmdb', { query })
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
