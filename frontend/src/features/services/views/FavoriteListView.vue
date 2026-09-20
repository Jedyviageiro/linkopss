<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Heart } from 'lucide-vue-next'
import AppButton from '@/app/components/common/misc/AppButton.vue'
import Tabs from '@/app/components/common/misc/Tabs.vue'
import Filters, { type FavoriteSort } from '@/app/components/partial/services/Filters.vue'
import ServiceCard from '@/app/components/partial/services/ServiceCard.vue'
import { servicesApi } from '../api/services-api'
import type { ServiceOffering } from '../types/service'
import { providersApi } from '@/features/providers/api/providers-api'
import type { Provider } from '@/features/providers/types/provider'
import { mediaApi } from '@/features/media/api/media-api'
import { useAuthStore } from '@/features/auth/stores/auth-store'

interface FavoriteItem { service: ServiceOffering; provider: Provider | null; photo: string | null }
type FavoriteTab = 'all' | 'providers' | 'services' | 'categories'

const router = useRouter()
const auth = useAuthStore()
const favorites = ref<string[]>([])
const items = ref<FavoriteItem[]>([])
const search = ref('')
const activeTab = ref<FavoriteTab>('all')
const sort = ref<FavoriteSort>('recent')
const loading = ref(true)
const error = ref('')
const favoriteError = ref('')
const favoriteKey = computed(() => `linkops.favorites.${auth.user?.id ?? 'guest'}`)
const visibleItems = computed(() => {
  const term = search.value.toLocaleLowerCase('pt-MZ')
  const filtered = items.value.filter(item => {
    const matchesSearch = !term || `${item.service.title} ${item.service.providerName} ${item.service.categoryName}`.toLocaleLowerCase('pt-MZ').includes(term)
    const matchesTab = activeTab.value === 'all' || (activeTab.value === 'providers' && !!item.provider) || (activeTab.value === 'services') || (activeTab.value === 'categories' && !!item.service.categoryName)
    return matchesSearch && matchesTab
  })
  return [...filtered].sort((first, second) => {
    if (sort.value === 'rating') return (second.provider?.averageRating ?? 0) - (first.provider?.averageRating ?? 0)
    if (sort.value === 'price') return (first.service.price ?? Infinity) - (second.service.price ?? Infinity)
    return Date.parse(second.service.createdAt) - Date.parse(first.service.createdAt)
  })
})

const tabItems = computed(() => [
  { id: 'all' as const, label: 'Todos', count: items.value.length },
  { id: 'providers' as const, label: 'Profissionais', count: new Set(items.value.map(item => item.service.providerId)).size },
  { id: 'services' as const, label: 'Serviços', count: items.value.length },
  { id: 'categories' as const, label: 'Categorias', count: new Set(items.value.map(item => item.service.categoryId)).size },
])

function readFavoriteIds() {
  try {
    const saved: unknown = JSON.parse(localStorage.getItem(favoriteKey.value) || '[]')
    return Array.isArray(saved) ? saved.filter((id): id is string => typeof id === 'string') : []
  } catch {
    return []
  }
}

function saveFavoriteIds() {
  localStorage.setItem(favoriteKey.value, JSON.stringify(favorites.value))
}

async function load() {
  loading.value = true
  error.value = ''
  favoriteError.value = ''
  favorites.value = readFavoriteIds()
  if (!favorites.value.length) {
    items.value = []
    loading.value = false
    return
  }
  try {
    const response = await servicesApi.list({ size: 100, sort: 'createdAt,desc' })
    const selected = response.content.filter(service => favorites.value.includes(service.id))
    const providerCache = new Map<string, Promise<Provider>>()
    items.value = await Promise.all(selected.map(async service => {
      if (!providerCache.has(service.providerId)) providerCache.set(service.providerId, providersApi.get(service.providerId))
      const [provider, images] = await Promise.allSettled([providerCache.get(service.providerId)!, mediaApi.listServiceImages(service.id)])
      return {
        service,
        provider: provider.status === 'fulfilled' ? provider.value : null,
        photo: images.status === 'fulfilled' ? images.value[0]?.url ?? null : null,
      }
    }))
  } catch {
    error.value = 'Não foi possível carregar os seus favoritos.'
    items.value = []
  } finally {
    loading.value = false
  }
}

