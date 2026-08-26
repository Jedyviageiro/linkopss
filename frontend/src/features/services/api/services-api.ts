import { apiRequest } from '@/shared/api/client'
import { toQueryString, type PageQuery, type PageResponse, type UUID } from '@/shared/types/api'
import type { CreateServiceRequest, ServiceOffering, ServiceQuery, UpdateServiceRequest } from '../types/service'

export const servicesApi = {
  list: (query: ServiceQuery = {}) => apiRequest<PageResponse<ServiceOffering>>(`/services${toQueryString({ ...query })}`, { auth: false }),
  get: (id: UUID) => apiRequest<ServiceOffering>(`/services/${id}`, { auth: false }),
  byProvider: (providerId: UUID, query: PageQuery = {}) => apiRequest<PageResponse<ServiceOffering>>(`/providers/${providerId}/services${toQueryString({ ...query })}`, { auth: false }),
  create: (payload: CreateServiceRequest) => apiRequest<ServiceOffering>('/services', { method: 'POST', body: payload }),
  update: (id: UUID, payload: UpdateServiceRequest) => apiRequest<ServiceOffering>(`/services/${id}`, { method: 'PATCH', body: payload }),
  deactivate: (id: UUID) => apiRequest<void>(`/services/${id}`, { method: 'DELETE' }),
}
