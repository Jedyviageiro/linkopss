import { env } from '@/config/env'
import { tokenStorage } from '@/shared/auth/token-storage'
import { ApiError } from './api-error'
import type { ApiErrorPayload } from '@/shared/types/api'

interface ApiRequestOptions extends Omit<RequestInit, 'body'> {
  body?: unknown
  auth?: boolean
  retryOnUnauthorized?: boolean
}

interface RefreshResponse {
  accessToken: string
  refreshToken: string
}

let refreshRequest: Promise<boolean> | null = null

function serializeBody(body: unknown): BodyInit | undefined {
  if (body === undefined || body === null) return undefined
  if (
    typeof body === 'string' ||
    body instanceof FormData ||
    body instanceof Blob ||
    body instanceof URLSearchParams
  ) {
    return body
  }
  return JSON.stringify(body)
}

async function parseBody(response: Response): Promise<unknown> {
  if (response.status === 204) return undefined
  const text = await response.text()
  if (!text) return undefined

  try {
    return JSON.parse(text)
  } catch {
    return text
  }
}

async function refreshTokens(): Promise<boolean> {
  const refreshToken = tokenStorage.getRefreshToken()
  if (!refreshToken) return false
  const persistent = tokenStorage.isPersistent()

  if (!refreshRequest) {
    refreshRequest = fetch(`${env.apiBaseUrl}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      body: JSON.stringify({ refreshToken }),
    })
      .then(async (response) => {
        if (!response.ok) return false
        const session = (await response.json()) as RefreshResponse
        tokenStorage.set(session.accessToken, session.refreshToken, persistent)
        return true
      })
      .catch(() => false)
      .finally(() => {
        refreshRequest = null
      })
  }

  return refreshRequest
}

export async function apiRequest<T>(
  path: string,
  options: ApiRequestOptions = {},
): Promise<T> {
  const { auth = true, retryOnUnauthorized = true, body, ...requestInit } = options
  const headers = new Headers(requestInit.headers)
  headers.set('Accept', 'application/json')

  const serializedBody = serializeBody(body)
  if (serializedBody && !(serializedBody instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const accessToken = tokenStorage.getAccessToken()
  if (auth && accessToken) headers.set('Authorization', `Bearer ${accessToken}`)

  let response: Response
  try {
    response = await fetch(`${env.apiBaseUrl}${path}`, {
      ...requestInit,
      headers,
      body: serializedBody,
    })
  } catch {
    throw new ApiError(0, 'Não conseguimos concluir o pedido. Verifique sua ligação e tente novamente.')
  }

  if (response.status === 401 && auth && retryOnUnauthorized && (await refreshTokens())) {
    return apiRequest<T>(path, { ...options, retryOnUnauthorized: false })
  }

  const payload = await parseBody(response)
  if (!response.ok) {
    if (response.status === 401 && auth) tokenStorage.clear()
    const error = typeof payload === 'object' && payload ? (payload as ApiErrorPayload) : undefined
    throw new ApiError(
      response.status,
      error?.message ?? `A requisição falhou com o código ${response.status}.`,
      error,
    )
  }

  return payload as T
}
