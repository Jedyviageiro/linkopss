<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppButton from '@/app/components/common/misc/AppButton.vue'
import SelectInput from '@/app/components/common/input/SelectInput.vue'
import Tabs from '@/app/components/common/misc/Tabs.vue'
import HistoryAside from '@/app/components/partial/history/HistoryAside.vue'
import HistoryServiceCard from '@/app/components/partial/history/HistoryServiceCard.vue'
import HistorySummaryPanel from '@/app/components/partial/history/HistorySummaryPanel.vue'
import { mockBookings } from '@/app/data/mock'
import type { Booking } from '../types/booking'

const router = useRouter()
type HistoryTab = 'all' | 'completed' | 'active' | 'cancelled'
type HistorySort = 'recent' | 'oldest'
const bookings = ref<Booking[]>([])
const activeTab = ref<HistoryTab>('all')
const sort = ref<HistorySort>('recent')
const loading = ref(true)
const error = ref('')
const sortOptions = [{ label: 'Mais recentes', value: 'recent' }, { label: 'Mais antigos', value: 'oldest' }]
const activeStatuses = ['PENDING', 'ACCEPTED', 'IN_PROGRESS'] as const
const completed = computed(() => bookings.value.filter(booking => booking.status === 'COMPLETED'))
const active = computed(() => bookings.value.filter(booking => activeStatuses.includes(booking.status as typeof activeStatuses[number])))
const cancelled = computed(() => bookings.value.filter(booking => booking.status === 'CANCELLED'))
const totalSpent = computed(() => completed.value.reduce((total, booking) => total + (booking.price ?? 0), 0))
const tabs = computed(() => [
  { id: 'all' as const, label: 'Todos' },
  { id: 'completed' as const, label: 'Concluídos', count: completed.value.length },
  { id: 'active' as const, label: 'Em andamento', count: active.value.length },
  { id: 'cancelled' as const, label: 'Cancelados', count: cancelled.value.length },
])
const visibleBookings = computed(() => {
  const filtered = bookings.value.filter(booking => activeTab.value === 'all' || (activeTab.value === 'completed' && booking.status === 'COMPLETED') || (activeTab.value === 'active' && activeStatuses.includes(booking.status as typeof activeStatuses[number])) || (activeTab.value === 'cancelled' && booking.status === 'CANCELLED'))
  return [...filtered].sort((first, second) => {
    const direction = sort.value === 'recent' ? -1 : 1
    return direction * (Date.parse(first.createdAt) - Date.parse(second.createdAt))
  })
})

function imageFor(booking: Booking) {
  const title = booking.serviceTitle.toLocaleLowerCase('pt-MZ')
  if (title.includes('canal')) return '/images/plumber-service-demo.jpg'
  if (title.includes('limpeza')) return '/images/cleaning-service-demo.jpg'
  if (title.includes('ar condicionado')) return '/images/ac-service-demo.jpg'
  if (title.includes('fotografia')) return '/images/photography-service-demo.jpg'
  return undefined
}

function load() {
  loading.value = true
  error.value = ''
  bookings.value = mockBookings.map(booking => ({ ...booking }))
  loading.value = false
}

function ratingFor(booking: Booking) {
  const ratings: Record<string, number> = { 'Reparação de canalização': 4.9, 'Limpeza profunda da casa': 4.7, 'Instalação de ar condicionado': 4.8, 'Manutenção de jardim': 4.8, 'Pintura de interiores': 4.6 }
  return ratings[booking.serviceTitle]
}

function openDetails(booking: Booking) {
  void router.push({ name: 'bookings', query: { booking: booking.id } })
}

onMounted(load)
</script>

<template>
  <div class="mx-auto max-w-287.5 container px-7 pb-10 max-[600px]:px-4">
    <header class="flex items-start justify-between gap-6 pt-3.25 pb-4 max-[720px]:flex-col max-[720px]:gap-3">
      <div><h1 class="m-0 text-[28px] leading-[1.15] font-semibold tracking-[-1px] text-deep-navy">Histórico de serviços</h1><p class="mt-1 mb-0 text-sm text-linkops-slate-500">Veja todos os serviços que você contratou e o que pagou.</p></div>
      <SelectInput v-model="sort" :options="sortOptions" aria-label="Ordenar histórico" class="w-36" />
    </header>
    <div class="grid grid-cols-[minmax(0,1fr)_262px] items-start gap-5 max-[950px]:grid-cols-1">
      <main class="min-w-0">
        <Tabs v-model="activeTab" :tabs="tabs" />
        <div v-if="error" class="mt-4 rounded-lg border border-red-100 bg-red-50 p-6 text-center" role="alert"><p class="m-0 text-sm text-red-700">{{ error }}</p><AppButton class="mt-4" variant="secondary" size="sm" @click="load">Tentar novamente</AppButton></div>
        <div v-else-if="loading" class="mt-4 grid gap-2.5" aria-busy="true"><div v-for="item in 5" :key="item" class="h-28 animate-pulse rounded-lg border border-slate-200 bg-slate-50"></div></div>
        <div v-else-if="!visibleBookings.length" class="mt-4 rounded-lg border border-dashed border-slate-200 p-10 text-center"><p class="m-0 text-sm text-slate-500">Não existem serviços neste filtro.</p><AppButton class="mt-4" size="sm" @click="router.push({ name: 'services' })">Explorar serviços</AppButton></div>
        <div v-else class="mt-4 grid gap-2.5"><HistoryServiceCard v-for="booking in visibleBookings" :key="booking.id" :booking="booking" :image-url="imageFor(booking)" :rating="ratingFor(booking)" @details="openDetails(booking)" /></div>
      </main>
      <div class="grid gap-3.5"><HistorySummaryPanel :completed="completed.length" :active="active.length" :cancelled="cancelled.length" :total-spent="totalSpent" /><HistoryAside /></div>
    </div>
  </div>
</template>
