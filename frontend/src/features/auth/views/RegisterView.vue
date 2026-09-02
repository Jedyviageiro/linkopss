<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth-store'
import { getErrorMessage } from '@/shared/api/api-error'
import type { RegisterRequest } from '../types/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const errorMessage = ref('')
const form = reactive<RegisterRequest>({
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  role: route.query.role === 'PROVIDER' ? 'PROVIDER' : 'CLIENT',
})

async function submit() {
  errorMessage.value = ''
  try {
    const user = await auth.register(form)
    await router.replace(user.role === 'PROVIDER' ? '/provider/profile' : '/dashboard')
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  }
}
</script>

<template>
  <main class="auth-page">
    <form class="auth-card auth-card--wide" @submit.prevent="submit">
      <RouterLink to="/" class="brand">LinkOps</RouterLink>
      <h1>Criar conta</h1>
      <div class="form-grid">
        <label>Nome <input v-model.trim="form.firstName" required maxlength="100" /></label>
        <label>Apelido <input v-model.trim="form.lastName" required maxlength="100" /></label>
        <label class="full">E-mail <input v-model.trim="form.email" type="email" required /></label>
        <label class="full">Telefone <input v-model.trim="form.phone" type="tel" maxlength="50" /></label>
        <label>
          Palavra-passe
          <input v-model="form.password" type="password" minlength="8" maxlength="72" required />
        </label>
        <label>
          Confirmar palavra-passe
          <input v-model="form.confirmPassword" type="password" minlength="8" maxlength="72" required />
        </label>
        <label class="full">
          Tipo de conta
          <select v-model="form.role">
            <option value="CLIENT">Cliente</option>
            <option value="PROVIDER">Prestador de serviços</option>
          </select>
        </label>
      </div>
      <p v-if="errorMessage" class="form-error" role="alert">{{ errorMessage }}</p>
      <button type="submit" :disabled="auth.loading">
        {{ auth.loading ? 'A criar…' : 'Criar conta' }}
      </button>
      <RouterLink to="/login">Já tenho uma conta</RouterLink>
    </form>
  </main>
</template>
