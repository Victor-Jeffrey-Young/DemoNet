import request from './request'

export function loginApi(username, password) {
  return request.post('/auth/login', { username, password })
}

export function registerApi(username, email, password) {
  return request.post('/auth/register', { username, email, password })
}

export function getMe() {
  return request.get('/auth/me')
}

export function saveUserItem(itemId, status) {
  return request.post('/user/items', { itemId, status })
}

export function getUserItems(status) {
  return request.get('/user/items', { params: { status } })
}

export function removeUserItem(itemId) {
  return request.delete(`/user/items/${itemId}`)
}

export function getUserItemStatus(itemId) {
  return request.get(`/user/items/${itemId}`)
}
