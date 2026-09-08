<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import OnboardingIcon, { type OnboardingIconName } from '../components/OnboardingIcon.vue'
import { categoriesApi } from '@/features/categories/api/categories-api'
import type { Category } from '@/features/categories/types/category'
import { mediaApi } from '@/features/media/api/media-api'
import { providersApi } from '@/features/providers/api/providers-api'
import type { ProviderVerificationStatus } from '@/features/providers/types/provider'
import { servicesApi } from '@/features/services/api/services-api'
import type { PriceType } from '@/features/services/types/service'
import { useAuthStore } from '@/features/auth/stores/auth-store'
import { useNotificationStore } from '@/shared/notifications/notification-store'
import providerHero from '@/assets/photos/workers-onboarding-left-side.png'
import mpesaLogo from '@/assets/photos/m-pesa.png'

type Screen = 'welcome' | 'success' | 1 | 2 | 3 | 4
interface SelectedPhoto { file: File; url: string }

const router = useRouter()
const auth = useAuthStore()
const notifications = useNotificationStore()
const screen = ref<Screen>('welcome')
const loading = ref(false)
const locating = ref(false)
const returnToReview = ref(false)
const profileExists = ref(false)
const createdServiceId = ref<string | null>(null)
const profileImageUrl = ref<string | null>(null)
const serviceImageUrl = ref<string | null>(null)
const acceptedTerms = ref(false)
const verificationStatus = ref<ProviderVerificationStatus>('NOT_REQUESTED')
const categories = ref<Category[]>([])
const photos = ref<SelectedPhoto[]>([])

const profile = reactive({ bio: '', experience: '', city: '', latitude: null as number | null, longitude: null as number | null })
const service = reactive({ categoryId: '', title: '', description: '', priceType: 'FIXED' as PriceType, price: '' })
const paymentMethods = reactive({ cash: true, mpesa: true })

const benefits: Array<{ icon: OnboardingIconName; title: string; description: string }> = [
  { icon: 'badge-check', title: 'Crie um perfil que inspira confiança', description: 'Apresente seus serviços, experiência e trabalhos anteriores.' },
  { icon: 'clipboard-list', title: 'Receba mais pedidos', description: 'Seja encontrado por clientes que procuram o que você oferece.' },
  { icon: 'users', title: 'Gerencie tudo num só lugar', description: 'Acompanhe pedidos, mensagens, agenda e pagamentos.' },
]

const selectableCategories = computed(() => categories.value.flatMap((category) => {
  const children = (category.children ?? []).filter((child) => child.active)
  return children.length ? children : (category.active ? [category] : [])
}))
const selectedCategory = computed(() => selectableCategories.value.find((item) => item.id === service.categoryId)?.name ?? 'Não selecionada')
const providerName = computed(() => {
  const firstName = auth.user?.firstName?.trim() ?? ''
  const lastName = auth.user?.lastName?.trim() ?? ''
  return `${firstName} ${lastName}`.trim() || 'Seu perfil profissional'
})
const experienceLabel = computed(() => ({
  BEGINNER: 'Menos de 1 ano de experiência',
  INTERMEDIATE: '1 a 3 anos de experiência',
  EXPERIENCED: '4 a 7 anos de experiência',
  EXPERT: 'Mais de 7 anos de experiência',
})[profile.experience] ?? 'Experiência informada')

onMounted(async () => {
  try {
    const saved = window.localStorage.getItem('linkops.providerOnboardingDraft')
    if (saved) {
      const draft = JSON.parse(saved) as {
        profile?: Partial<typeof profile>
        service?: Partial<typeof service>
        paymentMethods?: Partial<typeof paymentMethods>
        acceptedTerms?: boolean
        serviceImageUrl?: string | null
        screen?: number
        createdServiceId?: string | null
      }
      if (draft.profile) Object.assign(profile, draft.profile)
      if (draft.service) Object.assign(service, draft.service)
      if (draft.paymentMethods) Object.assign(paymentMethods, draft.paymentMethods)
      acceptedTerms.value = draft.acceptedTerms ?? false
      serviceImageUrl.value = draft.serviceImageUrl ?? null
      if (draft.screen && draft.screen >= 1 && draft.screen <= 4) screen.value = draft.screen as Screen
      createdServiceId.value = draft.createdServiceId ?? null
    }
  } catch { window.localStorage.removeItem('linkops.providerOnboardingDraft') }
  try { categories.value = await categoriesApi.list() } catch { notifications.error('Não conseguimos carregar as categorias agora.', 'Categorias indisponíveis') }
  try {
    const existing = await providersApi.me()
    profileExists.value = true
    profile.bio = existing.bio ?? ''
    profile.city = existing.city
    profile.latitude = existing.latitude
    profile.longitude = existing.longitude
    profileImageUrl.value = existing.profileImageUrl
    verificationStatus.value = existing.verificationStatus
    paymentMethods.cash = existing.acceptsCash
    paymentMethods.mpesa = existing.acceptsMpesa
  } catch { /* A new provider is expected not to have a profile yet. */ }
})

