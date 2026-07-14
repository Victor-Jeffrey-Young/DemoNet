import request from './request'

export function getScenes(params) {
  return request.get('/scenes', { params })
}

export function getSceneBySlug(slug) {
  return request.get(`/scenes/${slug}`)
}
