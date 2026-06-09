import request from './request'

export function getReviews(itemId, page = 1, size = 20) {
  return request.get(`/reviews/item/${itemId}`, { params: { page, size } })
}

export function createReview(data) {
  return request.post('/reviews', data)
}

export function deleteReview(id) {
  return request.delete(`/reviews/${id}`)
}
