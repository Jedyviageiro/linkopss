<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import MarketplaceSidebar from '@/app/components/MarketplaceSidebar.vue'
import HomeCardCollection from '@/app/components/HomeCardCollection.vue'
import MarketplaceAvatar from '@/app/components/MarketplaceAvatar.vue'
import Icon, { type DashboardIconName } from '@/app/components/DashboardIcon.vue'
import { useAuthStore } from '@/features/auth/stores/auth-store'
import { bookingsApi } from '@/features/bookings/api/bookings-api'
import type { Booking } from '@/features/bookings/types/booking'
import { categoriesApi } from '@/features/categories/api/categories-api'
import { servicesApi } from '@/features/services/api/services-api'
import type { ServiceOffering } from '@/features/services/types/service'
import { providersApi } from '@/features/providers/api/providers-api'
import type { Provider } from '@/features/providers/types/provider'
import { mediaApi } from '@/features/media/api/media-api'
import { notificationsApi } from '@/features/notifications/api/notifications-api'
import { useModalStore } from '@/shared/modals/modal-store'
import { useNotificationStore } from '@/shared/notifications/notification-store'

const router = useRouter()
const auth = useAuthStore()
const modals = useModalStore()
const notices = useNotificationStore()
const searchText = ref('')
const loading = ref(true)
const failed = ref(false)
interface HomeService { service: ServiceOffering; provider: Provider | null; photo: string | null }
const recommendations = ref<HomeService[]>([])
interface HomeProvider { provider: Provider; service: ServiceOffering | null }
const featured = ref<HomeProvider[]>([])
const providersFailed = ref(false)
const servicePage = ref(0)
const providerPage = ref(0)
const hasMoreServices = ref(false)
const hasMoreProviders = ref(false)
const loadingMoreServices = ref(false)
const loadingMoreProviders = ref(false)
const providerCache = new Map<string, Promise<Provider>>()
const pageSize = 8
const bookings = ref<Booking[]>([])
const unread = ref(0)
const menuOpen = ref(false)
const favorites = ref<string[]>([])
const categorySlugs = ref<Record<string, string>>({})
const fullName = computed(() => `${auth.user?.firstName ?? ''} ${auth.user?.lastName ?? ''}`.trim())
const initials = computed(() => fullName.value.split(' ').map(p => p[0]).slice(0, 2).join(''))
const activeOrders = computed(() => bookings.value.filter(b => ['PENDING', 'ACCEPTED', 'IN_PROGRESS'].includes(b.status)))
const currentOrder = computed(() => activeOrders.value[0])
const completedOrders = computed(() => bookings.value.filter(b => b.status === 'COMPLETED').filter((b, i, all) => all.findIndex(other => other.serviceOfferingId === b.serviceOfferingId) === i).slice(0, 3))
function price(service: ServiceOffering) { return service.priceType === 'FIXED' ? `Desde ${new Intl.NumberFormat('pt-MZ', { maximumFractionDigits: 0 }).format(service.price ?? 0)} MT` : 'Preço negociável' }
function rating(provider: Provider | null) { return provider?.averageRating ? provider.averageRating.toLocaleString('pt-MZ', { minimumFractionDigits: 1 }) : 'Novo' }
function providerLabel(item: { provider: Provider | null; service: ServiceOffering | null }) { return item.provider ? `${item.provider.firstName} ${item.provider.firstName === 'Studio' ? item.provider.lastName : `${item.provider.lastName[0]}.`}` : item.service?.providerName ?? '' }
function serviceIcon(service: ServiceOffering): DashboardIconName {
  const category = service.categoryName.toLowerCase()
  return category.includes('limpeza') ? 'spray' : category.includes('fotografia') ? 'camera' : category.includes('ar-condicionado') ? 'air' : 'wrench'
}
const favoriteKey = computed(() => `linkops.favorites.${auth.user?.id ?? 'guest'}`)

