import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { tokenStorage } from '@/shared/auth/token-storage'
import { usersApi } from '@/features/users/api/users-api'
import { authApi } from '../api/auth-api'
import type { LoginRequest, RegisterRequest } from '../types/auth'
import type { User } from '@/features/users/types/user'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const initialized = ref(false)
  const loading = ref(false)

  const isAuthenticated = computed(() => user.value !== null)
  const role = computed(() => user.value?.role ?? null)

  async function initialize() {
    if (initialized.value) return
    try {
      if (tokenStorage.getAccessToken() || tokenStorage.getRefreshToken()) {
        user.value = await usersApi.me()
      }
    } catch {
      tokenStorage.clear()
      user.value = null
    } finally {
      initialized.value = true
    }
  }

  async function login(payload: LoginRequest) {
    loading.value = true
    try {
      const session = await authApi.login(payload)
      tokenStorage.set(session.accessToken, session.refreshToken)
      user.value = session.user
      return session.user
    } finally {
      loading.value = false
    }
  }

  async function register(payload: RegisterRequest) {
    loading.value = true
    try {
      const session = await authApi.register(payload)
      tokenStorage.set(session.accessToken, session.refreshToken)
      user.value = session.user
      return session.user
    } finally {
      loading.value = false
    }
  }

  function logout() {
    tokenStorage.clear()
    user.value = null
  }

  return { user, role, initialized, loading, isAuthenticated, initialize, login, register, logout }
})
