<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authApi } from '../api/auth-api'
import { getErrorMessage } from '@/shared/api/api-error'

const route = useRoute()
const router = useRouter()
const password = ref('')
const confirmPassword = ref('')
const message = ref('')

async function submit() {
  const token = typeof route.query.token === 'string' ? route.query.token : ''
  if (!token) {
    message.value = 'A ligação de recuperação é inválida.'
    return
  }
  try {
    await authApi.resetPassword({ token, password: password.value, confirmPassword: confirmPassword.value })
    await router.replace('/login')
  } catch (error) {
    message.value = getErrorMessage(error)
  }
}
</script>

<template>
  <main class="auth-page">
    <form class="auth-card" @submit.prevent="submit">
      <h1>Nova palavra-passe</h1>
      <label>Palavra-passe <input v-model="password" type="password" minlength="8" maxlength="72" required /></label>
      <label>Confirmar <input v-model="confirmPassword" type="password" minlength="8" maxlength="72" required /></label>
      <p v-if="message" class="form-error" role="alert">{{ message }}</p>
      <button type="submit">Guardar palavra-passe</button>
    </form>
  </main>
</template>
