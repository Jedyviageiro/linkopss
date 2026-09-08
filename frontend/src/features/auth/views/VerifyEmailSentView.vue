<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { authApi } from '../api/auth-api'
import { getEmailVerificationErrorMessage } from '../errors/auth-error-messages'
import { useNotificationStore } from '@/shared/notifications/notification-store'
import recoveryHero from '@/assets/photos/recovery-side-page.png'

const route = useRoute()
const notifications = useNotificationStore()
const loading = ref(false)
const email = computed(() => typeof route.query.email === 'string' ? route.query.email : '')

async function resend() {
  if (!email.value) {
    notifications.error('Volte ao cadastro e informe seu e-mail novamente.', 'E-mail não encontrado')
    return
  }
  loading.value = true
  try {
    await authApi.resendEmailVerification(email.value)
    notifications.success('Enviamos uma nova confirmação para seu e-mail.', 'E-mail reenviado!')
  } catch (error) {
    notifications.error(getEmailVerificationErrorMessage(error), 'Não conseguimos reenviar')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="flex min-h-screen items-center justify-center bg-soft-background p-3 font-sans text-deep-navy">
    <div class="grid h-[min(580px,calc(100vh-24px))] min-h-[520px] w-full max-w-[900px] grid-cols-[46.5%_53.5%] overflow-hidden rounded-[10px] border border-linkops-slate-200 bg-white shadow-[0_4px_22px_rgba(15,23,42,0.06)] max-[860px]:h-auto max-[860px]:min-h-0 max-[860px]:grid-cols-1">
      <section class="relative h-full overflow-hidden bg-deep-navy max-[860px]:h-[260px]" aria-label="Confirmação de e-mail LinkOps">
        <img :src="recoveryHero" alt="Ponte iluminada sobre a água à noite" class="absolute inset-0 size-full object-cover object-center" />
        <div class="absolute inset-0 bg-deep-navy/10" aria-hidden="true"></div>
      </section>

      <section class="flex min-w-0 items-center justify-center px-9 py-9 max-[480px]:px-5">
        <div class="flex w-full max-w-[390px] flex-col items-center text-center" role="status" aria-live="polite">
          <div class="mb-7 flex size-[88px] items-center justify-center rounded-full bg-green-50 text-linkops-green" aria-hidden="true">
            <svg class="size-10" viewBox="0 0 48 48" fill="none"><rect x="7" y="11" width="34" height="26" rx="3" stroke="currentColor" stroke-width="2.5"/><path d="m9 14 12.2 10a4.4 4.4 0 0 0 5.6 0L39 14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/><path d="M34 8v7m-3.5-3.5h7" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/></svg>
          </div>
          <h1 class="mb-3 text-h4 font-semibold tracking-[-0.01em]">Confirme seu e-mail</h1>
          <p class="m-0 max-w-[340px] text-body-small leading-6 text-linkops-slate-500">Enviamos um link de confirmação para<br /><strong class="font-medium text-linkops-slate-700">{{ email || 'o e-mail informado' }}</strong></p>
          <p class="mt-2 mb-0 text-caption text-linkops-slate-500">Abra o link dentro de 30 minutos para continuar.</p>

          <div class="mt-8">
            <p class="m-0 text-body-small text-linkops-slate-500">Não recebeu o e-mail?</p>
            <button type="button" class="!min-h-0 !bg-transparent !p-0 !text-[13px] !font-semibold !text-linkops-green hover:underline" :disabled="loading" @click="resend">{{ loading ? 'Reenviando…' : 'Reenviar confirmação' }}</button>
          </div>
          <RouterLink to="/login" class="mt-12 text-body-small font-medium text-linkops-slate-700 hover:text-linkops-green">Voltar para o login</RouterLink>
        </div>
      </section>
    </div>
  </main>
</template>
