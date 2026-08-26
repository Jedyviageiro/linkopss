import { apiRequest } from '@/shared/api/client'
import { toQueryString, type PageQuery, type PageResponse, type UUID } from '@/shared/types/api'
import type { User } from '@/features/users/types/user'
import type { Provider } from '@/features/providers/types/provider'
import type { ServiceOffering } from '@/features/services/types/service'
import type { Category, CreateCategoryRequest, UpdateCategoryRequest } from '@/features/categories/types/category'

export const adminApi = {
  users: (query: PageQuery = {}) => apiRequest<PageResponse<User>>(`/admin/users${toQueryString({ ...query })}`),
  suspendUser: (id: UUID) => apiRequest<User>(`/admin/users/${id}/suspend`, { method: 'PATCH' }),
  reactivateUser: (id: UUID) => apiRequest<User>(`/admin/users/${id}/reactivate`, { method: 'PATCH' }),

  providers: (query: PageQuery = {}) => apiRequest<PageResponse<Provider>>(`/admin/providers${toQueryString({ ...query })}`),
  verifyProvider: (id: UUID) => apiRequest<Provider>(`/admin/providers/${id}/verify`, { method: 'PATCH' }),
  rejectProvider: (id: UUID, reason: string) => apiRequest<Provider>(`/admin/providers/${id}/reject-verification`, { method: 'PATCH', body: { reason } }),
  revokeProviderVerification: (id: UUID, reason: string) => apiRequest<Provider>(`/admin/providers/${id}/revoke-verification`, { method: 'PATCH', body: { reason } }),

  services: (query: PageQuery = {}) => apiRequest<PageResponse<ServiceOffering>>(`/admin/services${toQueryString({ ...query })}`),
  deactivateService: (id: UUID) => apiRequest<void>(`/admin/services/${id}`, { method: 'DELETE' }),

  createCategory: (payload: CreateCategoryRequest) => apiRequest<Category>('/admin/categories', { method: 'POST', body: payload }),
  updateCategory: (id: UUID, payload: UpdateCategoryRequest) => apiRequest<Category>(`/admin/categories/${id}`, { method: 'PATCH', body: payload }),
  deactivateCategory: (id: UUID) => apiRequest<void>(`/admin/categories/${id}`, { method: 'DELETE' }),
}
