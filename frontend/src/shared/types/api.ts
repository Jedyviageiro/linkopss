export type UUID = string
export type ISODateTime = string

export interface PageMetadata {
  size: number
  number: number
  totalElements: number
  totalPages: number
}

export interface PageResponse<T> {
  content: T[]
  page: PageMetadata
}

export interface ApiErrorPayload {
  timestamp: ISODateTime
  status: number
  error: string
  message: string
  path: string
  validationErrors?: Record<string, string> | null
}

export interface MessageResponse {
  message: string
}

export interface PageQuery {
  page?: number
  size?: number
  sort?: string
}

export type QueryValue = string | number | boolean | null | undefined

export function toQueryString(params: Record<string, QueryValue>): string {
  const search = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      search.set(key, String(value))
    }
  })

  const query = search.toString()
  return query ? `?${query}` : ''
}
