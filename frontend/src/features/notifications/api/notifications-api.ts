import { apiRequest } from '@/shared/api/client'
import { toQueryString, type PageQuery, type PageResponse, type UUID } from '@/shared/types/api'
import type { Notification } from '../types/notification'

export const notificationsApi = {
  list: (query: PageQuery = {}) => apiRequest<PageResponse<Notification>>(`/notifications${toQueryString({ ...query })}`),
  markAsRead: (id: UUID) => apiRequest<Notification>(`/notifications/${id}/read`, { method: 'PATCH' }),
}