onBeforeUnmount(() => photos.value.forEach((photo) => URL.revokeObjectURL(photo.url)))

function start() {
  screen.value = 1
  persistDraft()
  window.localStorage.setItem('linkops.providerOnboarding', JSON.stringify({ introduced: true, skipped: false, updatedAt: new Date().toISOString() }))
}

function goBack() {
  if (returnToReview.value && (screen.value === 1 || screen.value === 2)) {
    returnToReview.value = false
    screen.value = 4
    return
  }
  if (screen.value === 1) screen.value = 'welcome'
  else if (typeof screen.value === 'number' && screen.value > 1) screen.value = (screen.value - 1) as Screen
}

async function skipForNow() {
  window.localStorage.setItem('linkops.providerOnboarding', JSON.stringify({ introduced: true, skipped: true, updatedAt: new Date().toISOString() }))
  await router.replace({ name: 'dashboard' })
}

function useLocation() {
  if (!navigator.geolocation) {
    notifications.error('Escolha sua cidade manualmente para continuar.', 'Localização indisponível')
    return
  }
  locating.value = true
  navigator.geolocation.getCurrentPosition(
    ({ coords }) => {
      profile.latitude = coords.latitude
      profile.longitude = coords.longitude
      locating.value = false
      notifications.success('Usaremos apenas sua localização aproximada.', 'Localização adicionada')
    },
    () => {
      locating.value = false
      notifications.error('Não conseguimos obter sua localização. Você pode continuar informando a cidade.', 'Localização não encontrada')
    },
    { enableHighAccuracy: false, timeout: 8000, maximumAge: 300000 },
  )
}

async function saveProfile() {
  if (!profile.bio.trim()) {
    notifications.error('Conte brevemente sobre você e sua experiência.', 'Apresentação incompleta')
    return
  }
  if (!profile.experience) {
    notifications.error('Selecione seu nível de experiência.', 'Experiência incompleta')
    return
  }
  if (!profile.city.trim()) {
    notifications.error('Informe a cidade onde você presta serviços.', 'Cidade incompleta')
    return
  }
  loading.value = true
  const payload = { bio: profile.bio.trim(), city: profile.city.trim(), latitude: profile.latitude, longitude: profile.longitude }
  try {
    if (profileExists.value) await providersApi.updateProfile(payload)
    else {
      await providersApi.createProfile(payload)
      profileExists.value = true
    }
    screen.value = returnToReview.value ? 4 : 2
    returnToReview.value = false
    persistDraft()
  } catch {
    notifications.error('Não conseguimos guardar seu perfil agora. Confira os dados e tente novamente.', 'Perfil não guardado')
  } finally { loading.value = false }
}

function choosePhotos(event: Event) {
  const input = event.target as HTMLInputElement
  const selected = Array.from(input.files ?? [])
  const available = 5 - photos.value.length
  const valid = selected.filter((file) => file.type.startsWith('image/') && file.size <= 5 * 1024 * 1024).slice(0, available)
  if (valid.length !== selected.length) notifications.warning('Escolha até 5 imagens, com no máximo 5 MB cada.', 'Algumas fotos não foram adicionadas')
  photos.value.push(...valid.map((file) => ({ file, url: URL.createObjectURL(file) })))
  input.value = ''
}

function removePhoto(index: number) {
  const [removed] = photos.value.splice(index, 1)
  if (removed) URL.revokeObjectURL(removed.url)
}

