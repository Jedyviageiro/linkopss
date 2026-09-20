<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppButton from '@/app/components/common/misc/AppButton.vue'
import SelectInput from '@/app/components/common/input/SelectInput.vue'
import Tabs from '@/app/components/common/misc/Tabs.vue'
import ServiceExplorerHero from '@/app/components/partial/services/ServiceExplorerHero.vue'
import ServiceFiltersPanel, { type ServiceFilters } from '@/app/components/partial/services/ServiceFiltersPanel.vue'
import ServiceCatalogCard from '@/app/components/partial/services/ServiceCatalogCard.vue'
import ServiceProfileDialog from '@/app/components/partial/services/ServiceProfileDialog.vue'
import Icon, { type DashboardIconName } from '@/app/components/DashboardIcon.vue'
import { categoriesApi } from '@/features/categories/api/categories-api'
import type { Category } from '@/features/categories/types/category'
import { servicesApi } from '@/features/services/api/services-api'
import type { ServiceOffering } from '@/features/services/types/service'
import { providersApi } from '@/features/providers/api/providers-api'
import type { Provider } from '@/features/providers/types/provider'
import { mediaApi } from '@/features/media/api/media-api'
import { useAuthStore } from '@/features/auth/stores/auth-store'
import { notificationsApi } from '@/features/notifications/api/notifications-api'
import { getErrorMessage } from '@/shared/api/api-error'

interface Result { service: ServiceOffering; provider: Provider | null; photo: string | null }
const route = useRoute(), router = useRouter(), auth = useAuthStore()
const textQuery = (key: string) => typeof route.query[key] === 'string' ? String(route.query[key]) : ''
const search = ref(textQuery('q'))
const filters = reactive<ServiceFilters>({ category: textQuery('category'), city: textQuery('city'), price: '', type: '', rating: 0 })
const applied = ref({ ...filters })
const categories = ref<Category[]>([]), results = ref<Result[]>([])
const loading = ref(false), error = ref(''), showFilters = ref(false), allCategories = ref(false)
const categoryError = ref(''), favoriteError = ref('')
const tab = ref<'all' | 'rated' | 'recent'>('all'), sort = ref('relevance'), page = ref(1), unread = ref(0)
const selected = ref<Result | null>(null), favorites = ref<string[]>([])
const favoriteKey = computed(() => `linkops.favorites.${auth.user?.id ?? 'guest'}`)
const categoryOptions = computed(() => categories.value.flatMap(c => [c, ...(c.children ?? [])]))
const tiles: { title: string; match: string; icon: DashboardIconName; color: string }[] = [
  { title: 'Reparações para Casa', match: 'repar', icon: 'home', color: '#008c39' }, { title: 'Limpeza e Casa', match: 'limpeza', icon: 'spray', color: '#0089ff' },
  { title: 'Babás e Cuidadores', match: 'bab', icon: 'care', color: '#a34cff' }, { title: 'Fotografia e Eventos', match: 'fotografia', icon: 'camera', color: '#ff7017' },
  { title: 'Beleza', match: 'beleza', icon: 'beauty', color: '#ff3892' }, { title: 'Transporte e Entregas', match: 'transport', icon: 'truck', color: '#009b43' },
]
const slugFor = (match: string) => categoryOptions.value.find(c => `${c.name} ${c.slug}`.toLowerCase().includes(match))?.slug
const visible = computed(() => {
  const f = applied.value
  const ranges: Record<string, [number, number]> = { low: [0, 500], mid: [500, 1000], high: [1000, 2500], premium: [2500, Infinity] }
  const range = ranges[f.price]
  const rows = results.value.filter(({ service: item, provider }) => (!f.type || item.priceType === f.type) && (!f.rating || (provider?.averageRating ?? 0) >= f.rating) && (!range || (item.price !== null && item.price >= range[0] && item.price <= range[1])))
  return [...rows].sort((a, b) => sort.value === 'price' ? (a.service.price ?? Infinity) - (b.service.price ?? Infinity) : (tab.value === 'rated' || sort.value === 'rating') ? (b.provider?.averageRating ?? 0) - (a.provider?.averageRating ?? 0) : tab.value === 'recent' ? Date.parse(b.service.createdAt) - Date.parse(a.service.createdAt) : 0)
})
const featured = computed(() => [...visible.value].sort((a, b) => (b.provider?.averageRating ?? 0) - (a.provider?.averageRating ?? 0)).slice(0, 5))
const pageCount = computed(() => Math.ceil(visible.value.length / 8))
const pageItems = computed(() => visible.value.slice((page.value - 1) * 8, page.value * 8))
const tabItems = [{ id: 'all' as const, label: 'Todos os serviços' }, { id: 'rated' as const, label: 'Mais bem avaliados' }, { id: 'recent' as const, label: 'Mais recentes' }]

