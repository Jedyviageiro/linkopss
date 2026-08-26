import type { User, UserRole } from '@/features/users/types/user'

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  firstName: string
  lastName: string
  email: string
  phone?: string
  password: string
  confirmPassword: string
  role: Exclude<UserRole, 'ADMIN'>
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: 'Bearer'
  expiresIn: number
  user: User
}

export interface ResetPasswordRequest {
  token: string
  password: string
  confirmPassword: string
}
