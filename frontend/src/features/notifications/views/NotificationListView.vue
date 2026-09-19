<script setup lang="ts">
import { CheckCheck, HelpCircle, RotateCcw, Settings } from 'lucide-vue-next'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppButton from '@/app/components/common/misc/AppButton.vue'
import NotificationCard from '@/app/components/partial/notifications/NotificationCard.vue'
import NotificationFilters, { type NotificationFilter } from '@/app/components/partial/notifications/NotificationFilters.vue'
import { mockNotifications } from '@/app/data/mock'
import type { Notification, NotificationType } from '../types/notification'

const router = useRouter()
const notifications = ref<Notification[]>([])
const activeFilter = ref<NotificationFilter>('all')
const loading = ref(true)
const loadingMore = ref(false)
const error = ref('')
const actionError = ref('')
const hasMore = ref(false)
const marking = ref(new Set<string>())

const bookingTypes: NotificationType[] = ['BOOKING_CREATED', 'BOOKING_ACCEPTED', 'BOOKING_REJECTED', 'BOOKING_CANCELLED', 'BOOKING_COMPLETED']
const reviewTypes: NotificationType[] = ['REVIEW_RECEIVED']
const accountTypes: NotificationType[] = ['PROVIDER_VERIFIED', 'PROVIDER_VERIFICATION_REJECTED', 'PROVIDER_VERIFICATION_REVOKED']
const unreadCount = computed(() => notifications.value.filter(notification => !notification.read).length)
const filteredNotifications = computed(() => notifications.value.filter(notification => {
  if (activeFilter.value === 'bookings') return bookingTypes.includes(notification.type)
  if (activeFilter.value === 'reviews') return reviewTypes.includes(notification.type)
  if (activeFilter.value === 'account') return accountTypes.includes(notification.type)
  return true
}))
const filterCounts = computed<Record<NotificationFilter, number>>(() => ({
  all: notifications.value.length,
  bookings: notifications.value.filter(notification => bookingTypes.includes(notification.type)).length,
  reviews: notifications.value.filter(notification => reviewTypes.includes(notification.type)).length,
  account: notifications.value.filter(notification => accountTypes.includes(notification.type)).length,
}))

async function load() {
  loading.value = true
  error.value = ''
  notifications.value = mockNotifications.map(notification => ({ ...notification }))
  hasMore.value = false
  loading.value = false
}

async function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  actionError.value = ''
  loadingMore.value = false
}

async function markAsRead(notification: Notification) {
  if (notification.read || marking.value.has(notification.id)) return
  notification.read = true
  notification.readAt = new Date().toISOString()
  marking.value.add(notification.id)
  marking.value.delete(notification.id)
}

async function markAllAsRead() {
  const unread = notifications.value.filter(notification => !notification.read)
  if (!unread.length) return
  actionError.value = ''
  unread.forEach(notification => {
    notification.read = true
    notification.readAt = new Date().toISOString()
    marking.value.add(notification.id)
  })
  unread.forEach(notification => marking.value.delete(notification.id))
}

function openNotification(notification: Notification) {
  void markAsRead(notification)
  if (bookingTypes.includes(notification.type) && notification.referenceId) {
    void router.push({ name: 'bookings', query: { booking: notification.referenceId } })
  }
}

onMounted(() => { void load() })
</script>

