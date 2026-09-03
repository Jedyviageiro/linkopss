<script setup lang="ts">
import { ref } from 'vue'
import { authApi } from '../api/auth-api'
import { getRecoveryRequestErrorMessage } from '@/features/auth/errors/auth-error-messages'
import { useNotificationStore } from '@/shared/notifications/notification-store'
import recoveryHero from '@/assets/photos/recovery-side-page.png'

const email = ref('')
const notifications = useNotificationStore()
const isSuccess = ref(false)
const loading = ref(false)
const submittedEmail = ref('')

async function sendInstructions(isResend: boolean) {
  loading.value = true

  try {
    await authApi.forgotPassword(email.value)
    submittedEmail.value = email.value
    isSuccess.value = true
    if (isResend) notifications.success('Enviamos um novo link para o seu e-mail.', 'Instruções reenviadas!')
  } catch (error) {
    notifications.error(getRecoveryRequestErrorMessage(error), 'Não conseguimos enviar as instruções')
    if (!isResend) isSuccess.value = false
  } finally {
    loading.value = false
  }
}

function submit() {
  return sendInstructions(false)
}

function resend() {
  return sendInstructions(true)
}
</script>

<template>
  <main class="flex min-h-screen items-center justify-center bg-soft-background p-2 font-sans text-deep-navy">
    <div class="grid h-[min(580px,calc(100vh-32px))] min-h-[520px] w-full max-w-[900px] grid-cols-[46.5%_53.5%] overflow-hidden rounded-[10px] border border-linkops-slate-200 bg-white shadow-[0_4px_22px_rgba(15,23,42,0.06)] max-[860px]:h-auto max-[860px]:min-h-0 max-[860px]:grid-cols-1">
      <section class="relative h-full overflow-hidden bg-deep-navy max-[860px]:h-[290px]" aria-label="LinkOps — recuperação de palavra-passe">
        <img :src="recoveryHero" alt="Ponte iluminada sobre a água à noite" class="absolute inset-0 size-full object-cover object-center" />
        <div class="absolute inset-0 bg-[linear-gradient(180deg,rgba(15,23,42,0.04)_0%,rgba(15,23,42,0.2)_100%)]" aria-hidden="true"></div>
      </section>

      <section class="flex min-w-0 items-start justify-center px-9 py-8 max-[860px]:px-6 max-[860px]:py-9 max-[480px]:px-5">
        <div v-if="isSuccess" class="flex h-full w-full max-w-[430px] flex-col items-center justify-center text-center" role="status" aria-live="polite">
          <div class="mb-7 flex size-[88px] items-center justify-center rounded-full bg-green-50 text-linkops-green" aria-hidden="true">
            <svg class="size-10" viewBox="0 0 48 48" fill="none">
              <rect x="7" y="11" width="34" height="26" rx="2.5" stroke="currentColor" stroke-width="2.5" />
              <path d="m9 14 12.2 10a4.4 4.4 0 0 0 5.6 0L39 14" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </div>

          <h1 class="mb-3 text-h4 font-semibold tracking-[-0.01em]">Instruções enviadas!</h1>
          <p class="m-0 max-w-[340px] text-body-small leading-6 text-linkops-slate-500">
            Enviamos um link para redefinir sua senha<br />para <strong class="font-medium text-linkops-slate-700">{{ submittedEmail }}</strong>
          </p>
          <p class="mt-2 mb-0 text-caption text-linkops-slate-500">O link é válido por 5 minutos.</p>

          <div class="mt-7">
            <p class="m-0 text-body-small text-linkops-slate-500">Não recebeu o e-mail?</p>
            <button
              type="button"
              class="!min-h-0 !bg-transparent !p-0 text-body-small !font-semibold !text-linkops-green hover:underline disabled:no-underline"
              :disabled="loading"
              @click="resend"
            >
              {{ loading ? 'Reenviando…' : 'Reenviar instruções' }}
            </button>
          </div>

          <RouterLink to="/login" class="mt-14 text-body-small font-medium text-linkops-slate-700 hover:text-linkops-green">
            Voltar para o login
          </RouterLink>
        </div>

        <div v-else class="w-full max-w-[430px]">
          <RouterLink to="/" class="mb-3 inline-block text-h5 font-medium tracking-[-0.02em] text-deep-navy no-underline hover:no-underline" aria-label="LinkOps — página inicial">
            <span class="text-linkops-green">Link</span>Ops
          </RouterLink>

          <header class="mb-4">
            <h1 class="mb-1.5 text-h4 font-semibold tracking-[-0.01em]">Esqueceu sua senha?</h1>
            <p class="m-0 max-w-[330px] text-caption text-linkops-slate-500">Informe seu e-mail para receber as instruções de redefinição de senha.</p>
          </header>

          <form @submit.prevent="submit">
            <div class="mb-4">
              <label for="recovery-email" class="!mb-1.5 !block text-xs !font-semibold">E-mail</label>
              <div class="relative flex items-center">
                <svg class="pointer-events-none absolute left-[13px] size-4 text-linkops-slate-500" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <rect x="3" y="5" width="18" height="14" rx="2.5" stroke="currentColor" stroke-width="1.7" />
                  <path d="m4.5 7 6.2 5a2 2 0 0 0 2.6 0l6.2-5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
                <input id="recovery-email" v-model.trim="email" type="email" autocomplete="email" maxlength="255" placeholder="Digite seu e-mail" required class="!h-[38px] !w-full !rounded-md !border-linkops-slate-200 !bg-white !py-0 !pr-3 !pl-[38px] text-[13px] leading-5 text-deep-navy placeholder:text-[13px] placeholder:leading-5 placeholder:text-linkops-slate-500 focus:!border-linkops-slate-200 focus:!outline-none focus:!ring-0" />
              </div>
            </div>

            <button class="!h-[38px] !min-h-[38px] !w-full !rounded-md !bg-linkops-green text-body-base !font-semibold !text-white hover:!bg-deep-navy" type="submit" :disabled="loading">
              {{ loading ? 'Enviando…' : 'Enviar instruções' }}
            </button>

            <p class="mt-8 text-center text-body-small text-linkops-slate-700">
              <RouterLink to="/login" class="font-medium text-linkops-slate-700 hover:text-linkops-green">Voltar para o login</RouterLink>
            </p>
          </form>
        </div>
      </section>
    </div>
  </main>
</template>
