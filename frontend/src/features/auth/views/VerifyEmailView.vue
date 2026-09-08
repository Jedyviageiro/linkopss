<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth-store'
import { getEmailVerificationErrorMessage } from '../errors/auth-error-messages'
import recoveryHero from '@/assets/photos/recovery-side-page.png'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const failed = ref(false)
const errorMessage = ref('')

async function verify() {
  const token = typeof route.query.token === 'string' ? route.query.token : ''
  if (!token) {
    failed.value = true
    errorMessage.value = 'Este link está incompleto. Solicite uma nova confirmação para continuar.'
    return
  }
  try {
    const user = await auth.verifyEmail(token)
    await router.replace(user.role === 'PROVIDER' ? '/onboarding/provider' : '/onboarding/client')
  } catch (error) {
    failed.value = true
    errorMessage.value = getEmailVerificationErrorMessage(error)
  }
}

onMounted(verify)
</script>

<template>
  <main class="flex min-h-screen items-center justify-center bg-soft-background p-3 font-sans text-deep-navy">
    <div class="grid h-[min(580px,calc(100vh-24px))] min-h-[520px] w-full max-w-[900px] grid-cols-[46.5%_53.5%] overflow-hidden rounded-[10px] border border-linkops-slate-200 bg-white shadow-[0_4px_22px_rgba(15,23,42,0.06)] max-[860px]:h-auto max-[860px]:min-h-0 max-[860px]:grid-cols-1">
      <section class="relative h-full overflow-hidden bg-deep-navy max-[860px]:h-[260px]"><img :src="recoveryHero" alt="Ponte iluminada sobre a água à noite" class="absolute inset-0 size-full object-cover object-center" /></section>
      <section class="flex min-w-0 items-center justify-center px-9 py-9 max-[480px]:px-5">
        <div class="flex w-full max-w-[390px] flex-col items-center text-center" role="status" aria-live="polite">
          <template v-if="!failed">
            <div class="mb-7 flex size-[88px] items-center justify-center rounded-full bg-green-50 text-linkops-green"><svg class="size-9 animate-pulse" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8"/><path d="m8 12 2.5 2.5L16 9" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg></div>
            <h1 class="mb-3 text-h4 font-semibold">Confirmando seu e-mail…</h1>
            <p class="m-0 text-body-small text-linkops-slate-500">Só um momento. Estamos preparando sua conta.</p>
          </template>
          <template v-else>
            <div class="mb-7 flex size-[88px] items-center justify-center rounded-full bg-red-50 text-red-600"><svg class="size-9" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8"/><path d="m9 9 6 6m0-6-6 6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg></div>
            <h1 class="mb-3 text-h4 font-semibold">Não conseguimos confirmar</h1>
            <p class="m-0 max-w-[330px] text-body-small leading-6 text-linkops-slate-500">{{ errorMessage }}</p>
            <RouterLink to="/login" class="mt-10 text-body-small font-semibold text-linkops-green">Ir para o login</RouterLink>
          </template>
        </div>
      </section>
    </div>
  </main>
</template>
