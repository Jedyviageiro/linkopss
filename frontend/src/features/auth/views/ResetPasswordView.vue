<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { authApi } from '../api/auth-api'
import { getPasswordResetErrorMessage } from '@/features/auth/errors/auth-error-messages'
import { useNotificationStore } from '@/shared/notifications/notification-store'
import { getPasswordValidationMessage } from '@/shared/validation/password'
import recoveryHero from '@/assets/photos/recovery-side-page.png'

const route = useRoute()
const notifications = useNotificationStore()
const password = ref('')
const confirmPassword = ref('')
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const loading = ref(false)
const isSuccess = ref(false)
const resetToken = computed(() => typeof route.query.token === 'string' ? route.query.token : '')
const hasValidResetToken = computed(() => /^[A-Za-z0-9_-]{43}$/.test(resetToken.value))
const passwordsMismatch = computed(() => (
  confirmPassword.value.length > 0 && password.value !== confirmPassword.value
))

async function submit() {
  if (!hasValidResetToken.value) {
    notifications.error('Solicite um novo link de recuperação e tente novamente.', 'Link inválido')
    return
  }
  if (password.value !== confirmPassword.value) {
    notifications.error('Digite a mesma senha nos dois campos.', 'As senhas não coincidem')
    return
  }
  const passwordValidationMessage = getPasswordValidationMessage(password.value)
  if (passwordValidationMessage) {
    notifications.error(passwordValidationMessage, 'Senha inválida')
    return
  }

  loading.value = true
  try {
    await authApi.resetPassword({ token: resetToken.value, password: password.value, confirmPassword: confirmPassword.value })
    isSuccess.value = true
    notifications.success('Sua nova senha já pode ser usada para entrar.', 'Senha redefinida!')
  } catch (error) {
    notifications.error(getPasswordResetErrorMessage(error), 'Não conseguimos alterar sua senha')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="flex min-h-screen items-center justify-center bg-soft-background p-2 font-sans text-deep-navy">
    <div class="grid h-[min(580px,calc(100vh-32px))] min-h-[520px] w-full max-w-[900px] grid-cols-[46.5%_53.5%] overflow-hidden rounded-[10px] border border-linkops-slate-200 bg-white shadow-[0_4px_22px_rgba(15,23,42,0.06)] max-[860px]:h-auto max-[860px]:min-h-0 max-[860px]:grid-cols-1">
      <section class="relative h-full overflow-hidden bg-deep-navy max-[860px]:h-[290px]" aria-label="LinkOps — redefinição de senha">
        <img :src="recoveryHero" alt="Ponte iluminada sobre a água à noite" class="absolute inset-0 size-full object-cover object-center" />
        <div class="absolute inset-0 bg-[linear-gradient(180deg,rgba(15,23,42,0.04)_0%,rgba(15,23,42,0.2)_100%)]" aria-hidden="true"></div>
      </section>

      <section class="flex min-w-0 items-start justify-center px-9 py-8 max-[860px]:px-6 max-[860px]:py-9 max-[480px]:px-5">
        <div v-if="isSuccess" class="flex h-full w-full max-w-[430px] flex-col items-center justify-center text-center" role="status" aria-live="polite">
          <div class="mb-7 flex size-[88px] items-center justify-center rounded-full bg-green-50 text-linkops-green" aria-hidden="true">
            <svg class="size-10" viewBox="0 0 48 48" fill="none">
              <circle cx="24" cy="24" r="15" stroke="currentColor" stroke-width="2.5" />
              <path d="m17.5 24.5 4.2 4.2 9-9" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </div>

          <h1 class="mb-3 max-w-[320px] text-h4 font-semibold tracking-[-0.01em]">Senha redefinida<br />com sucesso!</h1>
          <p class="m-0 max-w-[340px] text-body-small leading-6 text-linkops-slate-500">
            Sua senha foi alterada. Agora você pode<br />fazer login com sua nova senha.
          </p>

          <RouterLink to="/login" class="mt-16 inline-flex !h-[40px] !w-full max-w-[340px] items-center justify-center rounded-md !bg-linkops-green text-body-base font-semibold !text-white hover:!bg-deep-navy hover:!text-white">
            Ir para o login
          </RouterLink>
        </div>

        <div v-else-if="!hasValidResetToken" class="flex h-full w-full max-w-[430px] flex-col items-center justify-center text-center" role="alert">
          <div class="mb-7 flex size-[88px] items-center justify-center rounded-full bg-red-50 text-red-500" aria-hidden="true">
            <svg class="size-10" viewBox="0 0 48 48" fill="none">
              <circle cx="24" cy="24" r="15" stroke="currentColor" stroke-width="2.5" />
              <path d="m19 19 10 10m0-10L19 29" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" />
            </svg>
          </div>
          <h1 class="mb-3 text-h4 font-semibold tracking-[-0.01em]">Link inválido</h1>
          <p class="m-0 max-w-[340px] text-body-small leading-6 text-linkops-slate-500">
            Este link de recuperação está incompleto ou é inválido. Solicite novas instruções.
          </p>
          <RouterLink to="/forgot-password" class="mt-12 inline-flex !h-[40px] !w-full max-w-[340px] items-center justify-center rounded-md !bg-linkops-green text-body-base font-semibold !text-white hover:!bg-deep-navy hover:!text-white">
            Solicitar novo link
          </RouterLink>
        </div>

        <div v-else class="w-full max-w-[430px]">
          <RouterLink to="/" class="mb-5 inline-block text-h5 font-medium tracking-[-0.02em] text-deep-navy no-underline hover:no-underline" aria-label="LinkOps — página inicial">
            <span class="text-linkops-green">Link</span>Ops
          </RouterLink>

          <header class="mb-7">
            <h1 class="mb-1.5 text-h4 font-semibold tracking-[-0.01em]">Redefinir sua senha</h1>
            <p class="m-0 text-caption text-linkops-slate-500">Digite sua nova senha abaixo.</p>
          </header>

          <form @submit.prevent="submit">
            <div class="mb-4">
              <label for="new-password" class="!mb-1.5 !block text-xs !font-semibold">Nova senha</label>
              <div class="relative flex items-center">
                <svg class="pointer-events-none absolute left-[13px] size-4 text-linkops-slate-500" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <rect x="4" y="10" width="16" height="11" rx="2.5" stroke="currentColor" stroke-width="1.7" />
                  <path d="M8 10V7a4 4 0 0 1 8 0v3M12 14.5v2" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" />
                </svg>
                <input id="new-password" v-model="password" :type="showPassword ? 'text' : 'password'" autocomplete="new-password" minlength="8" maxlength="72" placeholder="Digite sua nova senha" required class="!h-[40px] !w-full !rounded-md !border-linkops-slate-200 !bg-white !py-0 !pr-10 !pl-[38px] text-[13px] leading-5 text-deep-navy outline-none placeholder:text-[13px] placeholder:text-linkops-slate-500 focus:!border-linkops-slate-200 focus:!outline-none focus:!ring-0" />
                <button type="button" class="absolute right-[13px] !min-h-0 !w-[18px] !rounded-none !bg-transparent !p-0 !text-linkops-slate-500" :aria-label="showPassword ? 'Ocultar nova senha' : 'Mostrar nova senha'" :aria-pressed="showPassword" @click="showPassword = !showPassword">
                  <svg class="size-[18px]" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                    <path d="M1.5 12S5 5 12 5s10.5 7 10.5 7-3.5 7-10.5 7S1.5 12 1.5 12Z" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round" />
                    <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.7" />
                  </svg>
                </button>
              </div>
            </div>

            <div class="mb-5">
              <label for="confirm-new-password" class="!mb-1.5 !block text-xs !font-semibold">Confirmar nova senha</label>
              <div class="relative flex items-center">
                <svg class="pointer-events-none absolute left-[13px] size-4 text-linkops-slate-500" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <rect x="4" y="10" width="16" height="11" rx="2.5" stroke="currentColor" stroke-width="1.7" />
                  <path d="M8 10V7a4 4 0 0 1 8 0v3M12 14.5v2" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" />
                </svg>
                <input id="confirm-new-password" v-model="confirmPassword" :type="showConfirmPassword ? 'text' : 'password'" autocomplete="new-password" minlength="8" maxlength="72" placeholder="Confirme sua nova senha" required class="!h-[40px] !w-full !rounded-md !bg-white !py-0 !pr-10 !pl-[38px] text-[13px] leading-5 text-deep-navy outline-none placeholder:text-[13px] placeholder:text-linkops-slate-500 focus:!outline-none focus:!ring-0" :class="passwordsMismatch ? '!border-red-500 focus:!border-red-500' : '!border-linkops-slate-200 focus:!border-linkops-slate-200'" :aria-invalid="passwordsMismatch" aria-describedby="reset-password-match" />
                <button type="button" class="absolute right-[13px] !min-h-0 !w-[18px] !rounded-none !bg-transparent !p-0 !text-linkops-slate-500" :aria-label="showConfirmPassword ? 'Ocultar confirmação da senha' : 'Mostrar confirmação da senha'" :aria-pressed="showConfirmPassword" @click="showConfirmPassword = !showConfirmPassword">
                  <svg class="size-[18px]" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                    <path d="M1.5 12S5 5 12 5s10.5 7 10.5 7-3.5 7-10.5 7S1.5 12 1.5 12Z" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round" />
                    <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.7" />
                  </svg>
                </button>
              </div>
              <p v-if="passwordsMismatch" id="reset-password-match" class="mt-1 mb-0 text-[11px] leading-4 text-red-600">As senhas não coincidem.</p>
            </div>

            <button class="!h-[40px] !min-h-[40px] !w-full !rounded-md !bg-linkops-green text-body-base !font-semibold !text-white hover:!bg-deep-navy" type="submit" :disabled="loading">
              {{ loading ? 'Redefinindo…' : 'Redefinir senha' }}
            </button>
          </form>
        </div>
      </section>
    </div>
  </main>
</template>