let requestId = 0
async function load() {
  const id = ++requestId; loading.value = true; error.value = ''; page.value = 1
  try {
    const query = { q: textQuery('q'), category: textQuery('category'), city: textQuery('city'), size: 50, sort: 'title,asc' }
    const first = await servicesApi.list(query); const services = [...first.content]
    for (let n = 1; n < first.page.totalPages; n++) { if (id !== requestId) return; services.push(...(await servicesApi.list({ ...query, page: n })).content) }
    const cache = new Map<string, Promise<Provider>>()
    const hydrated = await Promise.all(services.map(async service => { if (!cache.has(service.providerId)) cache.set(service.providerId, providersApi.get(service.providerId)); const [provider, images] = await Promise.allSettled([cache.get(service.providerId)!, mediaApi.listServiceImages(service.id)]); return { service, provider: provider.status === 'fulfilled' ? provider.value : null, photo: images.status === 'fulfilled' ? images.value[0]?.url ?? null : null } }))
    if (id === requestId) results.value = hydrated
  } catch (cause) { if (id === requestId) { error.value = getErrorMessage(cause); results.value = [] } } finally { if (id === requestId) loading.value = false }
}
async function apply() { applied.value = { ...filters }; page.value = 1; showFilters.value = false; await router.replace({ name: 'services', query: { ...(search.value ? { q: search.value } : {}), ...(filters.category ? { category: filters.category } : {}), ...(filters.city ? { city: filters.city } : {}) } }) }
function clear() { search.value = ''; Object.assign(filters, { category: '', city: '', price: '', type: '', rating: 0 }); void apply() }
function choose(match: string) { const slug = slugFor(match); if (slug) { filters.category = slug; search.value = '' } else { filters.category = ''; search.value = tiles.find(tile => tile.match === match)?.title ?? match }; void apply() }
function favorite(id: string) { const next = favorites.value.includes(id) ? favorites.value.filter(value => value !== id) : [...favorites.value, id]; try { localStorage.setItem(favoriteKey.value, JSON.stringify(next)); favorites.value = next; favoriteError.value = '' } catch { favoriteError.value = 'Não foi possível guardar o favorito neste dispositivo.' } }
function openProfile(item: Result) { selected.value = item }
function updateFilters(value: ServiceFilters) { Object.assign(filters, value) }
watch(() => route.query, () => { search.value = textQuery('q'); filters.category = textQuery('category'); filters.city = textQuery('city'); void load() })
watch([tab, sort], () => { page.value = 1 })
onMounted(async () => { try { const stored = JSON.parse(localStorage.getItem(favoriteKey.value) || '[]'); favorites.value = Array.isArray(stored) ? stored.filter(value => typeof value === 'string') : [] } catch { favorites.value = [] }; void load(); try { categories.value = await categoriesApi.list() } catch { categoryError.value = 'Não foi possível carregar as categorias. Atualize a página para tentar novamente.' }; if (auth.isAuthenticated) { try { unread.value = (await notificationsApi.list({ size: 50 })).content.filter(notification => !notification.read).length } catch { /* Header count is optional. */ } } })
</script>

