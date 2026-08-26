<script setup lang="ts">
import { ref } from 'vue'
import { authApi } from '../api/auth-api'
import { getErrorMessage } from '@/shared/api/api-error'

const email = ref('')
const message = ref('')

async function submit() {
  try {
    message.value = (await authApi.forgotPassword(email.value)).message
  } catch (error) {
    message.value = getErrorMessage(error)
  }
}
</script>

<template>
  <main class="auth-page">
    <form class="auth-card" @submit.prevent="submit">
      <RouterLink to="/" class="brand">LinkOps</RouterLink>
      <h1>Recuperar palavra-passe</h1>
      <label>E-mail <input v-model.trim="email" type="email" required /></label>
      <p v-if="message" class="notice" role="status">{{ message }}</p>
      <button type="submit">Enviar instruções</button>
      <RouterLink to="/login">Voltar ao login</RouterLink>
    </form>
  </main>
</template>