<template>
  <div class="mx-auto max-w-287.5 container px-7 pb-10 max-[600px]:px-4">
    <section class="flex items-start justify-between gap-6 pt-3.25 pb-5 max-[720px]:flex-col max-[720px]:gap-3">
      <div>
        <h1 class="m-0 text-[28px] leading-[1.15] font-semibold tracking-[-1px] text-deep-navy">Notificações</h1>
        <p class="mt-1 mb-0 text-sm text-linkops-slate-500">Fique por dentro de tudo que acontece na sua conta.</p>
      </div>
      <AppButton variant="secondary" size="sm" :disabled="loading || unreadCount === 0" @click="markAllAsRead">
        <template #icon><CheckCheck class="size-4" :stroke-width="2" /></template>
        Marcar todas como lidas
      </AppButton>
    </section>
    <p v-if="actionError" class="mb-4 rounded-md border border-red-100 bg-red-50 px-3 py-2 text-xs text-red-700" role="alert">{{ actionError }}</p>

    <div class="grid grid-cols-[minmax(0,1fr)_248px] items-start gap-4 max-[900px]:grid-cols-1">
      <main class="min-w-0">
        <div v-if="error" class="flex min-h-62.5 flex-col items-center justify-center rounded-lg border border-red-100 bg-red-50/30 px-6 text-center" role="alert">
          <RotateCcw class="size-8 text-red-500" aria-hidden="true" />
          <h2 class="mt-3 mb-1 text-base font-semibold text-deep-navy">{{ error }}</h2>
          <p class="m-0 mb-4 text-xs text-linkops-slate-500">Verifique a sua ligação e tente novamente.</p>
          <AppButton variant="secondary" size="sm" @click="load">Tentar novamente</AppButton>
        </div>
        <div v-else-if="loading" class="grid gap-2.5" aria-label="A carregar notificações" aria-busy="true">
          <div v-for="item in 6" :key="item" class="h-20 animate-pulse rounded-lg border border-linkops-slate-200 bg-slate-50"></div>
        </div>
        <div v-else-if="!filteredNotifications.length" class="flex min-h-62.5 flex-col items-center justify-center rounded-lg border border-dashed border-linkops-slate-200 px-6 text-center">
          <CheckCheck class="size-9 text-linkops-green" aria-hidden="true" />
          <h2 class="mt-3 mb-1 text-base font-semibold text-deep-navy">Está tudo em dia</h2>
          <p class="m-0 text-xs text-linkops-slate-500">Não existem notificações nesta categoria.</p>
        </div>
        <div v-else class="grid gap-2.5">
          <NotificationCard v-for="notification in filteredNotifications" :key="notification.id" :notification="notification" :marking="marking.has(notification.id)" @read="markAsRead" @open="openNotification" />
          <button v-if="hasMore" type="button" class="mx-auto mt-1 border-0 bg-transparent px-4 py-2 text-xs font-semibold text-linkops-green hover:text-green-800" :disabled="loadingMore" @click="loadMore">{{ loadingMore ? 'A carregar…' : 'Carregar mais' }}</button>
        </div>
      </main>

      <aside class="grid gap-4 max-[900px]:grid-cols-2 max-[600px]:grid-cols-1" aria-label="Opções de notificações">
        <NotificationFilters v-model="activeFilter" :counts="filterCounts" />
        <section class="rounded-lg border border-linkops-slate-200 bg-white p-3.5">
          <div class="mb-2 flex items-center gap-2 text-deep-navy"><Settings class="size-4" aria-hidden="true" /><h2 class="m-0 text-[13px] font-semibold">Preferências</h2></div>
          <h3 class="m-0 text-xs font-semibold text-deep-navy">Gerir notificações</h3>
          <p class="mt-1 mb-3 text-[11px] leading-4.5 text-linkops-slate-500">Escolha como deseja receber atualizações da sua conta.</p>
          <RouterLink to="/settings" class="text-[11px] font-semibold text-linkops-green hover:text-green-800">Gerir preferências</RouterLink>
        </section>
        <section class="rounded-lg border border-linkops-slate-200 bg-white p-3.5 max-[900px]:col-span-2 max-[600px]:col-span-1">
          <div class="mb-2 flex items-center gap-2 text-deep-navy"><HelpCircle class="size-4" aria-hidden="true" /><h2 class="m-0 text-[13px] font-semibold">Precisa de ajuda?</h2></div>
          <p class="m-0 text-[11px] leading-4.5 text-linkops-slate-500">Se não estiver a receber notificações, consulte a nossa central de ajuda.</p>
          <button type="button" class="mt-3 w-full rounded-md border-0 bg-green-50 px-3 py-2 text-[11px] font-semibold text-linkops-green hover:bg-green-100" @click="router.push({ name: 'help' })">Ver ajuda</button>
        </section>
      </aside>
    </div>
  </div>
</template>
