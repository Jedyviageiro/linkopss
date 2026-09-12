<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import toolboxArt from '@/assets/photos/nav-bar-bottom-left-corner-art.png'
import Icon, { type DashboardIconName } from './DashboardIcon.vue'
import { useAuthStore } from '@/features/auth/stores/auth-store'
import { bookingsApi } from '@/features/bookings/api/bookings-api'
import { notificationsApi } from '@/features/notifications/api/notifications-api'
import { useNotificationStore } from '@/shared/notifications/notification-store'
const props = defineProps<{ orders?: number; notifications?: number }>()
const route = useRoute(), auth = useAuthStore(), notices = useNotificationStore()
const fetchedOrders = ref(0), fetchedUnread = ref(0)
const orderCount = computed(() => props.orders ?? fetchedOrders.value)
const unread = computed(() => props.notifications ?? fetchedUnread.value)
const nav: { label: string; icon: DashboardIconName; path: string }[] = [
  { label: 'Início', icon: 'home', path: '/dashboard' }, { label: 'Explorar', icon: 'search', path: '/services' },
  { label: 'Pedidos', icon: 'clipboard', path: '/bookings' }, { label: 'Mensagens', icon: 'message', path: '/messages' },
  { label: 'Notificações', icon: 'bell', path: '/notifications' }, { label: 'Favoritos', icon: 'heart', path: '/favorites' },
  { label: 'Histórico', icon: 'history', path: '/history' }, { label: 'Configurações', icon: 'settings', path: '/settings' },
]

onMounted(async () => {
  if (!auth.isAuthenticated || props.orders !== undefined) return
  const [orders, notifications] = await Promise.allSettled([
    bookingsApi.list({ size: 50 }), notificationsApi.list({ size: 50 }),
  ])
  if (orders.status === 'fulfilled') fetchedOrders.value = orders.value.content.filter(b => ['PENDING', 'ACCEPTED', 'IN_PROGRESS'].includes(b.status)).length
  if (notifications.status === 'fulfilled') fetchedUnread.value = notifications.value.content.filter(n => !n.read).length
})
</script>
<template>
    <aside class="sticky top-0 flex h-screen self-start flex-col gap-[12px] border-r border-[#E7E9EC] bg-white px-[20px] pt-[28px] pb-[20px] [@media(max-height:700px)]:py-[14px] [@media(max-height:600px)]:static [@media(max-height:600px)]:h-auto max-[960px]:static max-[960px]:h-auto max-[960px]:gap-[14px] max-[960px]:py-[16px]">
      <RouterLink to="/dashboard" class="mb-[6px] px-[8px] text-[23px] font-bold tracking-[-.03em] text-[#111827]"><span class="text-[#0FA24A]">Link</span>Ops</RouterLink>
      <nav aria-label="Navegação principal" class="mt-[8px] grid gap-[6px] max-[960px]:grid-cols-3 max-[480px]:grid-cols-2">
        <RouterLink v-for="item in nav" :key="item.path" :to="item.path" class="relative flex shrink-0 items-center gap-[12px] rounded-[7px] px-[12px] py-[8px] text-[12px] leading-[18px] font-normal" :class="route.path === item.path ? 'bg-[#EDF4EF] text-[#0C7C3A]' : 'text-[#374151] hover:bg-[#F9FAFB]'">
          <Icon :name="item.icon" class="size-[17px]" /><span class="flex-1">{{ item.label }}</span>
          <span v-if="(item.path === '/bookings' && orderCount) || (item.path === '/notifications' && unread)" class="rounded-full bg-[#DFF6E6] px-[8px] py-px text-[11.5px] font-medium text-[#0C7C3A]">{{ item.path === '/bookings' ? orderCount : unread }}</span>
        </RouterLink>
      </nav>
      <section class="relative mt-auto shrink-0 [@media(max-height:640px)]:hidden rounded-[10px] border border-transparent bg-linear-to-b from-[#F1F8F3] to-[#F4FAF5] p-[14px] max-[960px]:hidden">
        <span class="absolute top-[16px] left-[14px] flex size-[18px] items-center justify-center text-[#00852F]"><Icon name="crown" class="size-[18px]" /></span>
        <h2 class="mb-[6px] pl-[26px] text-[12px] leading-[20px] font-medium text-[#00852F]">Seja Premium</h2><p class="mb-[10px] text-[11px] leading-[17px] text-[#6B7280]">Tenha mais controlo, veja quem visitou o seu pedido e muito mais.</p>
<div class="flex items-center justify-between gap-[12px]"><button type="button" class="!h-[30px] !min-h-0 !rounded-[5px] !bg-[#00852F] !px-[16px] !py-0 !text-[11px] !leading-[16px] !font-medium !text-white hover:!bg-[#006E27]" @click="notices.info('Os planos Premium estarão disponíveis em breve.')">Saiba mais</button><button type="button" aria-label="Saber mais sobre Premium" class="!size-[32px] !min-h-0 !rounded-full !border-0 !bg-[#DFF3E5] !p-0 !text-[#00852F] hover:!bg-[#CCEAD6]" @click="notices.info('Os planos Premium estarão disponíveis em breve.')"><Icon name="arrow-right" class="size-[16px]" /></button></div>
        <img :src="toolboxArt" alt="" width="126" height="115" class="mx-auto mt-[12px] h-[clamp(72px,12vh,108px)] w-[118px] rounded-[10px] object-cover" />
      </section>
    </aside>
</template>
