import { apiRequest } from '@/shared/api/client'
import { toQueryString, type PageResponse, type UUID } from '@/shared/types/api'
import type { Provider, ProviderProfileRequest, ProviderQuery, UpdateProviderProfileRequest } from '../types/provider'

export const providersApi = {
  list: (query: ProviderQuery = {}) => apiRequest<PageResponse<Provider>>(`/providers${toQueryString({ ...query })}`, { auth: false }),
  get: (id: UUID) => apiRequest<Provider>(`/providers/${id}`, { auth: false }),
  me: () => apiRequest<Provider>('/providers/me'),
  createProfile: (payload: ProviderProfileRequest) => apiRequest<Provider>('/providers/profile', { method: 'POST', body: payload }),
  updateProfile: (payload: UpdateProviderProfileRequest) => apiRequest<Provider>('/providers/me', { method: 'PATCH', body: payload }),
  requestVerification: () => apiRequest<Provider>('/providers/me/verification', { method: 'POST' }),
}