// Homepage rhythm: 8px controls, 12px card padding, 16px section gaps.
// Inter 400/500 is shared with authentication and onboarding; no synthetic bold.
const panel = 'rounded-[10px] border border-[#EBEEF2] bg-white shadow-[0_2px_8px_rgba(16,24,40,.025)]'
const heading = 'm-0 text-[13px] leading-[20px] font-medium tracking-[-.01em] text-[#111827]'
const textAction = '!min-h-0 !rounded-none !border-0 !bg-transparent !p-0 !text-[11px] !leading-[16px] !font-medium !text-[#0FA24A] hover:!text-[#0C7C3A]'
const primary = '!min-h-[36px] !rounded-[8px] !border-0 !bg-[#0FA24A] !px-[18px] !py-[7px] !text-[12px] !leading-[18px] !font-medium !text-white hover:!bg-[#0C7C3A]'
const outline = '!min-h-0 !rounded-[8px] !border !border-[#0FA24A] !bg-white !px-[13px] !py-[7px] !text-[12px] !leading-[18px] !font-medium !text-[#0C7C3A] hover:!bg-[#F0FDF4]'
const iconColors: Record<string, string> = { green: 'text-[#009C3B]', blue: 'text-[#0073DF]', purple: 'text-[#A04CFF]', orange: 'text-[#FF7415]', pink: 'text-[#FF4092]' }
const tones: Record<string, string> = { green: 'bg-[#DFF6E6] text-[#0FA24A]', blue: 'bg-[#DBEAFE] text-[#2563EB]', purple: 'bg-[#F3E8FF] text-[#9333EA]', orange: 'bg-[#FFEDD5] text-[#EA580C]', pink: 'bg-[#FCE7F3] text-[#DB2777]', amber: 'bg-[#FEF3C7] text-[#D97706]' }
const categories: { title: string; match: string; icon: DashboardIconName; tone: string }[] = [
  { title: 'Reparações para Casa', match: 'repar', icon: 'wrench', tone: 'green' },
  { title: 'Limpeza e Casa', match: 'limpeza', icon: 'spray', tone: 'blue' },
  { title: 'Babás e Cuidadores', match: 'bab', icon: 'care', tone: 'purple' },
  { title: 'Fotografia e Eventos', match: 'fotografia', icon: 'camera', tone: 'orange' },
  { title: 'Beleza', match: 'beleza', icon: 'beauty', tone: 'pink' },
]
const popular: { title: string; icon: DashboardIconName; tone: string }[] = [
  { title: 'Limpeza de casas', icon: 'spray', tone: 'blue' }, { title: 'Canalização', icon: 'pipe', tone: 'green' },
  { title: 'Eletricidade', icon: 'bolt', tone: 'amber' }, { title: 'Tranças', icon: 'beauty', tone: 'pink' },
  { title: 'Reparação de eletrodomésticos', icon: 'appliance', tone: 'purple' }, { title: 'Ar-condicionado', icon: 'air', tone: 'blue' },
]
const steps: { title: string; description: string; icon: DashboardIconName }[] = [
  { title: 'Escolha um serviço', description: 'Encontre o serviço que precisa e profissionais perto de si.', icon: 'search' },
  { title: 'Envie o pedido', description: 'Descreva o que precisa e envie o seu pedido em poucos passos.', icon: 'send' },
  { title: 'Combine com o prestador', description: 'Converse, defina os detalhes do serviço e o preço.', icon: 'message' },
  { title: 'Avalie o trabalho', description: 'Avalie o serviço e ajude outras pessoas a fazerem boas escolhas.', icon: 'star' },
]
function explore(q = '') { router.push({ name: 'services', query: q ? { q } : {} }) }
function categorySearch(item: typeof categories[number]) {
  const slug = categorySlugs.value[item.match]
  router.push({ name: 'services', query: slug ? { category: slug } : { q: item.title } })
}
function toggleFavorite(service: ServiceOffering) {
  try {
    const saved: unknown = JSON.parse(localStorage.getItem(favoriteKey.value) || '[]')
    const ids = new Set<string>(Array.isArray(saved) ? saved.filter(id => typeof id === 'string') : [])
    if (ids.has(service.id)) ids.delete(service.id)
    else ids.add(service.id)
    localStorage.setItem(favoriteKey.value, JSON.stringify([...ids]))
    favorites.value = [...ids]
    notices.success(ids.has(service.id) ? 'Serviço guardado neste dispositivo.' : 'Serviço removido dos favoritos.')
  } catch { notices.error('Não foi possível guardar a sua preferência. Tente novamente.') }
}
async function logout() {
  menuOpen.value = false
  if (!await modals.open({ kind: 'danger', title: 'Terminar sessão?', message: 'Terá de entrar novamente para aceder à sua conta.', confirmLabel: 'Sair', cancelLabel: 'Ficar' })) return
  try { await auth.logout() } finally { await router.replace('/login') }
}
async function cancelOrder() {
  const order = currentOrder.value
  if (!order || !await modals.open({ kind: 'danger', title: 'Cancelar pedido?', message: 'O prestador será informado do cancelamento.', confirmLabel: 'Cancelar pedido', cancelLabel: 'Manter pedido' })) return
  try { const updated = await bookingsApi.cancel(order.id); bookings.value = bookings.value.map(b => b.id === updated.id ? updated : b); notices.success('Pedido cancelado.') }
  catch { notices.error('Não conseguimos cancelar o pedido. Tente novamente.') }
}
async function hydrateServices(services: ServiceOffering[]): Promise<HomeService[]> {
  return Promise.all(services.map(async service => {
    if (!providerCache.has(service.providerId)) providerCache.set(service.providerId, providersApi.get(service.providerId))
    const [p, images] = await Promise.allSettled([providerCache.get(service.providerId)!, mediaApi.listServiceImages(service.id)])
    if (p.status === 'rejected') providerCache.delete(service.providerId)
    return { service, provider: p.status === 'fulfilled' ? p.value : null, photo: images.status === 'fulfilled' ? images.value[0]?.url ?? null : null }
  }))
}
async function hydrateProviders(providers: Provider[]): Promise<HomeProvider[]> {
  return Promise.all(providers.map(async provider => {
    providerCache.set(provider.id, Promise.resolve(provider))
    const existing = recommendations.value.find(item => item.service.providerId === provider.id)?.service
    if (existing) return { provider, service: existing }
    try { return { provider, service: (await servicesApi.byProvider(provider.id, { size: 1 })).content[0] ?? null } }
    catch { return { provider, service: null } }
  }))
}
async function loadMoreServices() {
  if (loadingMoreServices.value || !hasMoreServices.value) return
  loadingMoreServices.value = true
  try {
    const page = await servicesApi.list({ city: 'Maputo', size: pageSize, page: servicePage.value + 1, sort: 'title,asc' })
    const additions = await hydrateServices(page.content)
    const existingIds = new Set(recommendations.value.map(item => item.service.id))
    recommendations.value.push(...additions.filter(item => !existingIds.has(item.service.id)))
    servicePage.value = page.page.number
    hasMoreServices.value = page.page.number + 1 < page.page.totalPages
  } catch { notices.error('Não foi possível carregar mais serviços. Tente novamente.') }
  finally { loadingMoreServices.value = false }
}
async function loadMoreProviders() {
  if (loadingMoreProviders.value || !hasMoreProviders.value) return
  loadingMoreProviders.value = true
  try {
    const page = await providersApi.list({ city: 'Maputo', size: pageSize, page: providerPage.value + 1, sort: 'name,asc' })
    const additions = await hydrateProviders(page.content)
    const existingIds = new Set(featured.value.map(item => item.provider.id))
    featured.value.push(...additions.filter(item => !existingIds.has(item.provider.id)))
    providerPage.value = page.page.number
    hasMoreProviders.value = page.page.number + 1 < page.page.totalPages
  } catch { notices.error('Não foi possível carregar mais prestadores. Tente novamente.') }
  finally { loadingMoreProviders.value = false }
}
async function load() {
  loading.value = true
  failed.value = false
  providersFailed.value = false
  servicePage.value = 0
  providerPage.value = 0
  hasMoreServices.value = false
  hasMoreProviders.value = false
  const [servicesResult, catsResult, ordersResult, notificationsResult, providersResult] = await Promise.allSettled([
    servicesApi.list({ city: 'Maputo', size: pageSize, sort: 'title,asc' }),
    categoriesApi.list(), bookingsApi.list({ size: 50, sort: 'createdAt,desc' }), notificationsApi.list({ size: 50 }),
    providersApi.list({ city: 'Maputo', size: pageSize, sort: 'name,asc' }),
  ])
  if (providersResult.status === 'fulfilled') {
    for (const provider of providersResult.value.content) providerCache.set(provider.id, Promise.resolve(provider))
  }
  if (servicesResult.status === 'fulfilled') {
    recommendations.value = await hydrateServices(servicesResult.value.content)
    hasMoreServices.value = servicesResult.value.page.number + 1 < servicesResult.value.page.totalPages
    try { const saved: unknown = JSON.parse(localStorage.getItem(favoriteKey.value) || '[]'); favorites.value = Array.isArray(saved) ? saved.filter(id => typeof id === 'string') : [] } catch { favorites.value = [] }
  } else { failed.value = true; recommendations.value = [] }
  if (providersResult.status === 'fulfilled') {
    featured.value = await hydrateProviders(providersResult.value.content)
    hasMoreProviders.value = providersResult.value.page.number + 1 < providersResult.value.page.totalPages
  } else { providersFailed.value = true; featured.value = [] }
  if (catsResult.status === 'fulfilled') {
    const all = catsResult.value.flatMap(c => [c, ...(c.children ?? [])])
    for (const c of categories) categorySlugs.value[c.match] = all.find(item => `${item.name} ${item.slug}`.toLowerCase().includes(c.match))?.slug ?? ''
  }
  if (ordersResult.status === 'fulfilled') bookings.value = ordersResult.value.content
  if (notificationsResult.status === 'fulfilled') unread.value = notificationsResult.value.content.filter(n => !n.read).length
  loading.value = false
}
onMounted(load)
</script>