<template>
  <main class="min-w-0 pb-6 text-deep-navy">
    <ServiceExplorerHero :search="search" @update:search="search = $event" @submit="apply" @filters="showFilters = !showFilters" @popular="search = $event; apply()" />
    <div class="grid grid-cols-[205px_minmax(0,1fr)] items-start gap-4 px-7 max-[1200px]:grid-cols-[190px_minmax(0,1fr)] max-[760px]:grid-cols-1 max-[600px]:px-4">
      <ServiceFiltersPanel :filters="filters" :categories="categories" :category-options="categoryOptions" :all-categories="allCategories" :open="showFilters" @update:filters="updateFilters" @update:all-categories="allCategories = $event" @apply="apply" @clear="clear" @back="router.push({ name: 'dashboard' })" />
      <div class="grid min-w-0 gap-4">
        <p v-if="categoryError || favoriteError" class="m-0 rounded-md border border-red-100 bg-red-50 px-3 py-2 text-xs text-red-700" role="alert">{{ categoryError || favoriteError }}</p>
        <section class="rounded-lg max-w-full overflow-x-auto border border-linkops-slate-200 bg-white p-3">
          <div class="mb-2 flex items-center justify-between"><h2 class="m-0 text-[13px] font-medium">Categorias</h2><button type="button" class="border-0 bg-transparent p-0 text-[11px] text-linkops-green hover:underline" @click="allCategories = !allCategories; showFilters = true">Ver todas</button></div>
          <div class="flex gap-2.5 overflow-x-auto pb-1"><button v-for="tile in tiles" :key="tile.match" type="button" class="flex h-26 min-w-28 flex-col items-start justify-center gap-2 rounded-lg border border-slate-200 bg-white px-2.5 text-left text-xs font-medium hover:border-linkops-green" :class="{ 'border-green-300 bg-green-50 text-green-800': filters.category && filters.category === slugFor(tile.match) }" @click="choose(tile.match)"><Icon :name="tile.icon" class="size-8" :style="{ color: tile.color }" />{{ tile.title }}</button><button type="button" class="flex h-26 min-w-28 flex-col items-start justify-center rounded-lg border border-slate-200 px-2.5 text-left text-xs text-slate-500 hover:border-linkops-green" @click="allCategories = true; showFilters = true"><span class="text-2xl">•••</span>Mais categorias</button></div>
          <div v-if="allCategories" class="mt-3 flex flex-wrap gap-2"><button v-for="category in categoryOptions" :key="category.id" type="button" class="rounded-full border border-slate-200 bg-white px-2.5 py-1 text-[10px] text-slate-600 hover:border-linkops-green" @click="filters.category = category.slug; search = ''; apply()">{{ category.name }}</button></div>
        </section>
        <section class="rounded-lg border border-linkops-slate-200 bg-white p-3"><div class="mb-2 flex items-center justify-between"><h2 class="m-0 text-[13px] font-medium">Profissionais em destaque</h2><a href="#all-services" class="text-[11px] text-linkops-green hover:underline">Ver todos</a></div><p v-if="loading" class="p-6 text-center text-xs text-slate-500" role="status">A carregar profissionais…</p><p v-else-if="!featured.length" class="p-6 text-center text-xs text-slate-500">Sem profissionais para estes filtros.</p><div v-else class="grid grid-cols-4 gap-3 overflow-x-auto max-[760px]:grid-cols-[repeat(2,minmax(210px,1fr))]"><ServiceCatalogCard v-for="item in featured" :key="item.service.id" :item="item" variant="featured" :saved="favorites.includes(item.service.id)" @favorite="favorite(item.service.id)" @open="openProfile(item)" /></div></section>
        <section id="all-services" class="rounded-lg border border-linkops-slate-200 bg-white px-3 pb-3"><div class="flex min-h-12 items-center justify-between gap-4"><Tabs v-model="tab" :tabs="tabItems" /><label class="flex items-center gap-2 text-[11px] text-slate-500 max-[600px]:hidden">Ordenar por<SelectInput v-model="sort" :options="[{ label: 'Relevância', value: 'relevance' }, { label: 'Menor preço', value: 'price' }, { label: 'Avaliação', value: 'rating' }]" aria-label="Ordenar serviços" class="w-32" /></label></div><div v-if="error" class="p-6 text-center text-xs text-red-700" role="alert">{{ error }} <button type="button" class="text-linkops-green underline" @click="load">Tentar novamente</button></div><p v-else-if="loading" class="p-6 text-center text-xs text-slate-500" role="status">A carregar serviços…</p><div v-else-if="!pageItems.length" class="p-6 text-center text-xs text-slate-500">Nenhum serviço encontrado. <button type="button" class="text-linkops-green underline" @click="clear">Limpar filtros</button></div><div v-else class="grid gap-2.5"><ServiceCatalogCard v-for="item in pageItems" :key="item.service.id" :item="item" variant="result" :saved="favorites.includes(item.service.id)" @favorite="favorite(item.service.id)" @open="openProfile(item)" /></div><nav v-if="pageCount > 1" class="flex items-center justify-center gap-5 pt-4 text-xs" aria-label="Paginação"><button type="button" class="border-0 bg-transparent text-linkops-green disabled:text-slate-300" :disabled="page === 1" @click="page--">Anterior</button><span>{{ page }} / {{ pageCount }}</span><button type="button" class="border-0 bg-transparent text-linkops-green disabled:text-slate-300" :disabled="page === pageCount" @click="page++">Seguinte</button></nav></section>
      </div>
    </div>
    <ServiceProfileDialog :item="selected" @close="selected = null" />
  </main>
</template>
