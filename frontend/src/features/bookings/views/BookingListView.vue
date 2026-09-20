<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from 'lucide-vue-next'
import Icon from '@/app/components/DashboardIcon.vue'
import AppButton from '@/app/components/common/misc/AppButton.vue'
import Tabs from '@/app/components/common/misc/Tabs.vue'
import BookingCard from '@/app/components/partial/bookings/BookingCard.vue'
import BookingsToolbar from '@/app/components/partial/bookings/BookingsToolbar.vue'
import BookingSkeleton from '@/app/components/partial/bookings/BookingSkeleton.vue'
import BookingState from '@/app/components/partial/bookings/BookingState.vue'
import { mockBookings } from '@/app/data/mock'
import type { Booking } from '@/features/bookings/types/booking'
import { useModalStore } from '@/shared/modals/modal-store'
import { useNotificationStore } from '@/shared/notifications/notification-store'

type BookingTab = 'all' | 'active' | 'completed' | 'cancelled'
type BookingSort = 'recent' | 'oldest'

const router = useRouter()
const modals = useModalStore()
const notices = useNotificationStore()
const bookings = ref<Booking[]>([])
const activeTab = ref<BookingTab>('all')
const search = ref('')
const sort = ref<BookingSort>('recent')
const showFilters = ref(false)
const loading = ref(true)
const error = ref('')
const cancellingId = ref('')

const activeStatuses = ['PENDING', 'ACCEPTED', 'IN_PROGRESS'] as const
const activeBookings = computed(() => bookings.value.filter(booking => activeStatuses.includes(booking.status as typeof activeStatuses[number])))
const tabItems = computed(() => [
  { id: 'all' as const, label: 'Todos' },
  { id: 'active' as const, label: 'Em andamento', count: activeBookings.value.length },
  { id: 'completed' as const, label: 'Concluídos' },
  { id: 'cancelled' as const, label: 'Cancelados' },
])
const filteredBookings = computed(() => {
  const term = search.value.toLocaleLowerCase('pt-MZ')
  return bookings.value.filter(booking => {
    const matchesTab = activeTab.value === 'all' ||
      (activeTab.value === 'active' && activeStatuses.includes(booking.status as typeof activeStatuses[number])) ||
      (activeTab.value === 'completed' && booking.status === 'COMPLETED') ||
      (activeTab.value === 'cancelled' && booking.status === 'CANCELLED')
    const searchable = `${booking.serviceTitle} ${booking.providerName} ${booking.id}`.toLocaleLowerCase('pt-MZ')
    return matchesTab && (!term || searchable.includes(term))
  })
})

async function load() {
  loading.value = true
  error.value = ''
  bookings.value = [...mockBookings].sort((first, second) => {
    const firstTime = new Date(first.createdAt).getTime()
    const secondTime = new Date(second.createdAt).getTime()
    return sort.value === 'oldest' ? firstTime - secondTime : secondTime - firstTime
  })
  loading.value = false
}

async function cancelBooking(booking: Booking) {
  const confirmed = await modals.open({
    kind: 'danger',
    title: 'Cancelar pedido?',
    message: 'O profissional será informado do cancelamento deste pedido.',
    confirmLabel: 'Cancelar pedido',
    cancelLabel: 'Manter pedido',
  })
  if (!confirmed) return

  cancellingId.value = booking.id
  bookings.value = bookings.value.map(item => item.id === booking.id
    ? { ...item, status: 'CANCELLED', updatedAt: new Date().toISOString() }
    : item)
  notices.success('Pedido cancelado.')
  cancellingId.value = ''
}

function openMessages(booking: Booking) {
  router.push({ name: 'messages', query: { booking: booking.id, provider: booking.providerId } })
}
function requestReview() { notices.info('A avaliação ficará disponível em breve.') }
function viewDetails() { notices.info('Os detalhes do pedido ficarão disponíveis em breve.') }
function clearFilters() { search.value = ''; showFilters.value = false }

onMounted(() => { void load() })
</script>

<template>
  <div class="mx-auto max-w-287.5 pb-10 container px-7">
    <section class="flex items-start justify-between gap-6 pt-3.25 pb-6 max-[600px]:flex-col max-[600px]:gap-3.5">
      <div>
        <h1 class="m-0 leading-[1.15] tracking-[-1.5px] text-[#111827] font-medium! text-[28px]">Meus pedidos</h1>
        <p class="mt-0.5 mb-0 text-linkops-slate-500 text-sm">Acompanhe o status dos seus pedidos, converse com os profissionais e avalie os serviços.</p>
      </div>
      <AppButton variant="primary" size="md" @click="router.push('/services')">
        <template #icon>
          <Plus class="size-4" :stroke-width="2.5" />
        </template>
        Novo pedido
      </AppButton>
    </section>

    <Tabs v-model="activeTab" :tabs="tabItems" />
    <BookingsToolbar v-model:search="search" v-model:sort="sort" :show-filters="showFilters" :result-count="filteredBookings.length" @toggle-filters="showFilters = !showFilters" @clear-filters="clearFilters" />

    <BookingState v-if="error" :title="error" message="Não foi possível carregar os seus pedidos." error action-label="Tentar novamente" @action="load" />
    <BookingSkeleton v-else-if="loading" />
    <BookingState v-else-if="!filteredBookings.length" :title="search ? 'Nenhum pedido encontrado' : 'Ainda não tem pedidos'" :message="search ? 'Experimente outro termo de pesquisa.' : 'Explore os serviços disponíveis e envie o seu primeiro pedido.'">
      <RouterLink to="/services" class="inline-flex min-h-8.5 items-center justify-center rounded-[7px] bg-[#008C39] px-4.5 text-[12px] font-semibold text-white hover:bg-[#08742F]">Explorar serviços</RouterLink>
    </BookingState>
    <div v-else class="flex flex-col gap-2.25">
      <BookingCard v-for="booking in filteredBookings" :key="booking.id" :booking="booking" :cancelling="cancellingId === booking.id" @cancel="cancelBooking(booking)" @message="openMessages(booking)" @review="requestReview" @details="viewDetails" />
    </div>
  </div>
</template>
