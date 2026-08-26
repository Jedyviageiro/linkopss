const rawApiBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim()

if (!rawApiBaseUrl) {
  throw new Error('VITE_API_BASE_URL não está configurado.')
}

export const env = Object.freeze({
  apiBaseUrl: rawApiBaseUrl.replace(/\/$/, ''),
})
