<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/features/auth/stores/auth-store'
import { useModalStore } from '@/shared/modals/modal-store'
import { useNotificationStore } from '@/shared/notifications/notification-store'

const auth = useAuthStore()
const router = useRouter()
const modals = useModalStore()
const notifications = useNotificationStore()

async function logout() {
  const confirmed = await modals.open({
    kind: 'warning',
    title: 'Terminar sessão?',
    message: 'Você precisará entrar novamente para acessar sua conta.',
    confirmLabel: 'Sair',
    cancelLabel: 'Cancelar',
  })
  if (!confirmed) return

  await auth.logout()
  await router.replace({ name: 'login' })
  notifications.info('Você saiu da sua conta com segurança.', 'Sessão terminada')
}
</script>

<template>
  <div class="app-shell">
    <header class="app-header">
      <RouterLink class="brand" to="/">LinkOps</RouterLink>
      <nav aria-label="Navegação principal">
        <RouterLink to="/services">Serviços</RouterLink>
        <RouterLink to="/providers">Prestadores</RouterLink>
        <RouterLink v-if="auth.isAuthenticated" to="/bookings">Pedidos</RouterLink>
        <RouterLink v-if="auth.isAuthenticated" to="/notifications">Notificações</RouterLink>
        <RouterLink v-if="auth.role === 'ADMIN'" to="/admin">Administração</RouterLink>
      </nav>
      <div class="header-actions">
        <RouterLink v-if="!auth.isAuthenticated" to="/login">Entrar</RouterLink>
        <RouterLink v-if="!auth.isAuthenticated" class="button-link" to="/register">Criar conta</RouterLink>
        <RouterLink v-if="auth.isAuthenticated" to="/profile">{{ auth.user?.firstName }}</RouterLink>
        <button v-if="auth.isAuthenticated" class="button-quiet" type="button" @click="logout">Sair</button>
      </div>
    </header>
    <main class="app-content">
      <RouterView />
    </main>
  </div>
</template>
