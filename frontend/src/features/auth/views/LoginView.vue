<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth-store'
import { getErrorMessage } from '@/shared/api/api-error'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const email = ref('')
const password = ref('')
const errorMessage = ref('')

async function submit() {
  errorMessage.value = ''
  try {
    await auth.login({ email: email.value, password: password.value })
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
    await router.replace(redirect)
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  }
}
</script>

<template>
  <main class="auth-page">
    <form class="auth-card" @submit.prevent="submit">
      <RouterLink to="/" class="brand">LinkOps</RouterLink>
      <h1>Entrar</h1>
      <p class="muted">Aceda à sua conta de cliente, prestador ou administrador.</p>

      <label>
        E-mail
        <input v-model.trim="email" type="email" autocomplete="email" required />
      </label>
      <label>
        Palavra-passe
        <input v-model="password" type="password" autocomplete="current-password" required />
      </label>

      <p v-if="errorMessage" class="form-error" role="alert">{{ errorMessage }}</p>
      <button type="submit" :disabled="auth.loading">
        {{ auth.loading ? 'A entrar…' : 'Entrar' }}
      </button>

      <div class="auth-links">
        <RouterLink to="/forgot-password">Esqueci a palavra-passe</RouterLink>
        <RouterLink to="/register">Criar conta</RouterLink>
      </div>
    </form>
  </main>
</template>
