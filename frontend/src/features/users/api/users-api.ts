import { apiRequest } from '@/shared/api/client'
import type { UpdateUserRequest, User } from '../types/user'

export const usersApi = {
  me: () => apiRequest<User>('/users/me'),
  updateMe: (payload: UpdateUserRequest) =>
    apiRequest<User>('/users/me', { method: 'PATCH', body: payload }),
}