function removeFavorite(id: string) {
  favorites.value = favorites.value.filter(favoriteId => favoriteId !== id)
  items.value = items.value.filter(item => item.service.id !== id)
  try { saveFavoriteIds() } catch { favoriteError.value = 'Não foi possível atualizar os favoritos neste dispositivo.' }
}

function openService(item: FavoriteItem) {
  void router.push({ name: 'services', query: { q: item.service.title } })
}

watch(favoriteKey, () => { void load() })
onMounted(() => { void load() })
</script>

<template>
  <div class="mx-auto max-w-287.5 container px-7 pb-10 max-[600px]:px-4">
    <header class="flex items-start justify-between gap-6 pt-3.25 pb-4 max-[720px]:flex-col max-[720px]:gap-3">
      <div>
        <h1 class="m-0 text-[28px] leading-[1.15] font-semibold tracking-[-1px] text-deep-navy">Meus favoritos</h1>
        <p class="mt-1 mb-0 text-sm text-linkops-slate-500">Aqui estão os profissionais e serviços que você salvou.</p>
      </div>
      <Filters v-model:search="search" v-model:sort="sort" />
    </header>

    <Tabs v-model="activeTab" :tabs="tabItems" />

    <p v-if="favoriteError" class="mb-3 rounded-md border border-red-100 bg-red-50 px-3 py-2 text-xs text-red-700" role="alert">{{ favoriteError }}</p>
    <div v-if="error" class="rounded-lg border border-red-100 bg-red-50/30 p-8 text-center" role="alert"><p class="m-0 text-sm text-red-700">{{ error }}</p><AppButton class="mt-4" variant="secondary" size="sm" @click="load">Tentar novamente</AppButton></div>
    <div v-else-if="loading" class="grid grid-cols-4 gap-3 max-[1050px]:grid-cols-3 max-[720px]:grid-cols-2 max-[460px]:grid-cols-1" aria-busy="true" aria-label="A carregar favoritos"><div v-for="item in 8" :key="item" class="h-64 animate-pulse rounded-lg border border-linkops-slate-200 bg-slate-50"></div></div>
    <div v-else-if="!visibleItems.length" class="flex min-h-72 flex-col items-center justify-center rounded-lg border border-dashed border-linkops-slate-200 px-6 text-center"><Heart class="size-10 text-linkops-green" /><h2 class="mt-3 mb-1 text-base font-semibold text-deep-navy">{{ items.length ? 'Nenhum favorito encontrado' : 'Ainda não tem favoritos' }}</h2><p class="m-0 max-w-sm text-xs text-linkops-slate-500">{{ items.length ? 'Experimente outro termo de pesquisa.' : 'Guarde serviços de que gosta para encontrá-los rapidamente aqui.' }}</p><AppButton v-if="!items.length" class="mt-4" size="sm" @click="router.push({ name: 'services' })">Explorar serviços</AppButton></div>
    <div v-else class="pt-4 grid grid-cols-4 gap-3 max-[1050px]:grid-cols-3 max-[720px]:grid-cols-2 max-[460px]:grid-cols-1"><ServiceCard v-for="item in visibleItems" :key="item.service.id" :service="item.service" :provider="item.provider" :photo="item.photo" :saved="favorites.includes(item.service.id)" @remove="removeFavorite(item.service.id)" @open="openService(item)" /></div>

    <section v-if="!loading && !error && items.length" class="mt-4 flex items-center justify-between gap-4 rounded-lg border border-green-50 bg-green-50/70 px-4 py-3 max-[600px]:items-start max-[600px]:flex-col"><div class="flex items-center gap-3"><Heart class="size-7 shrink-0 text-linkops-green" /><div><h2 class="m-0 text-xs font-semibold text-deep-navy">Salve o que você gosta</h2><p class="m-0 text-[11px] text-linkops-slate-500">Encontre serviços de que confia e guarde-os para acessar facilmente mais tarde.</p></div></div><AppButton variant="secondary" size="sm" @click="router.push({ name: 'services' })">Explorar serviços</AppButton></section>
  </div>
</template>
