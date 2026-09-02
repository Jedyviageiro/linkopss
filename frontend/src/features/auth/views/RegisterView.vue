<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth-store'
import { getErrorMessage } from '@/shared/api/api-error'
import type { RegisterRequest } from '../types/auth'
import registerHero from '@/assets/photos/man-login-page.png'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const fullName = ref('')
const acceptedTerms = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
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
  const names = fullName.value.trim().split(/\s+/).filter(Boolean)

  if (names.length < 2) {
    errorMessage.value = 'Digite seu nome e apelido.'
    return
  }

  form.firstName = names.shift() ?? ''
  form.lastName = names.join(' ')

  try {
    const user = await auth.register(form)
    await router.replace(user.role === 'PROVIDER' ? '/provider/profile' : '/dashboard')
  } catch (error) {
    errorMessage.value = getErrorMessage(error)
  }
}
</script>

<template>
  <main class="flex min-h-screen overflow-x-hidden items-center justify-center bg-soft-background p-3 font-sans text-deep-navy">
    <div class="grid h-[min(600px,calc(100vh-24px))] min-h-[560px] w-full max-w-[1000px] grid-cols-[43%_57%] overflow-hidden rounded-[10px] border border-linkops-slate-200 bg-white shadow-[0_4px_22px_rgba(15,23,42,0.06)] max-[900px]:h-auto max-[900px]:min-h-0 max-[900px]:grid-cols-1">
      <section class="relative h-full overflow-hidden bg-deep-navy max-[900px]:h-[280px]" aria-label="LinkOps para novos prestadores">
        <img
          :src="registerHero"
          alt="Prestador de serviços trabalhando com um portátil"
          class="absolute inset-0 size-full object-cover object-[center_32%]"
        />
        <div class="absolute inset-0 bg-[linear-gradient(180deg,rgba(15,23,42,0.08)_0%,rgba(15,23,42,0.15)_42%,rgba(15,23,42,0.88)_100%)]" aria-hidden="true"></div>
        <div class="absolute top-[51%] left-8 z-10 max-w-[260px] pr-4 text-white max-[900px]:top-auto max-[900px]:bottom-7 max-[900px]:left-7">
          <h1 class="mb-3 text-[24px] leading-7 font-semibold tracking-[-0.02em]">Faça parte<br />da mudança.</h1>
          <p class="m-0 max-w-[230px] text-caption text-white/90">Crie sua conta e comece a conectar pessoas e oportunidades hoje mesmo.</p>
        </div>
      </section>

      <section class="flex min-w-0 items-start justify-center overflow-x-hidden px-7 py-6 max-[900px]:overflow-y-visible max-[900px]:px-6 max-[900px]:py-6 max-[480px]:px-5">
        <div class="w-full max-w-[430px]">
          <RouterLink to="/" class="mb-3 inline-block text-h5 font-medium tracking-[-0.02em] text-deep-navy no-underline hover:no-underline" aria-label="LinkOps — página inicial">
            <span class="text-linkops-green">Link</span>Ops
          </RouterLink>

          <header class="mb-4 shrink-0">
            <h2 class="mb-1.5 text-h4 font-semibold tracking-[-0.01em]">Criar sua conta</h2>
            <p class="m-0 text-caption text-linkops-slate-500">Preencha os dados abaixo para começar.</p>
          </header>

          <form class="max-h-[452px] w-full min-w-0 overflow-x-hidden overflow-y-auto pr-2 [scrollbar-width:thin] max-[900px]:max-h-none max-[900px]:overflow-y-visible max-[900px]:pr-0" @submit.prevent="submit">
            <fieldset class="mb-4 grid min-w-0 grid-cols-2 gap-2 border-0 p-0 max-[560px]:grid-cols-1">
              <legend class="sr-only">Tipo de conta</legend>
              <label
                class="relative !block cursor-pointer rounded-md border !p-3 transition-colors"
                :class="form.role === 'CLIENT' ? 'border-linkops-green bg-soft-background' : 'border-linkops-slate-200 bg-white'"
              >
                <input v-model="form.role" class="sr-only" type="radio" value="CLIENT" />
                <span class="flex items-center gap-1.5 text-[13px] leading-5 font-semibold" :class="form.role === 'CLIENT' ? 'text-linkops-green' : 'text-deep-navy'">
                  <span class="flex size-3 items-center justify-center rounded-full border-[1.5px]" :class="form.role === 'CLIENT' ? 'border-linkops-green bg-linkops-green' : 'border-linkops-slate-300'">
                    <span v-if="form.role === 'CLIENT'" class="size-1.5 rounded-full bg-white"></span>
                  </span>
                  Sou cliente
                </span>
                <span class="mt-0.5 block pl-[18px] text-[11px] leading-4 text-linkops-slate-500">Quero contratar serviços</span>
              </label>

              <label
                class="relative !block cursor-pointer rounded-md border !p-3 transition-colors"
                :class="form.role === 'PROVIDER' ? 'border-linkops-green bg-soft-background' : 'border-linkops-slate-200 bg-white'"
              >
                <input v-model="form.role" class="sr-only" type="radio" value="PROVIDER" />
                <span class="flex items-center gap-1.5 text-[13px] leading-5 font-semibold" :class="form.role === 'PROVIDER' ? 'text-linkops-green' : 'text-deep-navy'">
                  <span class="flex size-3 items-center justify-center rounded-full border-[1.5px]" :class="form.role === 'PROVIDER' ? 'border-linkops-green bg-linkops-green' : 'border-linkops-slate-300'">
                    <span v-if="form.role === 'PROVIDER'" class="size-1.5 rounded-full bg-white"></span>
                  </span>
                  Sou prestador de serviços
                </span>
                <span class="mt-0.5 block pl-[18px] text-[11px] leading-4 text-linkops-slate-500">Quero oferecer meus serviços</span>
              </label>
            </fieldset>

            <div class="mb-3">
              <label for="register-name" class="!mb-1.5 !block text-xs !font-semibold">Nome completo</label>
              <div class="relative flex items-center">
                <svg class="pointer-events-none absolute left-3 size-4 text-linkops-slate-500" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <circle cx="12" cy="8" r="3.25" stroke="currentColor" stroke-width="1.7" />
                  <path d="M5.5 19a6.5 6.5 0 0 1 13 0" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" />
                </svg>
                <input id="register-name" v-model.trim="fullName" class="!h-[38px] !w-full !rounded-md !border-linkops-slate-200 !bg-white !py-0 !pr-3 !pl-[38px] text-[13px] leading-5 text-deep-navy placeholder:text-[13px] placeholder:leading-5 placeholder:text-linkops-slate-500 focus:!border-linkops-slate-200 focus:!outline-none focus:!ring-0" placeholder="Digite seu nome completo" autocomplete="name" required />
              </div>
            </div>

            <div class="mb-3">
              <label for="register-email" class="!mb-1.5 !block text-xs !font-semibold">E-mail</label>
              <div class="relative flex items-center">
                <svg class="pointer-events-none absolute left-3 size-4 text-linkops-slate-500" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <rect x="3" y="5" width="18" height="14" rx="2.5" stroke="currentColor" stroke-width="1.7" />
                  <path d="m4.5 7 6.2 5a2 2 0 0 0 2.6 0l6.2-5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
                <input id="register-email" v-model.trim="form.email" class="!h-[38px] !w-full !rounded-md !border-linkops-slate-200 !bg-white !py-0 !pr-3 !pl-[38px] text-[13px] leading-5 text-deep-navy placeholder:text-[13px] placeholder:leading-5 placeholder:text-linkops-slate-500 focus:!border-linkops-slate-200 focus:!outline-none focus:!ring-0" type="email" placeholder="Digite seu e-mail" autocomplete="email" required />
              </div>
            </div>

            <div class="mb-3 grid min-w-0 grid-cols-2 gap-2 max-[560px]:grid-cols-1">
              <div class="min-w-0">
                <label for="register-phone" class="!mb-1.5 !block text-xs !font-semibold">Telefone</label>
                <div class="relative flex items-center">
                  <svg class="pointer-events-none absolute left-3 size-4 text-linkops-slate-500" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                    <path d="M5 4h3l1.5 4-2 1.5a14 14 0 0 0 7 7l1.5-2L20 16v3a1 1 0 0 1-1 1C10.7 20 4 13.3 4 5a1 1 0 0 1 1-1Z" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round" />
                  </svg>
                  <input id="register-phone" v-model.trim="form.phone" class="!h-[38px] !w-full !rounded-md !border-linkops-slate-200 !bg-white !py-0 !pr-3 !pl-[38px] text-[13px] leading-5 text-deep-navy placeholder:text-[13px] placeholder:leading-5 placeholder:text-linkops-slate-500 focus:!border-linkops-slate-200 focus:!outline-none focus:!ring-0" type="tel" placeholder="Digite seu telefone" autocomplete="tel" maxlength="50" />
                </div>
              </div>

              <div class="min-w-0">
                <label for="register-password" class="!mb-1.5 !block text-xs !font-semibold">Senha</label>
                <div class="relative flex items-center">
                  <svg class="pointer-events-none absolute left-3 size-4 text-linkops-slate-500" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                    <rect x="4" y="10" width="16" height="11" rx="2.5" stroke="currentColor" stroke-width="1.7" />
                    <path d="M8 10V7a4 4 0 0 1 8 0v3" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" />
                  </svg>
                  <input id="register-password" v-model="form.password" :type="showPassword ? 'text' : 'password'" class="!h-[38px] !w-full !rounded-md !border-linkops-slate-200 !bg-white !py-0 !pr-10 !pl-[38px] text-[13px] leading-5 text-deep-navy placeholder:text-[13px] placeholder:leading-5 placeholder:text-linkops-slate-500 focus:!border-linkops-slate-200 focus:!outline-none focus:!ring-0" placeholder="Crie uma senha" autocomplete="new-password" minlength="8" maxlength="72" required />
                  <button type="button" class="absolute right-3 !min-h-[18px] !w-[18px] !rounded-none !bg-transparent !p-0 !text-linkops-slate-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-linkops-green" :aria-label="showPassword ? 'Ocultar senha' : 'Mostrar senha'" @click="showPassword = !showPassword">
                    <svg class="size-[17px]" viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M1.5 12S5 5 12 5s10.5 7 10.5 7-3.5 7-10.5 7S1.5 12 1.5 12Z" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round" /><circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.7" /></svg>
                  </button>
                </div>
              </div>
            </div>

            <div class="mb-3">
              <label for="register-confirm-password" class="!mb-1.5 !block text-xs !font-semibold">Confirmar senha</label>
              <div class="relative flex items-center">
                <svg class="pointer-events-none absolute left-3 size-4 text-linkops-slate-500" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                  <rect x="4" y="10" width="16" height="11" rx="2.5" stroke="currentColor" stroke-width="1.7" />
                  <path d="M8 10V7a4 4 0 0 1 8 0v3" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" />
                </svg>
                <input id="register-confirm-password" v-model="form.confirmPassword" :type="showConfirmPassword ? 'text' : 'password'" class="!h-[38px] !w-full !rounded-md !border-linkops-slate-200 !bg-white !py-0 !pr-10 !pl-[38px] text-[13px] leading-5 text-deep-navy placeholder:text-[13px] placeholder:leading-5 placeholder:text-linkops-slate-500 focus:!border-linkops-slate-200 focus:!outline-none focus:!ring-0" placeholder="Confirme sua senha" autocomplete="new-password" minlength="8" maxlength="72" required />
                <button type="button" class="absolute right-3 !min-h-[18px] !w-[18px] !rounded-none !bg-transparent !p-0 !text-linkops-slate-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-linkops-green" :aria-label="showConfirmPassword ? 'Ocultar senha' : 'Mostrar senha'" @click="showConfirmPassword = !showConfirmPassword">
                  <svg class="size-[17px]" viewBox="0 0 24 24" fill="none" aria-hidden="true"><path d="M1.5 12S5 5 12 5s10.5 7 10.5 7-3.5 7-10.5 7S1.5 12 1.5 12Z" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round" /><circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="1.7" /></svg>
                </button>
              </div>
            </div>

            <label class="!mb-3 !inline-flex !cursor-pointer !flex-row !items-start !gap-2 !text-[10px] !leading-4 !font-normal text-linkops-slate-700">
              <input v-model="acceptedTerms" class="!mt-0.5 !size-3.5 !shrink-0 !rounded !p-0 accent-linkops-green" type="checkbox" required />
              <span>Concordando com os <a href="#" class="font-semibold text-linkops-green">Termos de Uso</a> e a <a href="#" class="font-semibold text-linkops-green">Política de Privacidade</a></span>
            </label>

            <p v-if="errorMessage" class="mb-3 rounded-md bg-soft-background px-3 py-2 text-caption font-medium text-linkops-amber" role="alert">{{ errorMessage }}</p>

            <button class="!h-[38px] !min-h-[38px] !w-full !rounded-md !bg-linkops-green text-body-base !font-semibold !text-white hover:!bg-deep-navy" type="submit" :disabled="auth.loading">
              {{ auth.loading ? 'Criando conta…' : 'Criar conta' }}
            </button>

            <div class="my-3 flex items-center gap-2 before:h-px before:flex-1 before:bg-linkops-slate-200 before:content-[''] after:h-px after:flex-1 after:bg-linkops-slate-200 after:content-['']">
              <span class="whitespace-nowrap text-caption text-linkops-slate-500">ou continue com</span>
            </div>

            <button class="!h-[38px] !min-h-[38px] !w-full !gap-2.5 !rounded-md !border !border-linkops-slate-200 !bg-white text-body-base !font-semibold !text-linkops-slate-900 hover:!border-linkops-slate-300 hover:!bg-soft-background" type="button" aria-label="Continuar com Google (indisponível)">
              <svg class="size-[17px]" viewBox="0 0 24 24" aria-hidden="true"><path d="M23.5 12.3c0-.9-.1-1.7-.2-2.5H12v4.6h6.5a5.5 5.5 0 0 1-2.4 3.6v3h3.9c2.2-2.1 3.5-5.1 3.5-8.7Z" fill="#4285f4" /><path d="M12 24c3.2 0 6-1.1 8-3l-3.9-3c-1.1.7-2.5 1.2-4.1 1.2-3.1 0-5.8-2.1-6.7-4.9h-4v3.1A12 12 0 0 0 12 24Z" fill="#34a853" /><path d="M5.3 14.3a7.2 7.2 0 0 1 0-4.6V6.6h-4a12 12 0 0 0 0 10.8l4-3.1Z" fill="#fbbc05" /><path d="M12 4.8c1.8 0 3.4.6 4.6 1.8L20 3.1A11.6 11.6 0 0 0 12 0 12 12 0 0 0 1.3 6.6l4 3.1C6.2 6.9 8.9 4.8 12 4.8Z" fill="#ea4335" /></svg>
              Continuar com Google
            </button>

            <p class="mt-3 text-center text-[10px] leading-4 text-linkops-slate-700">Já tem uma conta? <RouterLink to="/login" class="font-semibold text-linkops-green">Entrar</RouterLink></p>
          </form>
        </div>
      </section>
    </div>
  </main>
</template>
