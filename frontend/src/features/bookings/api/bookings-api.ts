import { apiRequest } from '@/shared/api/client'
import { toQueryString, type PageResponse, type UUID } from '@/shared/types/api'
import type { Booking, BookingQuery, CreateBookingRequest } from '../types/booking'

const action = (id: UUID, name: string) => apiRequest<Booking>(`/bookings/${id}/${name}`, { method: 'PATCH' })

export const bookingsApi = {
  list: (query: BookingQuery = {}) => apiRequest<PageResponse<Booking>>(`/bookings${toQueryString({ ...query })}`),
  get: (id: UUID) => apiRequest<Booking>(`/bookings/${id}`),
  create: (payload: CreateBookingRequest) => apiRequest<Booking>('/bookings', { method: 'POST', body: payload }),
  accept: (id: UUID) => action(id, 'accept'),
  reject: (id: UUID) => action(id, 'reject'),
  start: (id: UUID) => action(id, 'start'),
  complete: (id: UUID) => action(id, 'complete'),
  cancel: (id: UUID) => action(id, 'cancel'),
  markPaymentPaid: (id: UUID) => action(id, 'payment/paid'),
  markPaymentNotConfirmed: (id: UUID) => action(id, 'payment/not-confirmed'),
}
