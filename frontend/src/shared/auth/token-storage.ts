const ACCESS_TOKEN_KEY = 'linkops.accessToken'
const REFRESH_TOKEN_KEY = 'linkops.refreshToken'

function localStorage(): Storage | null {
  return typeof window === 'undefined' ? null : window.localStorage
}

function sessionStorage(): Storage | null {
  return typeof window === 'undefined' ? null : window.sessionStorage
}

function getItem(key: string): string | null {
  return localStorage()?.getItem(key) ?? sessionStorage()?.getItem(key) ?? null
}

export const tokenStorage = {
  getAccessToken: () => getItem(ACCESS_TOKEN_KEY),
  getRefreshToken: () => getItem(REFRESH_TOKEN_KEY),
  isPersistent: () => localStorage()?.getItem(REFRESH_TOKEN_KEY) !== null,

  set(accessToken: string, refreshToken: string, persistent = true) {
    this.clear()
    const target = persistent ? localStorage() : sessionStorage()
    target?.setItem(ACCESS_TOKEN_KEY, accessToken)
    target?.setItem(REFRESH_TOKEN_KEY, refreshToken)
  },

  clear() {
    for (const target of [localStorage(), sessionStorage()]) {
      target?.removeItem(ACCESS_TOKEN_KEY)
      target?.removeItem(REFRESH_TOKEN_KEY)
    }
  },
}
