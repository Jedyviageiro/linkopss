import type { ISODateTime, UUID } from '@/shared/types/api'

export type UserRole = 'CLIENT' | 'PROVIDER' | 'ADMIN'
export type UserStatus = 'ACTIVE' | 'SUSPENDED' | 'DEACTIVATED'

export interface User {
  id: UUID
  firstName: string
  lastName: string
  email: string
  phone: string | null
  role: UserRole
  status: UserStatus
  createdAt: ISODateTime
  updatedAt: ISODateTime
}

export interface UpdateUserRequest {
  firstName?: string
  lastName?: string
  phone?: string | null
}
