import type { ApiErrorPayload } from '@/shared/types/api'

export class ApiError extends Error {
  readonly status: number
  readonly title: string
  readonly path?: string
  readonly validationErrors: Record<string, string>

  constructor(status: number, message: string, payload?: Partial<ApiErrorPayload>) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.title = payload?.error ?? 'Erro na requisição'
    this.path = payload?.path
    this.validationErrors = payload?.validationErrors ?? {}
  }
}

export function getErrorMessage(error: unknown): string {
  if (error instanceof ApiError || error instanceof Error) return error.message
  return 'Ocorreu um erro inesperado. Tente novamente.'
}
