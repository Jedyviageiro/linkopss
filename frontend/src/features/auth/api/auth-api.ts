import { apiRequest } from '@/shared/api/client'
import type { MessageResponse } from '@/shared/types/api'
import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  ResetPasswordRequest,
} from '../types/auth'

export const authApi = {
  register: (payload: RegisterRequest) =>
    apiRequest<AuthResponse>('/auth/register', { method: 'POST', body: payload, auth: false }),
  login: (payload: LoginRequest) =>
    apiRequest<AuthResponse>('/auth/login', { method: 'POST', body: payload, auth: false }),
  logout: () => apiRequest<void>('/auth/logout', { method: 'POST' }),
  forgotPassword: (email: string) =>
    apiRequest<MessageResponse>('/auth/forgot-password', {
      method: 'POST',
      body: { email },
      auth: false,
    }),
  resetPassword: (payload: ResetPasswordRequest) =>
    apiRequest<MessageResponse>('/auth/reset-password', {
      method: 'POST',
      body: payload,
      auth: false,
    }),
}
