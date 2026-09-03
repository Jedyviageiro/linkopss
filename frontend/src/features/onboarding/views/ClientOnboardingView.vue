<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import OnboardingIcon, { type OnboardingIconName } from '../components/OnboardingIcon.vue'
import { useNotificationStore } from '@/shared/notifications/notification-store'
import onboardingHero from '@/assets/photos/client-onboarding-left-side.png'

type InterestId = 'repairs' | 'cleaning' | 'care' | 'events' | 'beauty' | 'other'

const router = useRouter()
const notifications = useNotificationStore()
const currentStep = ref(1)
const city = ref('Maputo')
const interests = ref<InterestId[]>(['repairs', 'cleaning', 'events'])
const notificationsEnabled = ref(true)
const locating = ref(false)
const coordinates = ref<{ latitude: number; longitude: number } | null>(null)

const steps = [
  { title: 'Como funciona', subtitle: 'Contratar serviços ficou mais fácil.' },
  { title: 'Onde procura serviços?', subtitle: 'Escolha sua cidade para encontrarmos prestadores perto de si.' },
  { title: 'Quais serviços lhe interessam?', subtitle: 'Selecione suas preferências. Você poderá alterá-las depois.' },
]

const howItWorks: Array<{ icon: OnboardingIconName; title: string; description: string }> = [
  { icon: 'search', title: 'Encontre profissionais', description: 'Pesquise serviços e compare prestadores perto de si.' },
  { icon: 'send', title: 'Envie um pedido', description: 'Explique o que precisa e envie seu pedido de serviço.' },
  { icon: 'calendar-check', title: 'Combine os detalhes', description: 'Acorde a data, o preço e a forma de pagamento.' },
  { icon: 'star', title: 'Avalie o serviço', description: 'Partilhe sua experiência e ajude outros clientes.' },
]

const interestOptions: Array<{ id: InterestId; label: string; icon: OnboardingIconName; color: string }> = [
  { id: 'repairs', label: 'Reparações para casa', icon: 'wrench', color: '#16A34A' },
  { id: 'cleaning', label: 'Limpeza e casa', icon: 'spray-can', color: '#2563EB' },
  { id: 'care', label: 'Babás e cuidadores', icon: 'child-care', color: '#9333EA' },
  { id: 'events', label: 'Fotografia e eventos', icon: 'camera', color: '#F97316' },
  { id: 'beauty', label: 'Beleza e cuidados', icon: 'sparkles', color: '#EC4899' },
  { id: 'other', label: 'Outros serviços', icon: 'grid', color: '#64748B' },
]

const stepDetails = computed(() => steps[currentStep.value - 1]!)

function nextStep() {
  if (currentStep.value < 3) currentStep.value += 1
  else completeOnboarding(false)
}

function previousStep() {
  if (currentStep.value > 1) currentStep.value -= 1
}

function toggleInterest(id: InterestId) {
  interests.value = interests.value.includes(id)
    ? interests.value.filter((interest) => interest !== id)
    : [...interests.value, id]
}

function requestLocation() {
  if (!navigator.geolocation) {
    notifications.error('Seu navegador não permite usar a localização. Escolha a cidade manualmente.', 'Localização indisponível')
    return
  }

  locating.value = true
  navigator.geolocation.getCurrentPosition(
    ({ coords }) => {
      coordinates.value = { latitude: coords.latitude, longitude: coords.longitude }
      locating.value = false
      notifications.success('Vamos mostrar prestadores mais próximos de si.', 'Localização adicionada')
    },
    () => {
      locating.value = false
      notifications.error('Não conseguimos obter sua localização. Escolha a cidade manualmente.', 'Localização não encontrada')
    },
    { enableHighAccuracy: false, timeout: 8000, maximumAge: 300000 },
  )
}

async function completeOnboarding(skipped: boolean) {
  window.localStorage.setItem('linkops.clientOnboarding', JSON.stringify({
    city: city.value,
    coordinates: coordinates.value,
    interests: interests.value,
    notificationsEnabled: notificationsEnabled.value,
    completed: !skipped,
    skipped,
    completedAt: new Date().toISOString(),
  }))
  notifications.success(
    skipped ? 'Você pode completar suas preferências mais tarde.' : 'Suas preferências foram guardadas.',
    skipped ? 'Tudo pronto para começar' : 'Configuração concluída!',
  )
  await router.replace({ name: 'services' })
}
</script>

