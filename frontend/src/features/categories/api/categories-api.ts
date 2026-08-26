import { apiRequest } from '@/shared/api/client'
import type { UUID } from '@/shared/types/api'
import type { Category } from '../types/category'

export const categoriesApi = {
  list: () => apiRequest<Category[]>('/categories', { auth: false }),
  get: (id: UUID) => apiRequest<Category>(`/categories/${id}`, { auth: false }),
}
