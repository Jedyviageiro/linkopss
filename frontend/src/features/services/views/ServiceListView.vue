<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import banner from '@/assets/photos/explorer-banner.png'
import Icon, { type DashboardIconName } from '@/app/components/DashboardIcon.vue'
import MarketplaceAvatar from '@/app/components/MarketplaceAvatar.vue'
import MarketplaceSidebar from '@/app/components/MarketplaceSidebar.vue'
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
const filters = reactive({ category: textQuery('category'), city: textQuery('city'), price: '', type: '', rating: 0 })
const applied = ref({ ...filters })
const categories = ref<Category[]>([]), results = ref<Result[]>([])
const loading = ref(false), error = ref(''), showFilters = ref(false), allCategories = ref(false)
const categoryError = ref(''), favoriteError = ref('')
const tab = ref('all'), sort = ref('relevance'), page = ref(1), unread = ref(0)
const selected = ref<Result | null>(null), dialog = ref<HTMLDialogElement | null>(null)
const favorites = ref<string[]>([])
const favoriteKey = computed(() => `linkops.favorites.${auth.user?.id ?? 'guest'}`)
const fullName = computed(() => [auth.user?.firstName, auth.user?.lastName].filter(Boolean).join(' '))
const initials = computed(() => fullName.value.split(' ').map(n => n[0]).slice(0, 2).join(''))
const categoryOptions = computed(() => categories.value.flatMap(c => [c, ...(c.children ?? [])]))
const tiles: { title: string; match: string; icon: DashboardIconName; color: string }[] = [
  { title: 'Reparações para Casa', match: 'repar', icon: 'home', color: '#008c39' },
  { title: 'Limpeza e Casa', match: 'limpeza', icon: 'spray', color: '#0089ff' },
  { title: 'Babás e Cuidadores', match: 'bab', icon: 'care', color: '#a34cff' },
  { title: 'Fotografia e Eventos', match: 'fotografia', icon: 'camera', color: '#ff7017' },
  { title: 'Beleza', match: 'beleza', icon: 'beauty', color: '#ff3892' },
  { title: 'Transporte e Entregas', match: 'transport', icon: 'truck', color: '#009b43' },
]
const slugFor = (match: string) => categoryOptions.value.find(c => `${c.name} ${c.slug}`.toLowerCase().includes(match))?.slug
const visible = computed(() => {
  const f = applied.value
  const ranges: Record<string, [number, number]> = { low: [0, 500], mid: [500, 1000], high: [1000, 2500], premium: [2500, Infinity] }
  const range = ranges[f.price]
  const rows = results.value.filter(({ service: s, provider: p }) =>
    (!f.type || s.priceType === f.type) && (!f.rating || (p?.averageRating ?? 0) >= f.rating) &&
    (!range || (s.price !== null && s.price >= range[0] && s.price <= range[1])))
  return rows.sort((a, b) => {
    if (sort.value === 'price') return (a.service.price ?? Infinity) - (b.service.price ?? Infinity)
    if (tab.value === 'rated' || sort.value === 'rating') return (b.provider?.averageRating ?? 0) - (a.provider?.averageRating ?? 0)
    if (tab.value === 'recent') return Date.parse(b.service.createdAt) - Date.parse(a.service.createdAt)
    return 0
  })
})
const featured = computed(() => [...visible.value].sort((a, b) => (b.provider?.averageRating ?? 0) - (a.provider?.averageRating ?? 0)).slice(0, 5))
const pageCount = computed(() => Math.ceil(visible.value.length / 8))
const pageItems = computed(() => visible.value.slice((page.value - 1) * 8, page.value * 8))
const price = (s: ServiceOffering) => s.priceType === 'NEGOTIABLE' ? 'Preço negociável' : `Desde ${new Intl.NumberFormat('pt-MZ', { maximumFractionDigits: 0 }).format(s.price ?? 0)} MT`
const rating = (p: Provider | null) => p?.averageRating ? p.averageRating.toLocaleString('pt-MZ', { minimumFractionDigits: 1 }) : 'Novo'
let requestId = 0
async function load() {
  const id = ++requestId
  loading.value = true; error.value = ''; page.value = 1
  try {
    const query = { q: textQuery('q'), category: textQuery('category'), city: textQuery('city'), size: 50, sort: 'title,asc' }
    const first = await servicesApi.list(query)
    const services = [...first.content]
    for (let n = 1; n < first.page.totalPages; n++) {
      if (id !== requestId) return
      services.push(...(await servicesApi.list({ ...query, page: n })).content)
    }
    const cache = new Map<string, Promise<Provider>>()
    const hydrated = await Promise.all(services.map(async service => {
      if (!cache.has(service.providerId)) cache.set(service.providerId, providersApi.get(service.providerId))
      const [provider, images] = await Promise.allSettled([cache.get(service.providerId)!, mediaApi.listServiceImages(service.id)])
      return { service, provider: provider.status === 'fulfilled' ? provider.value : null, photo: images.status === 'fulfilled' ? images.value[0]?.url ?? null : null }
    }))
    if (id === requestId) results.value = hydrated
  } catch (e) { if (id === requestId) { error.value = getErrorMessage(e); results.value = [] } }
  finally { if (id === requestId) loading.value = false }
}
async function apply() {
  applied.value = { ...filters }; page.value = 1; showFilters.value = false
  await router.replace({ name: 'services', query: { ...(search.value ? { q: search.value } : {}), ...(filters.category ? { category: filters.category } : {}), ...(filters.city ? { city: filters.city } : {}) } })
}
function clear() { search.value = ''; Object.assign(filters, { category: '', city: '', price: '', type: '', rating: 0 }); void apply() }
function choose(match: string) {
  const slug = slugFor(match)
  if (slug) { filters.category = slug; search.value = '' } else { filters.category = ''; search.value = tiles.find(t => t.match === match)?.title ?? match }
  void apply()
}
function favorite(id: string) {
  const next = favorites.value.includes(id) ? favorites.value.filter(v => v !== id) : [...favorites.value, id]
  try { localStorage.setItem(favoriteKey.value, JSON.stringify(next)); favorites.value = next; favoriteError.value = '' } catch { favoriteError.value = 'Não foi possível guardar o favorito neste dispositivo.' }
}
async function openProfile(item: Result) { selected.value = item; await nextTick(); dialog.value?.showModal() }
watch(() => route.query, () => { search.value = textQuery('q'); filters.category = textQuery('category'); filters.city = textQuery('city'); void load() })
watch([tab, sort], () => { page.value = 1 })
onMounted(async () => {
  try { const stored = JSON.parse(localStorage.getItem(favoriteKey.value) || '[]'); favorites.value = Array.isArray(stored) ? stored.filter(v => typeof v === 'string') : [] } catch { /* Storage is optional. */ }
  void load()
  try { categories.value = await categoriesApi.list() } catch { categoryError.value = 'Não foi possível carregar as categorias. Atualize a página para tentar novamente.' }
  if (auth.isAuthenticated) { try { unread.value = (await notificationsApi.list({ size: 50 })).content.filter(n => !n.read).length } catch { /* Keep notifications accessible. */ } }
})
</script>

