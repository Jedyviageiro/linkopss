<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth-store'
import { getErrorMessage } from '@/shared/api/api-error'
import loginHero from '@/assets/photos/man-login-page.png'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const email = ref('')
const password = ref('')
const rememberMe = ref(false)
const showPassword = ref(false)
const errorMessage = ref('')
const rememberedEmailKey = 'linkops.rememberedEmail'

onMounted(() => {
  const rememberedEmail = window.localStorage.getItem(rememberedEmailKey)
  if (rememberedEmail) {
    email.value = rememberedEmail
    rememberMe.value = true
  }
})

async function submit() {
  errorMessage.value = ''
  try {
    await auth.login({ email: email.value, password: password.value })
    if (rememberMe.value) window.localStorage.setItem(rememberedEmailKey, email.value)
    else window.localStorage.removeItem(rememberedEmailKey)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
    await router.replace(redirect)
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  }
}

function showGoogleUnavailable() {
  errorMessage.value = 'O login com Google ainda não está disponível.'
}
</script>

<template>
  <main class="grid min-h-screen grid-cols-[minmax(390px,46.5%)_1fr] bg-white text-[#111713] max-[860px]:grid-cols-1">
    <section
      class="relative min-h-screen overflow-hidden bg-[#111] max-[860px]:min-h-[330px] max-[480px]:min-h-[275px]"
      aria-label="LinkOps para prestadores de serviços"
    >
      <img
        :src="loginHero"
        alt="Prestador de serviços sorrindo enquanto utiliza um tablet"
        class="absolute inset-0 size-full object-cover object-[center_47%] max-[860px]:object-[center_38%]"
      />
      <div
        class="absolute inset-0 bg-[linear-gradient(180deg,rgba(6,9,7,0.08)_0%,rgba(6,9,7,0.06)_42%,rgba(4,8,5,0.76)_100%)]"
        aria-hidden="true"
      ></div>
      <div
        class="absolute bottom-[clamp(54px,12vh,116px)] left-[clamp(28px,4vw,64px)] z-10 max-w-[430px] pr-7 text-white max-[860px]:bottom-[34px] max-[480px]:bottom-[25px] max-[480px]:left-[22px]"
      >
        <h1
          class="mb-5 text-[clamp(36px,3.65vw,54px)] leading-[1.08] font-[750] tracking-[-0.035em] max-[860px]:mb-3 max-[860px]:text-[34px] max-[480px]:text-[29px]"
        >
          Conecte.<br />Gerencie.<br />Cresça.
        </h1>
        <p class="m-0 text-[clamp(14px,1.15vw,17px)] leading-[1.6] text-white/90 max-[860px]:text-sm">
          LinkOps conecta clientes e prestadores de serviços locais<br class="max-[480px]:hidden" />
          com facilidade e segurança.
        </p>
      </div>
    </section>

    <section
      class="flex min-w-0 items-center justify-center px-[clamp(34px,7vw,112px)] py-[42px] max-[860px]:px-6 max-[860px]:pt-[42px] max-[860px]:pb-14 max-[480px]:px-5"
    >
      <div class="w-full max-w-[430px]">
        <RouterLink
          to="/"
          class="mb-[33px] inline-block text-[22px] leading-none font-extrabold tracking-[-0.045em] text-[#161b18] no-underline hover:no-underline max-[480px]:mb-7"
          aria-label="LinkOps — página inicial"
        >
          <span class="text-[#149447]">Link</span>Ops
        </RouterLink>

        <header class="mb-7">
          <h2 class="mb-2 text-[25px] leading-[1.2] font-[750] tracking-[-0.025em]">Entrar na sua conta</h2>
          <p class="m-0 text-sm leading-[1.5] text-[#6d756f]">Bem-vindo de volta! Faça login para continuar.</p>
        </header>

        <form @submit.prevent="submit">
          <div class="mb-[19px]">
            <label for="login-email" class="!mb-[7px] !block text-[13px] !font-bold">E-mail ou telefone</label>
            <div class="relative flex items-center">
              <svg class="pointer-events-none absolute left-[13px] size-[17px] text-[#8b938e]" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <circle cx="12" cy="8" r="3.25" stroke="currentColor" stroke-width="1.7" />
                <path d="M5.5 19a6.5 6.5 0 0 1 13 0" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" />
              </svg>
              <input
                id="login-email"
                v-model.trim="email"
                type="text"
                inputmode="email"
                autocomplete="username"
                placeholder="Digite seu e-mail ou número de telefone"
                required
                class="!h-[45px] !w-full !rounded-md !border-[#dfe4e0] !bg-white !py-0 !pr-[42px] !pl-10 text-[13.5px] text-[#111713] outline-none placeholder:text-[#9ba29e] focus:!border-[#149447] focus:!outline-none focus:!ring-[3px] focus:!ring-[#149447]/10"
              />
            </div>
          </div>

          <div class="mb-[19px]">
            <label for="login-password" class="!mb-[7px] !block text-[13px] !font-bold">Senha</label>
            <div class="relative flex items-center">
              <svg class="pointer-events-none absolute left-[13px] size-[17px] text-[#8b938e]" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <rect x="4.5" y="10.5" width="15" height="9.5" rx="2" stroke="currentColor" stroke-width="1.7" />
                <path d="M8 10.5v-3a4 4 0 1 1 8 0v3" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" />
              </svg>
              <input
                id="login-password"
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="current-password"
                placeholder="Digite sua senha"
                required
                class="!h-[45px] !w-full !rounded-md !border-[#dfe4e0] !bg-white !py-0 !pr-[42px] !pl-10 text-[13.5px] text-[#111713] outline-none placeholder:text-[#9ba29e] focus:!border-[#149447] focus:!outline-none focus:!ring-[3px] focus:!ring-[#149447]/10"
              />
              <button
                type="button"
                class="absolute right-3 !min-h-6 !w-6 !rounded-none !bg-transparent !p-[3px] !text-[#858d88] hover:!text-[#4f5752] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#149447]"
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

          <div class="mt-0.5 mb-[23px] flex items-center justify-between">
            <label class="!inline-flex !cursor-pointer !flex-row !items-center !gap-[7px] text-[12.5px] !font-medium text-[#525b55]">
              <input v-model="rememberMe" type="checkbox" class="!m-0 !size-3.5 !rounded !p-0 accent-[#149447]" />
              <span>Lembrar-me</span>
            </label>
            <RouterLink to="/forgot-password" class="text-[12.5px] font-semibold text-[#149447]">Esqueceu sua senha?</RouterLink>
          </div>

          <p v-if="errorMessage" class="mt-[-9px] mb-3.5 rounded-md bg-red-50 px-[11px] py-[9px] text-[12.5px] leading-[1.4] text-[#b42318]" role="alert">
            {{ errorMessage }}
          </p>

          <button
            class="!h-11 !min-h-11 !w-full !rounded-md !bg-[#149447] text-[13.5px] !font-bold !text-white hover:!bg-[#0e7b38]"
            type="submit"
            :disabled="auth.loading"
          >
            {{ auth.loading ? 'Entrando…' : 'Entrar' }}
          </button>

          <div class="my-[18px] flex items-center gap-[13px] before:h-px before:flex-1 before:bg-[#eaeeeb] before:content-[''] after:h-px after:flex-1 after:bg-[#eaeeeb] after:content-['']">
            <span class="whitespace-nowrap text-[11.5px] text-[#7a827d]">ou continue com</span>
          </div>

          <button
            class="!h-11 !min-h-11 !w-full !gap-2.5 !rounded-md !border !border-[#dfe4e0] !bg-white text-[13.5px] !font-bold !text-[#202521] hover:!border-[#cbd1cd] hover:!bg-[#fafbfa]"
            type="button"
            @click="showGoogleUnavailable"
          >
            <svg class="size-[17px]" viewBox="0 0 24 24" aria-hidden="true">
              <path d="M23.5 12.3c0-.9-.1-1.7-.2-2.5H12v4.6h6.5a5.5 5.5 0 0 1-2.4 3.6v3h3.9c2.2-2.1 3.5-5.1 3.5-8.7Z" fill="#4285f4" />
              <path d="M12 24c3.2 0 6-1.1 8-3l-3.9-3c-1.1.7-2.5 1.2-4.1 1.2-3.1 0-5.8-2.1-6.7-4.9h-4v3.1A12 12 0 0 0 12 24Z" fill="#34a853" />
              <path d="M5.3 14.3a7.2 7.2 0 0 1 0-4.6V6.6h-4a12 12 0 0 0 0 10.8l4-3.1Z" fill="#fbbc05" />
              <path d="M12 4.8c1.8 0 3.4.6 4.6 1.8L20 3.1A11.6 11.6 0 0 0 12 0 12 12 0 0 0 1.3 6.6l4 3.1C6.2 6.9 8.9 4.8 12 4.8Z" fill="#ea4335" />
            </svg>
            Continuar com Google
          </button>

          <div class="mt-[29px] flex flex-col gap-2 text-center">
            <p class="m-0 text-[12.5px] text-[#626a65]">Não tem uma conta? <RouterLink to="/register" class="font-bold text-[#149447]">Criar conta</RouterLink></p>
            <p class="m-0 text-[12.5px] text-[#626a65]">Quer oferecer serviços? <RouterLink to="/register?role=PROVIDER" class="font-bold text-[#149447]">Sou um prestador de serviços</RouterLink></p>
          </div>
        </form>
      </div>
    </section>
  </main>
</template>