<template>
  <main class="flex min-h-screen items-center justify-center overflow-x-hidden bg-soft-background p-5 font-sans text-deep-navy max-[600px]:p-3">
    <div class="grid h-[min(720px,calc(100vh-40px))] min-h-[650px] w-full max-w-[1120px] grid-cols-[40%_60%] overflow-hidden rounded-xl border border-linkops-slate-200 bg-white shadow-[0_12px_38px_rgba(15,23,42,0.08)] max-[900px]:my-0 max-[900px]:h-auto max-[900px]:min-h-0 max-[900px]:grid-cols-1">
      <section class="relative h-full min-h-0 overflow-hidden bg-linkops-slate-900 max-[900px]:h-[220px]" aria-label="Clientes LinkOps">
        <img :src="onboardingHero" alt="Cliente a procurar serviços na LinkOps" class="absolute inset-0 size-full object-cover object-center" />
        <div class="absolute inset-0 bg-[linear-gradient(180deg,rgba(15,23,42,0.01),rgba(15,23,42,0.12))]" aria-hidden="true"></div>
      </section>

      <section class="flex min-w-0 justify-center overflow-hidden px-10 py-8 max-[900px]:overflow-visible max-[900px]:px-7 max-[600px]:px-5 max-[600px]:py-6">
        <div class="flex min-h-0 w-full max-w-[540px] flex-col">
          <header class="shrink-0">
            <div class="mb-6 flex items-center justify-between">
              <span class="text-[20px] leading-6 font-semibold tracking-[-0.02em]" aria-label="LinkOps"><span class="text-linkops-green">Link</span>Ops</span>
              <button type="button" class="!min-h-0 !rounded-none !bg-transparent !p-1 !text-[12px] !font-medium !text-linkops-slate-500 hover:!text-deep-navy" @click="completeOnboarding(true)">Saltar</button>
            </div>
            <div class="mb-7 flex items-center gap-4" aria-label="Progresso da configuração">
              <div class="grid flex-1 grid-cols-3 gap-2">
                <span v-for="step in 3" :key="step" class="h-1 rounded-full transition-colors duration-300" :class="step <= currentStep ? 'bg-linkops-green' : 'bg-linkops-slate-200'"></span>
              </div>
              <span class="shrink-0 text-[11px] font-medium text-linkops-slate-500">{{ String(currentStep).padStart(2, '0') }} de 03</span>
            </div>
          </header>

          <Transition name="onboarding-step" mode="out-in">
            <div :key="currentStep" class="min-h-0 flex-1 overflow-y-auto pr-1 [scrollbar-width:thin] max-[900px]:overflow-visible">
              <div class="mb-6">
                <h1 class="mb-1.5 text-[24px] leading-8 font-semibold tracking-[-0.025em]">{{ stepDetails.title }}</h1>
                <p class="m-0 max-w-[440px] text-[13px] leading-5 text-linkops-slate-500">{{ stepDetails.subtitle }}</p>
              </div>

              <div v-if="currentStep === 1" class="space-y-4">
                <article v-for="(item, index) in howItWorks" :key="item.title" class="relative flex items-center gap-4">
                  <div class="relative z-10 flex size-14 shrink-0 items-center justify-center rounded-full bg-green-50 text-linkops-green">
                    <OnboardingIcon :name="item.icon" class="size-6" />
                    <span v-if="index < howItWorks.length - 1" class="absolute top-14 left-1/2 h-4 border-l border-dashed border-linkops-slate-300" aria-hidden="true"></span>
                  </div>
                  <div><h2 class="mb-1 text-[13px] leading-4 font-semibold">{{ item.title }}</h2><p class="m-0 max-w-[390px] text-[11px] leading-4 text-linkops-slate-500">{{ item.description }}</p></div>
                </article>
              </div>

              <div v-else-if="currentStep === 2" class="space-y-4">
                <div>
                  <label for="onboarding-city" class="!block text-[12px] !font-semibold">Cidade</label>
                  <select id="onboarding-city" v-model="city" class="!mt-2 !h-11 !py-0 text-[13px]">
                    <option v-for="option in ['Maputo', 'Matola', 'Beira', 'Nampula', 'Chimoio', 'Quelimane', 'Tete', 'Xai-Xai', 'Pemba', 'Lichinga']" :key="option" :value="option">{{ option }}</option>
                  </select>
                </div>
                <button type="button" class="!flex !min-h-[76px] !w-full !justify-start gap-3.5 !rounded-lg !border !border-linkops-slate-200 !bg-white !p-4 !text-left !text-deep-navy hover:!border-linkops-green" :disabled="locating" @click="requestLocation">
                  <OnboardingIcon name="map-pin" class="size-5 shrink-0 text-linkops-green" />
                  <span class="min-w-0 flex-1"><span class="flex items-center justify-between gap-2 text-[12px] font-semibold"><span>{{ locating ? 'A obter sua localização...' : coordinates ? 'Localização atual adicionada' : 'Usar minha localização atual' }}</span><span class="rounded bg-green-50 px-2 py-0.5 text-[9px] text-linkops-green">Opcional</span></span><span class="mt-1 block text-[10px] leading-4 font-normal text-linkops-slate-500">Permita à LinkOps mostrar serviços próximos a si.</span></span>
                </button>
                <aside class="flex gap-3.5 rounded-lg bg-green-50/80 p-4">
                  <OnboardingIcon name="shield-check" class="size-5 shrink-0 text-linkops-green" />
                  <div><h2 class="mb-1 text-[11px] leading-4 font-semibold">Sua privacidade está protegida</h2><p class="m-0 text-[10px] leading-4 text-linkops-slate-500">Usamos sua localização apenas para encontrar prestadores próximos. Seu endereço exato não é partilhado.</p></div>
                </aside>
                <div class="relative h-[86px] overflow-hidden rounded-lg bg-[linear-gradient(135deg,#f8fafc_25%,#ecfdf5_25%,#ecfdf5_50%,#f8fafc_50%,#f8fafc_75%,#ecfdf5_75%)] bg-[length:36px_36px]" aria-hidden="true">
                  <span class="absolute inset-x-[28%] bottom-2 h-8 rounded-[50%] bg-linkops-green/15"></span><OnboardingIcon name="map-pin" class="absolute top-3 left-1/2 size-11 -translate-x-1/2 fill-linkops-green text-linkops-green drop-shadow" />
                </div>
              </div>

              <div v-else class="space-y-5">
                <div>
                  <div class="mb-2 flex items-center justify-between"><span class="text-[11px] font-medium text-linkops-slate-500">Escolha uma ou mais categorias</span><span class="text-[10px] font-semibold text-linkops-green">{{ interests.length }} selecionada{{ interests.length === 1 ? '' : 's' }}</span></div>
                  <div class="grid grid-cols-2 gap-3 max-[520px]:grid-cols-1">
                    <button v-for="option in interestOptions" :key="option.id" type="button" class="relative !flex !min-h-[72px] !justify-start gap-3 !rounded-lg !border !p-3 !text-left !text-deep-navy transition-colors" :class="interests.includes(option.id) ? '!border-linkops-green !bg-green-50/60' : '!border-linkops-slate-200 !bg-white hover:!border-linkops-slate-300'" :aria-pressed="interests.includes(option.id)" @click="toggleInterest(option.id)">
                      <span class="flex size-9 shrink-0 items-center justify-center rounded-full bg-white" :style="{ color: option.color }"><OnboardingIcon :name="option.icon" class="size-5" /></span>
                      <span class="pr-3 !text-[11px] leading-4 font-semibold">{{ option.label }}</span>
                      <span v-if="interests.includes(option.id)" class="absolute top-2.5 right-2.5 flex size-[18px] items-center justify-center rounded-full bg-linkops-green text-white"><svg class="size-3" viewBox="0 0 12 12" fill="none" aria-hidden="true"><path d="m3 6 2 2 4-4" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg></span>
                    </button>
                  </div>
                </div>
                <div class="flex min-h-[72px] items-center gap-3.5 rounded-lg border border-linkops-slate-200 p-4">
                  <OnboardingIcon name="bell" class="size-5 shrink-0 text-linkops-green" />
                  <div class="min-w-0 flex-1"><h2 class="m-0 text-[11px] leading-4 font-semibold">Quero receber notificações</h2><p class="mt-0.5 mb-0 text-[10px] leading-4 text-linkops-slate-500">Receba novidades e atualizações sobre seus pedidos.</p></div>
                  <button type="button" role="switch" :aria-checked="notificationsEnabled" class="relative !h-6 !min-h-6 !w-11 shrink-0 !rounded-full !p-0 transition-colors" :class="notificationsEnabled ? '!bg-linkops-green' : '!bg-linkops-slate-300'" aria-label="Receber notificações" @click="notificationsEnabled = !notificationsEnabled"><span class="absolute top-[3px] left-[3px] block size-[18px] rounded-full bg-white shadow-sm transition-transform" :class="notificationsEnabled ? 'translate-x-5' : 'translate-x-0'"></span></button>
                </div>
              </div>
            </div>
          </Transition>

          <footer class="mt-7 shrink-0 pb-1">
            <div class="flex gap-3">
              <button v-if="currentStep > 1" type="button" class="!h-11 !min-h-11 !w-[116px] !rounded-md !border !border-linkops-slate-300 !bg-white !p-0 !text-[13px] !font-semibold !text-linkops-slate-700 hover:!border-linkops-green hover:!text-linkops-green" @click="previousStep"><OnboardingIcon name="arrow-left" class="mr-1.5 size-4" />Voltar</button>
              <button type="button" class="!h-11 !min-h-11 flex-1 !rounded-md !bg-linkops-green !p-0 !text-[13px] !font-semibold hover:!bg-emerald-green" @click="nextStep">{{ currentStep === 3 ? 'Começar a procurar' : 'Continuar' }}</button>
            </div>
            <button type="button" class="mt-4 !min-h-0 !w-full !rounded-none !bg-transparent !p-1 !text-[11px] !font-medium !text-linkops-slate-500 hover:!text-deep-navy" @click="completeOnboarding(true)">Saltar por agora</button>
          </footer>
        </div>
      </section>
    </div>
  </main>
</template>

<style scoped>
.onboarding-step-enter-active,
.onboarding-step-leave-active { transition: opacity 180ms ease, transform 180ms ease; }
.onboarding-step-enter-from { opacity: 0; transform: translateX(12px); }
.onboarding-step-leave-to { opacity: 0; transform: translateX(-12px); }
@media (prefers-reduced-motion: reduce) {
  .onboarding-step-enter-active,
  .onboarding-step-leave-active { transition: none; }
}
</style>
