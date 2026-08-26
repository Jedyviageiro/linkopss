import { apiRequest } from '@/shared/api/client'
import { toQueryString, type PageQuery, type PageResponse, type UUID } from '@/shared/types/api'
import type { CreateReviewRequest, Review } from '../types/review'

export const reviewsApi = {
  byProvider: (providerId: UUID, query: PageQuery = {}) => apiRequest<PageResponse<Review>>(`/providers/${providerId}/reviews${toQueryString({ ...query })}`, { auth: false }),
  create: (bookingId: UUID, payload: CreateReviewRequest) => apiRequest<Review>(`/bookings/${bookingId}/review`, { method: 'POST', body: payload }),
}