<template>
  <div class="grid min-h-screen grid-cols-[230px_minmax(0,1fr)] bg-white font-sans text-[14px] leading-[1.4] text-[#111827] max-[960px]:grid-cols-1 [&_svg]:block [&_svg]:shrink-0 [&_button]:transition-colors [&_a]:transition-colors [&_:focus-visible]:outline-2 [&_:focus-visible]:outline-offset-2 [&_:focus-visible]:outline-[#0FA24A]">
    <MarketplaceSidebar :orders="activeOrders.length" :notifications="unread" />

    <div class="min-w-0">
      <header class="flex items-center justify-between gap-[24px] h-[76px] px-[27px] py-[18px] max-[600px]:gap-[12px] max-[600px]:px-[16px]">
        <div><button type="button" class="!min-h-0 !border-0 !bg-transparent !p-0 !text-[13px] !font-medium !text-[#6B7280]" @click="router.push({ name: 'services', query: { city: 'Maputo' } })"><Icon name="pin" class="mr-[5px] size-[14px] text-[#0FA24A]" />Maputo, Moçambique<Icon name="chevron-down" class="ml-[5px] size-[14px]" /></button></div>
        <div class="flex shrink-0 items-center gap-[18px] max-[600px]:gap-[8px]">
          <RouterLink to="/notifications" class="relative flex size-[40px] items-center justify-center rounded-[8px] text-[#111827] hover:bg-[#F1F8F3]" :aria-label="unread ? `Notificações: ${unread} por ler` : 'Notificações'"><Icon name="bell" class="size-[19px]" /><span v-if="unread" class="absolute top-0 right-0 flex size-[16px] items-center justify-center rounded-full border-2 border-[#F6F7F9] bg-[#0FA24A] text-[10px] font-medium text-white">{{ unread }}</span></RouterLink>
          <div class="relative" @keydown.esc="menuOpen = false"><button type="button" class="!min-h-0 !gap-[9px] !border-0 !bg-transparent !p-0 !text-[#111827]" :aria-expanded="menuOpen" aria-label="Menu da conta" @click="menuOpen = !menuOpen"><span class="flex size-[38px] items-center justify-center rounded-full bg-[#DFF6E6] text-[13px] text-[#0C7C3A]">{{ initials }}</span><span class="text-[13.5px] font-medium max-[600px]:hidden">{{ fullName }}</span><Icon name="chevron-down" class="size-[15px] text-[#9CA3AF]" /></button><div v-if="menuOpen" class="absolute top-[48px] right-0 z-30 w-[180px] rounded-[12px] border border-[#E7E9EC] bg-white p-[8px] shadow-lg"><RouterLink to="/profile" class="block rounded-[8px] p-[10px] text-[#374151] hover:bg-[#F3F4F6]">Minha conta</RouterLink><button type="button" class="!w-full !justify-start !border-0 !bg-white !p-[10px] !text-[#EF4444]" @click="logout">Sair</button></div></div>
        </div>
      </header>

      <div class="grid grid-cols-[minmax(0,1fr)_375px] items-start gap-[24px] px-[27px] pb-[8px] max-[1380px]:grid-cols-1 max-[600px]:px-[16px]">
        <main class="flex min-w-0 flex-col gap-[16px]">
          <h1 class="sr-only">Início</h1>
          <section :class="panel" class="p-[14px]">
            <h2 class="mb-[10px] text-[15px] leading-[20px] font-medium">De que serviço precisa?</h2>
            <form class="flex h-[37px] gap-[10px] max-[600px]:h-auto max-[600px]:flex-wrap" @submit.prevent="explore(searchText)"><label class="!flex !flex-row min-w-0 flex-1 !items-center !gap-[10px] rounded-[8px] border border-[#E7E9EC] bg-white px-[12px] max-[600px]:basis-full"><Icon name="search" class="size-[18px] text-[#9CA3AF]" /><input v-model.trim="searchText" aria-label="Pesquisar serviços" type="search" placeholder="Ex.: Canalizador, Limpeza de casa, Fotógrafo..." class="!w-full !rounded-none !border-0 !bg-transparent !px-0 !py-[11px] !text-[12px] !leading-[18px] !text-[#111827] !shadow-none placeholder:!text-[#9CA3AF]" /></label><button type="submit" :class="primary" class="!w-[113px]">Buscar</button><button type="button" class="!gap-[8px] !rounded-[8px] !border !border-[#E7E9EC] !bg-white !min-h-0 !w-[115px] !px-[16px] !text-[11px] !font-medium !text-[#374151]" @click="explore()"><Icon name="sliders" class="size-[16px]" />Filtros</button></form>
            <div class="mt-[10px] flex flex-wrap items-center gap-[8px]"><span class="mr-[2px] text-[11px] font-medium text-[#6B7280]">Popular:</span><button v-for="term in ['Canalizador', 'Limpeza de casa', 'Eletricista', 'Ar-condicionado', 'Fotógrafo']" :key="term" type="button" class="!min-h-0 !rounded-full !border !border-[#E7E9EC] !bg-white !px-[11px] !py-[3px] !text-[11px] !leading-[16px] !font-medium !text-[#374151] hover:!border-[#0FA24A]" @click="explore(term)">{{ term }}</button></div>
          </section>

          <section><div class="mb-[6px] flex items-center justify-between"><h2 :class="heading">Categorias</h2><button type="button" :class="textAction" @click="explore()">Ver todas</button></div><div class="grid grid-cols-5 gap-[16px] max-[960px]:grid-cols-3 max-[480px]:grid-cols-2"><button v-for="item in categories" :key="item.title" type="button" class="!flex !min-w-0 !flex-col !h-[104px] !gap-[7px] !rounded-[9px] !border !border-[#EBEEF2] !bg-white !px-[10px] !py-[12px] shadow-[0_3px_12px_rgba(16,24,40,.025)] !text-[#111827] hover:!border-[#0FA24A]" @click="categorySearch(item)"><span class="flex size-[36px] items-center justify-center [&_svg]:size-[32px]" :class="iconColors[item.tone]"><Icon :name="item.icon" class="size-[24px]" /></span><span class="max-w-[104px] text-[12px] leading-[17px] font-medium">{{ item.title }}</span></button></div></section>

          <HomeCardCollection title="Serviços recomendados para si" variant="services" :count="loading ? 0 : recommendations.length"
            :has-more="hasMoreServices" :loading-more="loadingMoreServices" @load-more="loadMoreServices">
            <template #actions><button type="button" :class="textAction" @click="explore()">Ver todos</button></template>
            <template #empty>
              <p v-if="loading" class="py-[48px] text-center text-[#6B7280]" role="status">A carregar serviços…</p>
              <div v-else-if="failed" class="py-[24px] text-center"><p class="mb-[12px] text-[#6B7280]">Não conseguimos carregar os serviços.</p><button type="button" :class="outline" @click="load">Tentar novamente</button></div>
              <p v-else class="py-[24px] text-[#6B7280]">Ainda não existem serviços disponíveis nesta região.</p>
            </template>
              <article v-for="item in recommendations" :key="item.service.id" class="flex min-h-[214px] min-w-0 flex-col overflow-hidden rounded-[10px] border border-[#EBEEF2] bg-white shadow-[0_2px_8px_rgba(16,24,40,.03)]">
                <div class="relative aspect-[1.85] shrink-0"><img v-if="item.photo" :src="item.photo ?? undefined" :alt="item.service.title" class="absolute inset-0 size-full object-cover object-top" /><span v-else class="absolute inset-0 flex items-center justify-center bg-[#F1F8F3] text-[#0FA24A]"><Icon :name="serviceIcon(item.service)" class="size-[44px]" /></span><button type="button" class="!absolute !top-[7px] !right-[7px] !size-[27px] !min-h-0 !rounded-full !border-0 !bg-white/95 !p-0" :class="favorites.includes(item.service.id) ? '!text-[#EF4444]' : '!text-[#6B7280]'" :aria-pressed="favorites.includes(item.service.id)" aria-label="Guardar serviço nos favoritos" @click="toggleFavorite(item.service)"><Icon name="heart" class="size-[15px]" :class="{ 'fill-current': favorites.includes(item.service.id) }" /></button></div>
                <div class="flex flex-1 flex-col gap-[6px] text-[11px] leading-[15px] p-[11px]"><button type="button" class="!h-[16px] !min-h-0 !items-start !justify-start !border-0 !bg-transparent !p-0 !text-left !text-[11px] !leading-[16px] !font-medium !text-[#111827]" @click="explore(item.service.title)" :title="item.service.title"><span class="block truncate">{{ item.service.title }}</span></button><div class="flex items-center gap-[6px] text-[10.5px] font-medium text-[#374151]"><MarketplaceAvatar :provider="item.provider" :fallback-name="item.service.providerName" class="size-[20px] text-[8px]" /><span class="min-w-0 flex-1 truncate">{{ providerLabel(item) }}</span><span class="flex shrink-0 items-center gap-[3px] font-normal text-[#6B7280]"><Icon v-if="item.provider && item.provider.averageRating > 0" name="star" class="size-[12px] fill-[#00852F] text-[#00852F]" />{{ rating(item.provider) }}</span></div><p class="m-0 flex items-center gap-[5px] text-[11px] text-[#6B7280]"><Icon name="pin" class="size-[13px] text-[#9CA3AF]" />{{ item.service.city }}</p><div class="mt-auto flex min-h-[22px] flex-wrap items-center justify-between gap-[6px] pt-[2px]"><strong class="text-[12px] font-medium text-[#00852F]">{{ price(item.service) }}</strong><span v-if="item.provider?.verified" class="flex items-center gap-[4px] rounded-[4px] bg-[#E3F4E8] px-[5px] py-[3px] text-[9px] font-medium text-[#0C7C3A]"><Icon name="shield" class="size-[14px]" />Verificado</span></div></div>
              </article>
          </HomeCardCollection>

          <div class="grid grid-cols-[minmax(0,1.42fr)_minmax(0,1fr)] items-stretch gap-[18px] max-[1100px]:grid-cols-1">
            <HomeCardCollection title="Prestadores em destaque" variant="providers" :count="loading ? 0 : featured.length"
              :has-more="hasMoreProviders" :loading-more="loadingMoreProviders" @load-more="loadMoreProviders">
              <template #empty>
                <div v-if="providersFailed" class="py-[16px]"><p class="mb-[8px] text-[12px] text-[#6B7280]">Não conseguimos carregar os prestadores.</p><button type="button" :class="outline" @click="load">Tentar novamente</button></div>
                <p v-else class="text-[12px] leading-[18px] text-[#6B7280]">{{ loading ? 'A carregar prestadores…' : 'Sem prestadores em destaque.' }}</p>
              </template>
                <button v-for="item in featured" :key="item.provider.id" type="button"
                  class="!flex !min-w-0 !min-h-[174px] !flex-col !items-start !justify-start !gap-0 !rounded-[9px] !border !border-[#EBEEF2] !bg-white !p-[10px] !text-left !font-sans !font-normal !text-[#111827] hover:!border-[#A8D5B5]"
                  @click="explore(item.service?.title ?? providerLabel(item))">
                  <MarketplaceAvatar :provider="item.provider" :fallback-name="providerLabel(item)" class="mb-[6px] size-[54px] text-[15px]" />
                  <strong class="max-w-full truncate text-[11px] leading-[16px] font-medium">{{ providerLabel(item) }}</strong>
                  <span class="mt-[3px] max-w-full truncate text-[10px] leading-[15px] text-[#6B7280]">{{ item.service?.categoryName ?? 'Prestador de serviços' }}</span>
                  <span class="mt-[4px] flex items-center gap-[4px] text-[11px] leading-[16px] text-[#6B7280]"><Icon v-if="item.provider && item.provider.averageRating > 0" name="star" class="size-[12px] fill-[#00852F] text-[#00852F]" />{{ rating(item.provider) }}</span>
                  <span class="mt-[3px] text-[10px] leading-[15px] text-[#6B7280]">{{ item.provider?.completedJobs }} trabalhos</span>
                  <span v-if="item.provider?.verified" class="mt-[6px] inline-flex items-center gap-[3px] rounded-[4px] bg-[#E3F4E8] px-[4px] py-[2px] text-[9px] leading-[14px] font-medium text-[#0C7C3A]"><Icon name="shield" class="size-[12px]" />Verificado</span>
                </button>
            </HomeCardCollection>
            <section class="flex min-w-0 flex-col">
              <div class="mb-[8px] flex items-center justify-between gap-[8px]"><h2 :class="heading">Serviços populares</h2><button type="button" :class="textAction" @click="explore()">Ver todos</button></div>
              <div class="grid flex-1 grid-cols-2 grid-rows-3 gap-[8px]">
                <button v-for="item in popular" :key="item.title" type="button" class="!min-h-[48px] !min-w-0 !justify-start !gap-[8px] !rounded-[8px] !border !border-[#EBEEF2] !bg-white !px-[8px] !py-[7px] !text-left !font-sans !text-[#111827] hover:!border-[#A8D5B5]" @click="explore(item.title)">
                  <span class="flex size-[28px] shrink-0 items-center justify-center rounded-full" :class="tones[item.tone]"><Icon :name="item.icon" class="size-[16px]" /></span><span class="text-[10px] leading-[15px] font-medium">{{ item.title }}</span>
                </button>
              </div>
            </section>
          </div>

          <section class="-mt-[8px] flex items-center justify-between gap-[12px] rounded-[8px] border border-[#F0F6F2] bg-[#F1F8F3] px-[12px] py-[7px] max-[600px]:flex-wrap"><div class="flex items-center gap-[10px]"><span class="flex size-[28px] shrink-0 items-center justify-center text-[#00852F]"><Icon name="shield" class="size-[25px]" /></span><div><h2 class="mb-[3px] text-[12px] leading-[17px] font-medium text-[#00852F]">A sua segurança é nossa prioridade</h2><p class="m-0 text-[10px] leading-[14px] text-[#6B7280]">Prestadores verificados, avaliações reais e pagamento combinado consigo.</p></div></div><RouterLink to="/help" class="shrink-0 rounded-[4px] border border-[#7AB88D] bg-white px-[18px] py-[6px] text-[10px] font-medium text-[#0C7C3A]">Saiba mais</RouterLink></section>
        </main>

        <aside aria-label="Resumo da conta" class="flex min-w-0 flex-col gap-[16px] max-[1380px]:grid max-[1380px]:grid-cols-3 max-[960px]:grid-cols-1">
          <section :class="panel" class="p-[14px]"><div class="mb-[12px] flex items-center justify-between"><h2 class="text-[13px] leading-[20px] font-medium">Pedido em andamento</h2><RouterLink to="/bookings" :class="textAction">Ver todos</RouterLink></div><div v-if="currentOrder" class="rounded-[12px] border border-[#E7E9EC] p-[14px]"><div class="flex items-start gap-[11px]"><span class="flex size-[42px] shrink-0 items-center justify-center rounded-[10px] bg-[#DBEAFE] text-[#2563EB]"><Icon name="wrench" class="size-[20px]" /></span><div class="min-w-0"><h3 class="mb-[3px] text-[14px] leading-[20px] font-medium">{{ currentOrder.serviceTitle }}</h3><p class="mb-[6px] text-[12px] text-[#6B7280]">{{ currentOrder.providerName }}</p><span class="text-[12px] font-medium text-[#0FA24A]">{{ currentOrder.status === 'IN_PROGRESS' ? 'Serviço em andamento' : currentOrder.status === 'ACCEPTED' ? 'Pedido aceite' : 'Aguardando resposta' }}</span></div></div><div class="mt-[12px] grid grid-cols-2 gap-[8px]"><RouterLink to="/bookings" class="rounded-[8px] border border-[#E7E9EC] px-[4px] py-[8px] text-center text-[12px] font-medium text-[#374151]">Ver pedido</RouterLink><button type="button" class="!min-h-0 !rounded-[8px] !border !border-[#EF4444] !bg-white !px-[4px] !py-[8px] !text-[12px] !text-[#EF4444]" @click="cancelOrder">Cancelar</button></div></div><div v-else class="flex items-center gap-[12px] rounded-[12px] border border-[#E7E9EC] p-[18px]"><Icon name="clipboard" class="size-[28px] text-[#0FA24A]" /><div><strong class="text-[13px] font-medium">{{ loading ? 'A carregar pedidos…' : 'Nenhum pedido em andamento' }}</strong><p class="mt-[5px] text-[12px] leading-[18px] text-[#6B7280]">Acompanhe aqui os serviços que contratar.</p></div></div></section>
          <section :class="panel" class="p-[14px]"><div class="mb-[14px] flex items-center justify-between"><h2 class="text-[13px] leading-[20px] font-medium">Contratar novamente</h2><RouterLink to="/bookings" :class="textAction">Ver todos</RouterLink></div><div v-if="completedOrders.length" class="flex flex-col gap-[14px]"><article v-for="order in completedOrders" :key="order.id" class="flex items-center gap-[11px]"><span class="flex size-[40px] shrink-0 items-center justify-center rounded-[8px] bg-[#DFF6E6] text-[#0FA24A]"><Icon name="wrench" class="size-[20px]" /></span><div class="min-w-0 flex-1"><h3 class="text-[12px] leading-[18px] font-medium">{{ order.serviceTitle }}</h3><p class="text-[12px] text-[#6B7280]">{{ order.providerName }}</p></div><button type="button" :class="outline" @click="explore(order.serviceTitle)">Ver</button></article></div><p v-else class="py-[12px] text-[12px] leading-[18px] text-[#6B7280]">Os serviços concluídos aparecerão aqui para voltar a contratar com facilidade.</p></section>
          <section :class="panel" class="bg-[#F8FCF9]! p-[14px]"><div class="mb-[14px] flex items-center justify-between"><h2 class="text-[13px] leading-[20px] font-medium">Como funciona?</h2><RouterLink to="/help" :class="textAction">Ver mais</RouterLink></div><ol class="m-0 flex list-none flex-col gap-[18px] pt-[4px] pb-[10px]"><li v-for="(step, i) in steps" :key="step.title" class="flex items-start gap-[20px]"><span class="relative shrink-0"><span class="flex size-[44px] items-center justify-center rounded-full border border-[#E8F5EC] bg-[#E8F5EC] text-[#0FA24A]"><Icon :name="step.icon" class="size-[23px]" /></span><span class="absolute top-[1px] -right-[33px] flex size-[18px] items-center justify-center rounded-full bg-[#0FA24A] text-[9.5px] font-medium text-white">{{ i + 1 }}</span></span><div class="pl-[23px]"><h3 class="mb-[3px] text-[12px] leading-[18px] font-medium">{{ step.title }}</h3><p class="m-0 text-[11px] leading-[1.55] text-[#6B7280]">{{ step.description }}</p></div></li></ol></section>
        </aside>
      </div>
    </div>
  </div>
</template>
