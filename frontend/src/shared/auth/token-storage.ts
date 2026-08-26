const ACCESS_TOKEN_KEY = 'linkops.accessToken'
const REFRESH_TOKEN_KEY = 'linkops.refreshToken'

function storage(): Storage | null {
  return typeof window === 'undefined' ? null : window.localStorage
}

export const tokenStorage = {
  getAccessToken: () => storage()?.getItem(ACCESS_TOKEN_KEY) ?? null,
  getRefreshToken: () => storage()?.getItem(REFRESH_TOKEN_KEY) ?? null,

  set(accessToken: string, refreshToken: string) {
    storage()?.setItem(ACCESS_TOKEN_KEY, accessToken)
    storage()?.setItem(REFRESH_TOKEN_KEY, refreshToken)
  },

  clear() {
    storage()?.removeItem(ACCESS_TOKEN_KEY)
    storage()?.removeItem(REFRESH_TOKEN_KEY)
  },
}