async function saveService() {
  if (!service.categoryId || !service.title.trim() || !service.description.trim()) {
    notifications.error('Preencha a categoria, o nome e a descrição do serviço.', 'Serviço incompleto')
    return
  }
  const price = service.priceType === 'FIXED' ? Number(service.price) : null
  if (service.priceType === 'FIXED' && (!Number.isFinite(price) || Number(price) <= 0)) {
    notifications.error('Informe um preço válido em meticais.', 'Preço incompleto')
    return
  }
  loading.value = true
  try {
    const payload = {
      categoryId: service.categoryId,
      title: service.title.trim(),
      description: service.description.trim(),
      priceType: service.priceType,
      price,
    }
    const created = createdServiceId.value
      ? await servicesApi.update(createdServiceId.value, payload)
      : await servicesApi.create(payload)
    createdServiceId.value = created.id
    let failedUploads = 0
    for (const photo of photos.value) {
      try {
        const uploaded = await mediaApi.uploadServiceImage(created.id, photo.file)
        if (!serviceImageUrl.value) serviceImageUrl.value = uploaded.url
      } catch { failedUploads += 1 }
    }
    if (failedUploads) notifications.warning('Seu serviço foi criado, mas algumas fotos não foram enviadas.', 'Fotos pendentes')
    photos.value.forEach((photo) => URL.revokeObjectURL(photo.url))
    photos.value = []
    screen.value = returnToReview.value ? 4 : 3
    returnToReview.value = false
    persistDraft()
  } catch {
    notifications.error('Não conseguimos criar o serviço agora. Confira os dados e tente novamente.', 'Serviço não criado')
  } finally { loading.value = false }
}

function persistDraft() {
  window.localStorage.setItem('linkops.providerOnboardingDraft', JSON.stringify({
    profile,
    service,
    paymentMethods,
    acceptedTerms: acceptedTerms.value,
    serviceImageUrl: serviceImageUrl.value,
    screen: screen.value,
    createdServiceId: createdServiceId.value,
    updatedAt: new Date().toISOString(),
  }))
}

async function savePaymentMethods() {
  if (!paymentMethods.cash && !paymentMethods.mpesa) {
    notifications.error('Selecione pelo menos uma forma de pagamento para continuar.', 'Forma de pagamento necessária')
    return
  }
  loading.value = true
  try {
    await providersApi.updatePaymentMethods({ acceptsCash: paymentMethods.cash, acceptsMpesa: paymentMethods.mpesa })
    screen.value = 4
    persistDraft()
  } catch {
    notifications.error('Não conseguimos guardar as formas de pagamento agora. Tente novamente.', 'Preferências não guardadas')
  } finally { loading.value = false }
}

async function finish() {
  if (!acceptedTerms.value) {
    notifications.error('Leia e aceite os Termos de Uso e a Política de Privacidade para publicar.', 'Confirmação necessária')
    return
  }
  loading.value = true
  try {
    if (profileImageUrl.value && verificationStatus.value !== 'PENDING' && verificationStatus.value !== 'VERIFIED') {
      const provider = await providersApi.requestVerification()
      verificationStatus.value = provider.verificationStatus
    }
    window.localStorage.removeItem('linkops.providerOnboardingDraft')
    window.localStorage.setItem('linkops.providerOnboarding', JSON.stringify({ introduced: true, skipped: false, completed: true, completedAt: new Date().toISOString() }))
    screen.value = 'success'
  } catch {
    notifications.error('Não conseguimos publicar seu perfil agora. Tente novamente em instantes.', 'Perfil não publicado')
  } finally { loading.value = false }
}

function editProfile() {
  returnToReview.value = true
  screen.value = 1
}

function editService() {
  returnToReview.value = true
  screen.value = 2
}

async function viewProfile() {
  await router.replace({ name: 'provider-profile' })
}

async function viewBookings() {
  await router.replace({ name: 'bookings' })
}

function addAnotherService() {
  service.categoryId = ''
  service.title = ''
  service.description = ''
  service.priceType = 'FIXED'
  service.price = ''
  createdServiceId.value = null
  serviceImageUrl.value = null
  acceptedTerms.value = false
  screen.value = 2
  persistDraft()
}
</script>

