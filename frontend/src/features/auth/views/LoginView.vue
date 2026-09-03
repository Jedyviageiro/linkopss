<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth-store'
import { getLoginErrorMessage } from '@/features/auth/errors/auth-error-messages'
import { useNotificationStore } from '@/shared/notifications/notification-store'
import loginHero from '@/assets/photos/man-login-page.png'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const notifications = useNotificationStore()
const email = ref('')
const password = ref('')
const rememberMe = ref(false)
const showPassword = ref(false)
const rememberedEmailKey = 'linkops.rememberedEmail'

onMounted(() => {
  const rememberedEmail = window.localStorage.getItem(rememberedEmailKey)
  if (rememberedEmail) {
    email.value = rememberedEmail
    rememberMe.value = true
  }
})

async function submit() {
  try {
    await auth.login({ email: email.value, password: password.value }, rememberMe.value)
    if (rememberMe.value) window.localStorage.setItem(rememberedEmailKey, email.value)
    else window.localStorage.removeItem(rememberedEmailKey)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
    notifications.success('Você entrou na sua conta com sucesso.', 'Bem-vindo!')
    await router.replace(redirect)
  } catch (error) {
    notifications.error(getLoginErrorMessage(error), 'Não conseguimos entrar')
  }
}

</script>

<template>
  <main class="flex min-h-screen items-center justify-center bg-soft-background p-3 font-sans text-deep-navy">
    <div class="grid h-[min(600px,calc(100vh-24px))] min-h-[560px] w-full max-w-[1000px] grid-cols-[43%_57%] overflow-hidden rounded-[10px] border border-linkops-slate-200 bg-white shadow-[0_4px_22px_rgba(15,23,42,0.06)] max-[900px]:h-auto max-[900px]:min-h-0 max-[900px]:grid-cols-1">
    <section
      class="relative h-full overflow-hidden bg-deep-navy max-[900px]:h-[280px]"
      aria-label="LinkOps para prestadores de serviços"
    >
      <img
        :src="loginHero"
        alt="Prestador de serviços sorrindo enquanto utiliza um tablet"
        class="absolute inset-0 size-full object-cover object-[center_30%]"
      />
      <div
        class="absolute inset-0 bg-[linear-gradient(180deg,rgba(10,12,10,0.15)_0%,rgba(10,12,10,0.15)_45%,rgba(8,10,8,0.86)_100%)]"
        aria-hidden="true"
      ></div>
      <div
        class="absolute top-[36%] left-7 z-10 max-w-[310px] pr-5 text-white min-[1100px]:left-12 max-[900px]:top-auto max-[900px]:bottom-7 max-[900px]:left-7"
      >
        <h1 class="mb-6 text-h3 font-semibold tracking-[-0.02em] max-[900px]:mb-4">
          Conecte.<br />Gerencie.<br />Cresça.
        </h1>
        <p class="m-0 max-w-[200px] text-caption text-white/90">
          LinkOps conecta clientes e prestadores de serviços locais com facilidade e segurança.
        </p>
      </div>
    </section>

    <section class="flex min-w-0 items-start justify-center px-7 py-6 max-[900px]:px-6 max-[900px]:py-6 max-[480px]:px-5">
      <div class="w-full max-w-[430px]">
        <RouterLink
          to="/"
          class="mb-3 inline-block text-h5 font-medium tracking-[-0.02em] text-deep-navy no-underline hover:no-underline"
          aria-label="LinkOps — página inicial"
        >
          <span class="text-linkops-green">Link</span>Ops
        </RouterLink>

        <header class="mb-4">
          <h2 class="mb-1.5 text-h4 font-semibold tracking-[-0.01em]">Entrar na sua conta</h2>
          <p class="m-0 text-caption text-linkops-slate-500">Bem-vindo de volta! Faça login para continuar.</p>
        </header>

        <form @submit.prevent="submit">
          <div class="mb-3">
            <label for="login-email" class="!mb-1.5 !block text-xs !font-semibold">E-mail</label>
            <div class="relative flex items-center">
              <svg class="pointer-events-none absolute left-[13px] size-4 text-linkops-slate-500" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <rect x="3" y="5" width="18" height="14" rx="2.5" stroke="currentColor" stroke-width="1.7" />
                <path d="m4.5 7 6.2 5a2 2 0 0 0 2.6 0l6.2-5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
              <input
                id="login-email"
                v-model.trim="email"
                type="email"
                inputmode="email"
                autocomplete="username"
                maxlength="255"
                placeholder="Digite seu e-mail"
                required
                class="!h-[38px] !w-full !rounded-md !border-linkops-slate-200 !bg-white !py-0 !pr-3 !pl-[38px] text-[13px] leading-5 text-deep-navy outline-none placeholder:text-[13px] placeholder:leading-5 placeholder:text-linkops-slate-500 focus:!border-linkops-slate-200 focus:!outline-none focus:!ring-0"
              />
            </div>
          </div>

          <div class="mb-3">
            <label for="login-password" class="!mb-1.5 !block text-xs !font-semibold">Senha</label>
            <div class="relative flex items-center">
              <svg class="pointer-events-none absolute left-[13px] size-4 text-linkops-slate-500" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <rect x="4" y="10" width="16" height="11" rx="2.5" stroke="currentColor" stroke-width="1.7" />
                <path d="M8 10V7a4 4 0 0 1 8 0v3M12 14.5v2" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" />
              </svg>
              <input
                id="login-password"
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="current-password"
                maxlength="72"
                placeholder="Digite sua senha"
                required
                class="!h-[38px] !w-full !rounded-md !border-linkops-slate-200 !bg-white !py-0 !pr-10 !pl-[38px] text-[13px] leading-5 text-deep-navy outline-none placeholder:text-[13px] placeholder:leading-5 placeholder:text-linkops-slate-500 focus:!border-linkops-slate-200 focus:!outline-none focus:!ring-0"
              />
              <button
                type="button"
                class="absolute right-[13px] !min-h-[18px] !w-[18px] !rounded-none !bg-transparent !p-0 !text-linkops-slate-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-linkops-green"
                :aria-label="showPassword ? 'Ocultar senha' : 'Mostrar senha'"
                :aria-pressed="showPassword"
                @click="showPassword = !showPassword"
              >
                <svg v-if="showPassword" class="size-[18px]" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="m3 3 18 18M10.1 10.1a2.7 2.7 0 0 0 3.8 3.8M9.6 5.4A10.5 10.5 0 0 1 12 5c7 0 10.5 7 10.5 7a17 17 0 0 1-2.1 3.1M6.2 6.2A18 18 0 0 0 1.5 12S5 19 12 19c1.6 0 3-.4 4.2-1" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
                <svg v-else class="size-[18px]" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <path d="M1.5 12S5 5 12 5s10.5 7 10.5 7-3.5 7-10.5 7S1.5 12 1.5 12Z" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round" />
                  <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.7" />
                </svg>
              </button>
            </div>
          </div>

          <div class="mb-4 flex items-center justify-between">
            <label class="!inline-flex !cursor-pointer !flex-row !items-center !gap-1.5 text-[11px] !font-normal text-linkops-slate-700">
              <input v-model="rememberMe" type="checkbox" class="!m-0 !size-3.5 !rounded !p-0 accent-linkops-green" />
              <span>Lembrar-me</span>
            </label>
            <RouterLink to="/forgot-password" class="text-[11px] font-medium text-linkops-green">Esqueceu sua senha?</RouterLink>
          </div>

          <button
            class="!h-[38px] !min-h-[38px] !w-full !rounded-md !bg-linkops-green text-body-base !font-semibold !text-white hover:!bg-deep-navy"
            type="submit"
            :disabled="auth.loading"
          >
            {{ auth.loading ? 'Entrando…' : 'Entrar' }}
          </button>

          <div class="my-3 flex items-center gap-3 before:h-px before:flex-1 before:bg-linkops-slate-200 before:content-[''] after:h-px after:flex-1 after:bg-linkops-slate-200 after:content-['']">
            <span class="whitespace-nowrap text-caption text-linkops-slate-500">ou continue com</span>
          </div>

          <button
            class="!h-[38px] !min-h-[38px] !w-full !gap-2.5 !rounded-md !border !border-linkops-slate-200 !bg-white text-body-base !font-semibold !text-linkops-slate-900 hover:!border-linkops-slate-300 hover:!bg-soft-background"
            type="button"
            aria-label="Continuar com Google (indisponível)"
          >
            <svg class="size-[17px]" viewBox="0 0 24 24" aria-hidden="true">
              <path d="M23.5 12.3c0-.9-.1-1.7-.2-2.5H12v4.6h6.5a5.5 5.5 0 0 1-2.4 3.6v3h3.9c2.2-2.1 3.5-5.1 3.5-8.7Z" fill="#4285f4" />
              <path d="M12 24c3.2 0 6-1.1 8-3l-3.9-3c-1.1.7-2.5 1.2-4.1 1.2-3.1 0-5.8-2.1-6.7-4.9h-4v3.1A12 12 0 0 0 12 24Z" fill="#34a853" />
              <path d="M5.3 14.3a7.2 7.2 0 0 1 0-4.6V6.6h-4a12 12 0 0 0 0 10.8l4-3.1Z" fill="#fbbc05" />
              <path d="M12 4.8c1.8 0 3.4.6 4.6 1.8L20 3.1A11.6 11.6 0 0 0 12 0 12 12 0 0 0 1.3 6.6l4 3.1C6.2 6.9 8.9 4.8 12 4.8Z" fill="#ea4335" />
            </svg>
            Continuar com Google
          </button>

          <div class="mt-8 flex flex-col gap-2 text-center text-body-small text-linkops-slate-700">
            <p class="m-0">Não tem uma conta? <RouterLink to="/register" class="font-semibold text-linkops-green">Criar conta</RouterLink></p>
            <p class="m-0">Quer oferecer serviços? <RouterLink to="/register?role=PROVIDER" class="font-semibold text-linkops-green">Sou um prestador de serviços</RouterLink></p>
          </div>

        </form>
      </div>
    </section>
    </div>
  </main>
</template>
