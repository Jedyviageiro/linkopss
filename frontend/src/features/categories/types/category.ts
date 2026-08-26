import type { ISODateTime, UUID } from '@/shared/types/api'

export interface Category {
  id: UUID
  name: string
  slug: string
  parentId: UUID | null
  active: boolean
  children: Category[]
  createdAt: ISODateTime
  updatedAt: ISODateTime
}

export interface CreateCategoryRequest { name: string; parentId?: UUID | null }
export interface UpdateCategoryRequest { name?: string; parentId?: UUID | null; active?: boolean }