<template>
  <main class="flex min-h-screen max-w-full items-center justify-center overflow-x-hidden bg-soft-background p-5 font-sans text-deep-navy max-[600px]:p-3">
    <div class="grid h-[min(720px,calc(100vh-40px))] min-h-[650px] w-full max-w-[1120px] grid-cols-[40%_60%] overflow-hidden rounded-xl border border-linkops-slate-200 bg-white shadow-[0_12px_38px_rgba(15,23,42,0.08)] max-[900px]:my-0 max-[900px]:h-auto max-[900px]:min-h-0 max-[900px]:grid-cols-1">
      <section class="relative h-full min-h-0 overflow-hidden bg-deep-navy max-[900px]:h-[220px]">
        <img :src="providerHero" alt="Prestador de serviços LinkOps" class="absolute inset-0 size-full object-cover object-center" />
        <div v-if="screen !== 'success'" class="absolute inset-0 bg-[linear-gradient(180deg,rgba(5,12,9,0.04),rgba(5,12,9,0.9))]" aria-hidden="true"></div>
        <Transition name="fade">
          <div v-if="screen === 'welcome'" class="absolute inset-x-0 bottom-0 z-10 p-9 text-white max-[600px]:p-6">
            <h1 class="mb-4 text-[29px] leading-9 font-semibold">Conecte-se.<br />Gerencie.<br />Cresça.</h1>
            <p class="mb-6 text-[12px] leading-[18px] text-white/85">Encontre mais clientes, gerencie pedidos e construa sua reputação.</p>
            <ul class="m-0 space-y-2 p-0 text-[11px] font-medium"><li v-for="item in ['Mais visibilidade', 'Mais pedidos', 'Mais oportunidades']" :key="item" class="flex items-center gap-2"><span class="flex size-4 items-center justify-center rounded-full bg-linkops-green">✓</span>{{ item }}</li></ul>
          </div>
        </Transition>
      </section>

      <section class="flex min-w-0 max-w-full justify-center overflow-hidden px-10 py-8 max-[900px]:overflow-visible max-[900px]:px-7 max-[600px]:px-5 max-[600px]:py-6">
        <div class="flex min-h-0 w-full min-w-0 max-w-[540px] flex-col">
          <template v-if="screen === 'welcome'">
            <header><div class="mb-10 text-[20px] font-semibold"><span class="text-linkops-green">Link</span>Ops</div><h2 class="mb-2 text-[25px] leading-8 font-semibold">Bem-vindo à LinkOps!</h2><p class="m-0 max-w-[390px] text-[13px] leading-5 text-linkops-slate-500">Vamos criar seu perfil profissional para que clientes possam encontrar você.</p></header>
            <div class="my-9 flex-1 space-y-6"><article v-for="benefit in benefits" :key="benefit.title" class="flex items-start gap-4"><span class="flex size-12 shrink-0 items-center justify-center rounded-full bg-green-50 text-linkops-green"><OnboardingIcon :name="benefit.icon" class="size-5" /></span><div><h3 class="mb-1 text-[13px] leading-[18px] font-semibold">{{ benefit.title }}</h3><p class="m-0 text-[11px] leading-[17px] text-linkops-slate-500">{{ benefit.description }}</p></div></article></div>
            <footer><button type="button" class="!h-11 !min-h-11 !w-full !rounded-md !bg-linkops-green !p-0 !text-[13px] !font-semibold !text-white hover:!bg-emerald-green" @click="start">Criar perfil profissional</button><button type="button" class="mt-4 !min-h-0 !w-full !rounded-none !bg-transparent !p-1 !text-[11px] !font-medium !text-linkops-slate-500 hover:!text-deep-navy" @click="skipForNow">Agora não</button></footer>
          </template>

          <template v-else-if="screen === 'success'">
            <header class="text-[20px] font-semibold"><span class="text-linkops-green">Link</span>Ops</header>
            <div class="flex flex-1 flex-col items-center justify-center text-center">
              <span class="mb-6 flex size-16 items-center justify-center rounded-full bg-green-50 text-linkops-green"><OnboardingIcon name="badge-check" class="size-8" /></span>
              <h2 class="mb-2 text-[23px] leading-8 font-semibold">Perfil publicado com sucesso!</h2>
              <p class="m-0 max-w-[380px] text-[12px] leading-5 text-linkops-slate-500">Seu perfil está no ar. Em breve você poderá receber pedidos de clientes.</p>
              <div class="mt-7 flex w-full max-w-[440px] items-center gap-4 rounded-lg border border-green-200 bg-green-50/70 p-4 text-left">
                <OnboardingIcon name="shield-check" class="size-6 shrink-0 text-linkops-green" />
                <div class="flex-1"><p class="mb-0.5 text-[10px] font-semibold">Status de verificação</p><p class="m-0 text-[9px] text-linkops-slate-500">{{ verificationStatus === 'NOT_REQUESTED' ? 'Adicione uma foto de perfil para iniciar a verificação.' : verificationStatus === 'VERIFIED' ? 'Suas informações já foram verificadas.' : 'Verificaremos suas informações em até 24h.' }}</p></div>
                <span class="text-[10px] font-semibold text-linkops-slate-600">{{ verificationStatus === 'VERIFIED' ? 'Verificado' : verificationStatus === 'PENDING' ? 'Pendente' : 'Por completar' }}</span>
              </div>
            </div>
            <footer class="mx-auto grid w-full max-w-[440px] gap-2.5">
              <button type="button" class="!h-11 !min-h-11 !rounded-md !bg-linkops-green !p-0 !text-[13px] !font-semibold !text-white hover:!bg-emerald-green" @click="viewProfile">Ver meu perfil</button>
              <button type="button" class="!h-11 !min-h-11 !rounded-md !border !border-linkops-slate-300 !bg-white !p-0 !text-[13px] !font-semibold !text-linkops-slate-700 hover:!border-linkops-green hover:!text-linkops-green" @click="addAnotherService">Publicar outro serviço</button>
              <button type="button" class="!h-9 !min-h-9 !rounded-none !bg-transparent !p-0 !text-[11px] !font-medium !text-linkops-slate-500 hover:!text-deep-navy" @click="viewBookings">Ir para pedidos</button>
            </footer>
          </template>

          <template v-else>
            <header class="shrink-0"><div class="mb-6 text-[20px] leading-6 font-semibold tracking-[-0.02em]"><span class="text-linkops-green">Link</span>Ops</div><p class="mb-2 text-[11px] font-medium text-linkops-slate-500">Etapa {{ screen }} de 4</p><div class="mb-7 grid grid-cols-4 gap-2"><span v-for="step in 4" :key="step" class="h-1 rounded-full transition-colors duration-300" :class="step <= screen ? 'bg-linkops-green' : 'bg-linkops-slate-200'"></span></div></header>

            <Transition name="onboarding-step" mode="out-in">
              <div :key="screen" class="min-h-0 min-w-0 flex-1 overflow-x-hidden overflow-y-auto pr-1 [scrollbar-width:none] max-[900px]:overflow-visible [&::-webkit-scrollbar]:hidden">
                <form v-if="screen === 1" id="provider-profile-step" class="space-y-3.5" @submit.prevent="saveProfile">
                  <div class="mb-6"><h2 class="mb-1.5 text-[24px] leading-8 font-semibold tracking-[-0.025em]">Conte sobre você</h2><p class="m-0 text-[13px] leading-5 text-linkops-slate-500">Essas informações serão exibidas no seu perfil.</p></div>
                  <div><label for="provider-bio" class="!mb-1.5 !block text-[11px] !font-semibold">Apresentação</label><div class="relative"><textarea id="provider-bio" v-model="profile.bio" maxlength="300" rows="3" class="!resize-none !text-[12px]" placeholder="Fale sobre você e sua experiência…"></textarea><span class="absolute right-2 bottom-1 text-[9px] text-linkops-slate-500">{{ profile.bio.length }}/300</span></div></div>
                  <div><label for="provider-experience" class="!mb-1.5 !block text-[11px] !font-semibold">Experiência</label><select id="provider-experience" v-model="profile.experience" class="!h-10 !py-0 !text-[12px]"><option value="" disabled>Selecione sua experiência</option><option value="BEGINNER">Menos de 1 ano</option><option value="INTERMEDIATE">1 a 3 anos</option><option value="EXPERIENCED">4 a 7 anos</option><option value="EXPERT">Mais de 7 anos</option></select></div>
                  <div><label for="provider-city" class="!mb-1.5 !block text-[11px] !font-semibold">Cidade</label><input id="provider-city" v-model.trim="profile.city" maxlength="100" class="!h-10 !text-[12px]" placeholder="Digite sua cidade" /></div>
                  <div><p class="mb-1 text-[11px] font-semibold">Localização <span class="font-normal text-linkops-slate-500">(não será pública)</span></p><p class="mb-2 flex items-start gap-2 text-[10px] leading-4 text-linkops-slate-500"><OnboardingIcon name="map-pin" class="mt-0.5 size-4 shrink-0" />Usaremos sua localização aproximada para mostrar seus serviços na sua região.</p><button type="button" class="!h-10 !min-h-10 !w-full !rounded-md !border !border-linkops-slate-300 !bg-white !p-0 !text-[12px] !font-semibold !text-linkops-slate-700 hover:!border-linkops-green hover:!text-linkops-green" :disabled="locating" @click="useLocation"><OnboardingIcon name="map-pin" class="mr-2 size-4 text-linkops-green" />{{ locating ? 'Obtendo localização…' : profile.latitude ? 'Localização adicionada' : 'Usar minha localização atual' }}</button></div>
                </form>

                <form v-else-if="screen === 2" id="provider-service-step" @submit.prevent="saveService">
                  <div class="mb-6"><h2 class="mb-1.5 text-[24px] leading-8 font-semibold tracking-[-0.025em]">Adicione seu primeiro serviço</h2><p class="m-0 text-[13px] leading-5 text-linkops-slate-500">Mostre o que você faz de melhor.</p></div>
                  <div class="grid grid-cols-[1.08fr_.92fr] gap-8 max-[620px]:grid-cols-1">
                    <div class="space-y-3"><div><label for="service-category" class="!mb-1 !block text-[10px] !font-semibold">Categoria</label><select id="service-category" v-model="service.categoryId" class="!h-9 !py-0 !text-[11px]"><option value="" disabled>Selecione uma categoria</option><option v-for="category in selectableCategories" :key="category.id" :value="category.id">{{ category.name }}</option></select></div><div><label for="service-title" class="!mb-1 !block text-[10px] !font-semibold">Nome do serviço</label><input id="service-title" v-model.trim="service.title" maxlength="150" class="!h-9 !text-[11px]" placeholder="Ex.: Instalação elétrica residencial" /></div><div><label for="service-description" class="!mb-1 !block text-[10px] !font-semibold">Descrição do serviço</label><div class="relative"><textarea id="service-description" v-model="service.description" maxlength="500" rows="3" class="!resize-none !text-[11px]" placeholder="Descreva seu serviço, o que está incluso, benefícios, etc."></textarea><span class="absolute right-2 bottom-1 text-[9px] text-linkops-slate-500">{{ service.description.length }}/500</span></div></div><fieldset class="border-0 p-0"><legend class="mb-1 text-[10px] font-semibold">Tipo de preço</legend><div class="grid grid-cols-2 gap-2"><label v-for="option in [{ value: 'FIXED', title: 'Preço fixo', text: 'Valor definido' }, { value: 'NEGOTIABLE', title: 'Negociável', text: 'Combinamos depois' }]" :key="option.value" class="cursor-pointer rounded-lg border !p-2.5" :class="service.priceType === option.value ? 'border-linkops-green bg-green-50' : 'border-linkops-slate-200'"><input v-model="service.priceType" type="radio" :value="option.value" class="!hidden" /><span class="text-[10px] font-semibold">{{ option.title }}</span><span class="text-[8px] text-linkops-slate-500">{{ option.text }}</span></label></div></fieldset><div v-if="service.priceType === 'FIXED'"><label for="service-price" class="!mb-1 !block text-[10px] !font-semibold">Preço (MZN)</label><input id="service-price" v-model="service.price" type="number" min="1" step="0.01" class="!h-9 !text-[11px]" placeholder="Ex.: 1500" /></div></div>
                    <div><p class="mb-1 text-[10px] font-semibold">Fotos do seu trabalho <span class="font-normal text-linkops-slate-500">(opcional)</span></p><p class="mb-3 text-[9px] leading-4 text-linkops-slate-500">Adicione fotos de qualidade do seu serviço.</p><label class="!flex min-h-[110px] cursor-pointer !items-center !justify-center rounded-lg border border-dashed border-linkops-slate-300 text-center"><input type="file" class="!hidden" accept="image/*" multiple @change="choosePhotos" /><span><span class="block text-[20px] text-linkops-slate-500">+</span><strong class="block text-[10px]">Adicionar fotos</strong><span class="text-[9px] text-linkops-slate-500">Até 5 fotos</span></span></label><div v-if="photos.length" class="mt-3 grid grid-cols-3 gap-2"><button v-for="(photo, index) in photos" :key="photo.url" type="button" class="relative !h-14 !min-h-14 overflow-hidden !rounded-md !p-0" :aria-label="`Remover foto ${index + 1}`" @click="removePhoto(index)"><img :src="photo.url" alt="" class="size-full object-cover" /><span class="absolute top-1 right-1 flex size-4 items-center justify-center rounded-full bg-red-600 text-[9px] text-white">×</span></button></div></div>
                  </div>
                </form>

                <div v-else-if="screen === 3">
                  <div class="mb-6"><h2 class="mb-1.5 text-[24px] leading-8 font-semibold tracking-[-0.025em]">Como você recebe pagamentos?</h2><p class="m-0 text-[13px] leading-5 text-linkops-slate-500">Selecione os métodos que você aceita.</p></div>
                  <fieldset class="space-y-3 border-0 p-0">
                    <legend class="sr-only">Métodos de pagamento aceites</legend>
                    <label class="!flex !min-h-[76px] cursor-pointer !flex-row !items-center !gap-4 rounded-lg border border-linkops-slate-200 bg-white !p-4 transition-colors hover:border-linkops-slate-300">
                      <input v-model="paymentMethods.cash" type="checkbox" class="!m-0 !size-4 !w-4 shrink-0 !rounded !p-0 accent-linkops-green" />
                      <span class="flex size-9 items-center justify-center rounded-full bg-white text-linkops-green"><OnboardingIcon name="banknote" class="size-5" /></span>
                      <span><strong class="block text-[11px]">Dinheiro</strong><span class="text-[9px] text-linkops-slate-500">Pagamento em espécie</span></span>
                    </label>
                    <label class="!flex !min-h-[76px] cursor-pointer !flex-row !items-center !gap-4 rounded-lg border border-linkops-slate-200 bg-white !p-4 transition-colors hover:border-linkops-slate-300">
                      <input v-model="paymentMethods.mpesa" type="checkbox" class="!m-0 !size-4 !w-4 shrink-0 !rounded !p-0 accent-linkops-green" />
                      <span class="flex size-9 shrink-0 items-center justify-center rounded-full bg-white"><img :src="mpesaLogo" alt="" class="size-8 object-contain" /></span>
                      <span><strong class="block text-[11px]">M-Pesa</strong><span class="text-[9px] text-linkops-slate-500">Pagamento via M-Pesa</span></span>
                    </label>
                  </fieldset>
                  <div class="mt-5 flex items-start gap-3 rounded-lg border border-green-200 bg-green-50 p-4"><OnboardingIcon name="shield-check" class="mt-0.5 size-5 shrink-0 text-linkops-green" /><p class="m-0 text-[10px] leading-4 text-green-800"><strong class="block">Nunca solicitamos seu PIN, senha ou credenciais do M-Pesa.</strong>Sua segurança é nossa prioridade.</p></div>
                </div>

                <div v-else>
                  <div class="mb-6"><h2 class="mb-1.5 text-[24px] leading-8 font-semibold tracking-[-0.025em]">Revise e publique seu perfil</h2><p class="m-0 text-[13px] leading-5 text-linkops-slate-500">Confira suas informações antes de publicar.</p></div>
                  <div class="space-y-4">
                    <section><div class="mb-1.5 flex items-center justify-between"><h3 class="m-0 text-[10px] font-semibold">Seu perfil</h3><button type="button" class="!min-h-0 !bg-transparent !p-0 !text-[9px] !font-semibold !text-linkops-green" @click="editProfile">Editar</button></div><article class="flex items-center gap-4 rounded-lg border border-linkops-slate-200 p-3"><img :src="profileImageUrl || providerHero" alt="" class="size-14 shrink-0 rounded-full object-cover" /><div class="min-w-0"><h4 class="mb-0.5 truncate text-[12px] font-semibold">{{ providerName }}</h4><p class="mb-0.5 text-[9px] text-linkops-slate-500">{{ profile.city }}, Moçambique</p><p class="mb-0.5 text-[9px] text-linkops-slate-500">{{ experienceLabel }}</p><p class="m-0 line-clamp-1 text-[9px] text-linkops-slate-500">{{ profile.bio }}</p></div></article></section>
                    <section><div class="mb-1.5 flex items-center justify-between"><h3 class="m-0 text-[10px] font-semibold">Seu serviço principal</h3><button type="button" class="!min-h-0 !bg-transparent !p-0 !text-[9px] !font-semibold !text-linkops-green" @click="editService">Editar</button></div><article class="flex items-center gap-4 rounded-lg border border-linkops-slate-200 p-3"><img :src="serviceImageUrl || providerHero" alt="" class="h-14 w-20 shrink-0 rounded-md object-cover" /><div class="min-w-0 flex-1"><div class="flex items-center gap-2"><h4 class="mb-0.5 truncate text-[12px] font-semibold">{{ service.title }}</h4><span v-if="service.priceType === 'FIXED'" class="shrink-0 rounded-full bg-green-50 px-2 py-0.5 text-[8px] font-semibold text-linkops-green">Preço fixo</span></div><p class="mb-0.5 text-[9px] text-linkops-slate-500">Categoria: {{ selectedCategory }}</p><p class="m-0 text-[9px] text-linkops-slate-500">Preço: {{ service.priceType === 'FIXED' ? `${service.price} MZN` : 'Negociável' }}</p></div></article></section>
                    <label class="!flex cursor-pointer !flex-row !items-start !gap-2 text-[11px] leading-4 text-linkops-slate-600"><input v-model="acceptedTerms" type="checkbox" class="!mt-0.5 !size-4 !w-4 shrink-0 !rounded !p-0 accent-linkops-green" /><span>Li e concordo com os <a href="#" class="font-semibold text-linkops-green" @click.prevent>Termos de Uso</a> e a <a href="#" class="font-semibold text-linkops-green" @click.prevent>Política de Privacidade</a>.</span></label>
                  </div>
                </div>
              </div>
            </Transition>

            <footer class="mt-7 flex shrink-0 gap-3 pb-1"><button type="button" class="!h-11 !min-h-11 !w-[116px] !rounded-md !border !border-linkops-slate-300 !bg-white !p-0 !text-[13px] !font-semibold !text-linkops-slate-700 hover:!border-linkops-green hover:!text-linkops-green" :disabled="loading" @click="goBack"><OnboardingIcon name="arrow-left" class="mr-1.5 size-4" />Voltar</button><button v-if="screen === 1" type="submit" form="provider-profile-step" class="!h-11 !min-h-11 flex-1 !rounded-md !bg-linkops-green !p-0 !text-[13px] !font-semibold !text-white hover:!bg-emerald-green" :disabled="loading">{{ loading ? 'Guardando…' : 'Continuar' }}</button><button v-else-if="screen === 2" type="submit" form="provider-service-step" class="!h-11 !min-h-11 flex-1 !rounded-md !bg-linkops-green !p-0 !text-[13px] !font-semibold !text-white hover:!bg-emerald-green" :disabled="loading">{{ loading ? 'Criando serviço…' : 'Continuar' }}</button><button v-else-if="screen === 3" type="button" class="!h-11 !min-h-11 flex-1 !rounded-md !bg-linkops-green !p-0 !text-[13px] !font-semibold !text-white hover:!bg-emerald-green" :disabled="loading" @click="savePaymentMethods">{{ loading ? 'Guardando…' : 'Continuar' }}</button><button v-else type="button" class="!h-11 !min-h-11 flex-1 !rounded-md !bg-linkops-green !p-0 !text-[13px] !font-semibold !text-white hover:!bg-emerald-green" :disabled="loading" @click="finish">{{ loading ? 'Publicando…' : 'Publicar perfil' }}</button></footer>
          </template>
        </div>
      </section>
    </div>
  </main>
</template>

<style scoped>
.onboarding-step-enter-active,.onboarding-step-leave-active,.fade-enter-active,.fade-leave-active{transition:opacity .18s ease,transform .18s ease}.onboarding-step-enter-from{opacity:0;transform:translateX(12px)}.onboarding-step-leave-to{opacity:0;transform:translateX(-12px)}.fade-enter-from,.fade-leave-to{opacity:0}@media(prefers-reduced-motion:reduce){.onboarding-step-enter-active,.onboarding-step-leave-active,.fade-enter-active,.fade-leave-active{transition:none}}
</style>