<template>
  <div class="marketplace-shell">
  <MarketplaceSidebar :notifications="unread" />
  <main class="explorer">
    <section class="explore-hero">
      <div class="banner-art">
        <img class="banner" :src="banner" width="2172" height="724" fetchpriority="high" alt="Ponte de Maputo. Serviços locais, mais oportunidades." />
      </div>
      <header class="topbar">
        <button class="location" @click="showFilters = true"><Icon name="pin" />{{ filters.city || 'Todas as cidades' }}<span v-if="filters.city === 'Maputo'">, Moçambique</span><Icon name="chevron-down" /></button>
        <div class="account"><RouterLink class="notification" to="/notifications" :aria-label="`Notificações${unread ? `: ${unread} por ler` : ''}`"><Icon name="bell" /><i v-if="unread" /></RouterLink><RouterLink v-if="auth.isAuthenticated" to="/profile" class="profile"><span class="initials">{{ initials }}</span><span>{{ fullName }}</span><Icon name="chevron-down" /></RouterLink><RouterLink v-else to="/login">Entrar</RouterLink></div>
      </header>
      <div class="hero-copy"><h1>Explore serviços perto de você</h1><p>Encontre profissionais de confiança para o que precisa, quando precisa.</p></div>
      <div class="search-area"><form class="search-form" @submit.prevent="apply"><label class="search-field"><Icon name="search" /><input v-model.trim="search" type="search" placeholder="De que serviço precisa?" aria-label="Pesquisar serviços" /></label><button class="primary" type="submit">Buscar</button><button type="button" class="filter-toggle" :aria-expanded="showFilters" aria-controls="explore-filters" @click="showFilters = !showFilters"><Icon name="sliders" />Filtros</button></form>
        <div class="popular"><span>Popular:</span><button v-for="term in ['Canalizador', 'Limpeza de casa', 'Eletricista', 'Ar-condicionado', 'Fotógrafo', 'Babá', 'Pintor']" :key="term" @click="search = term; apply()">{{ term }}</button></div>
      </div>
    </section>
    <div class="catalog-layout">
      <aside id="explore-filters" class="filter-panel panel" :class="{ open: showFilters }">
        <div class="section-heading"><h2>Filtros</h2><button class="text-action" @click="clear">Limpar tudo</button></div>
        <form @submit.prevent="apply">
          <details open><summary>Categoria<Icon name="chevron-down" /></summary><label v-for="category in (allCategories ? categoryOptions : categories.slice(0, 5))" :key="category.id" class="check"><input v-model="filters.category" type="radio" :value="category.slug" name="category" />{{ category.name }}</label><button class="text-action" type="button" @click="allCategories = !allCategories">{{ allCategories ? 'Ver menos categorias' : 'Ver todas as categorias' }}</button></details>
          <details open><summary>Localização<Icon name="chevron-down" /></summary><label class="city-field"><Icon name="pin" /><input v-model.trim="filters.city" placeholder="Todas as cidades" aria-label="Cidade" /></label><select disabled aria-label="Distância indisponível sem localização" title="A pesquisa por raio requer uma localização geográfica."><option>Raio indisponível</option></select></details>
          <details open><summary>Preço<Icon name="chevron-down" /></summary><label v-for="option in [{ value: 'low', label: 'Até 500 MT' }, { value: 'mid', label: '500 – 1.000 MT' }, { value: 'high', label: '1.000 – 2.500 MT' }, { value: 'premium', label: 'Mais de 2.500 MT' }]" :key="option.value" class="check"><input v-model="filters.price" type="radio" name="price" :value="option.value" />{{ option.label }}</label></details>
          <details open><summary>Tipo de preço<Icon name="chevron-down" /></summary><label class="check"><input v-model="filters.type" type="radio" value="FIXED" name="priceType" />Preço fixo</label><label class="check"><input v-model="filters.type" type="radio" value="NEGOTIABLE" name="priceType" />Negociável</label></details>
          <details open><summary>Avaliação<Icon name="chevron-down" /></summary><label v-for="n in [4, 3, 2]" :key="n" class="check rating-option"><input v-model="filters.rating" type="radio" name="rating" :value="n" /><span aria-hidden="true"><span class="gold">{{ '★'.repeat(n) }}</span><span class="gray">{{ '★'.repeat(5 - n) }}</span></span>{{ n }}+</label></details>
          <button class="primary apply" type="submit">Aplicar filtros</button>
        </form>
        <RouterLink class="back-home" to="/dashboard">← Voltar ao início</RouterLink>
      </aside>
      <div class="catalog-main">
        <p v-if="categoryError" role="alert" class="status error">{{ categoryError }}</p>
        <p v-if="favoriteError" role="alert" class="status error">{{ favoriteError }}</p>
        <section class="panel categories"><div class="section-heading"><h2>Categorias</h2><button class="text-action" @click="allCategories = !allCategories; showFilters = true">Ver todas</button></div><div class="category-grid"><button v-for="tile in tiles" :key="tile.match" class="category-tile" :class="{ active: filters.category && filters.category === slugFor(tile.match) }" @click="choose(tile.match)"><Icon :name="tile.icon" :style="{ color: tile.color }" /><span>{{ tile.title }}</span></button><button class="category-tile" @click="allCategories = true; showFilters = true"><span class="dots">•••</span><span>Mais<br />Categorias</span></button></div><div v-if="allCategories" class="extra-categories"><button v-for="category in categoryOptions" :key="category.id" @click="filters.category = category.slug; search = ''; apply()">{{ category.name }}</button></div></section>
        <section class="panel featured"><div class="section-heading"><h2>Profissionais em destaque</h2><a class="text-action" href="#all-services">Ver todos</a></div><p v-if="loading" class="status" role="status">A carregar profissionais…</p><p v-else-if="!featured.length" class="status">Sem profissionais para estes filtros.</p><div v-else class="featured-grid"><article v-for="item in featured" :key="item.service.id" class="feature-card"><div class="feature-photo"><img v-if="item.photo" :src="item.photo" :alt="item.service.title" /><Icon v-else name="wrench" class="placeholder-icon" /><button class="favorite" :class="{ saved: favorites.includes(item.service.id) }" :aria-pressed="favorites.includes(item.service.id)" aria-label="Guardar serviço nos favoritos" @click="favorite(item.service.id)"><Icon name="heart" /></button></div><div class="feature-body"><button class="service-title" @click="openProfile(item)">{{ item.service.title }}</button><div class="provider-line"><MarketplaceAvatar :provider="item.provider" :fallback-name="item.service.providerName" class="avatar" /><span>{{ item.service.providerName }}</span><span class="rating"><Icon v-if="item.provider?.averageRating" name="star" />{{ rating(item.provider) }}</span></div><p class="city"><Icon name="pin" />{{ item.service.city }}</p><div class="price-line"><strong>{{ price(item.service) }}</strong><span v-if="item.provider?.verified" class="verified"><Icon name="shield" />Verificado</span></div></div></article></div></section>
        <section id="all-services" class="panel service-results" aria-label="Resultados da pesquisa" :aria-busy="loading"><div class="results-toolbar"><div class="tabs" aria-label="Ordenar serviços"><button v-for="t in [{ id: 'all', name: 'Todos os serviços' }, { id: 'rated', name: 'Mais bem avaliados' }, { id: 'recent', name: 'Mais recentes' }]" :key="t.id" :class="{ active: tab === t.id }" :aria-pressed="tab === t.id" @click="tab = t.id">{{ t.name }}</button></div><label class="sort">Ordenar por<select v-model="sort"><option value="relevance">Relevância</option><option value="price">Menor preço</option><option value="rating">Avaliação</option></select></label></div>
          <div v-if="error" class="status error" role="alert">{{ error }}<button @click="load">Tentar novamente</button></div><p v-else-if="loading" class="status" role="status">A carregar serviços…</p><div v-else-if="!pageItems.length" class="status"><p>Nenhum serviço encontrado. Experimente outros filtros.</p><button class="text-action" @click="clear">Limpar filtros</button></div>
          <div v-else class="result-list"><article v-for="item in pageItems" :key="item.service.id" class="result-card"><img v-if="item.photo" class="result-photo" :src="item.photo" :alt="item.service.title" loading="lazy" /><div v-else class="result-photo fallback"><Icon name="wrench" /></div><div class="result-details"><button class="service-title" @click="openProfile(item)">{{ item.service.title }}</button><div class="provider-line"><MarketplaceAvatar :provider="item.provider" :fallback-name="item.service.providerName" class="avatar" /><span>{{ item.service.providerName }}</span><span class="rating"><Icon v-if="item.provider?.averageRating" name="star" />{{ rating(item.provider) }}</span><span class="city"><Icon name="pin" />{{ item.service.city }}</span></div><p class="description">{{ item.service.description || 'Consulte o perfil para conhecer este serviço.' }}</p><span class="tag">{{ item.service.priceType === 'FIXED' ? 'Preço fixo' : 'Negociável' }}</span></div><div class="result-action"><button class="favorite" :class="{ saved: favorites.includes(item.service.id) }" :aria-pressed="favorites.includes(item.service.id)" aria-label="Guardar serviço nos favoritos" @click="favorite(item.service.id)"><Icon name="heart" /></button><strong>{{ price(item.service) }}</strong><button class="primary" @click="openProfile(item)">Ver perfil</button></div></article></div>
          <nav v-if="pageCount > 1" class="pagination" aria-label="Paginação"><button :disabled="page === 1" @click="page--">Anterior</button><span>{{ page }} / {{ pageCount }}</span><button :disabled="page === pageCount" @click="page++">Seguinte</button></nav>
        </section>
      </div>
    </div>
    <dialog ref="dialog" class="provider-dialog" @click="(event) => { if (event.target === dialog) dialog?.close() }"><template v-if="selected"><button class="close-dialog" autofocus aria-label="Fechar perfil" @click="dialog?.close()">×</button><MarketplaceAvatar :provider="selected.provider" :fallback-name="selected.service.providerName" class="dialog-avatar" /><h2>{{ selected.service.providerName }}</h2><p>{{ selected.provider?.bio }}</p><h3>{{ selected.service.title }}</h3><p>{{ selected.service.description }}</p><p class="city"><Icon name="pin" />{{ selected.service.city }} · {{ rating(selected.provider) }}</p><strong>{{ price(selected.service) }}</strong></template></dialog>
  </main>
  </div>
</template>

<style scoped src="./service-explore.css"></style>
