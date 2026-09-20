<script setup lang="ts">
import { CalendarDays, CheckCircle2, ChevronRight, ClipboardList, Star, XCircle } from 'lucide-vue-next'
import type { Component } from 'vue'
import type { Notification, NotificationType } from '../../../../features/notifications/types/notification'

const props = defineProps<{
  notification: Notification
  marking?: boolean
}>()

const emit = defineEmits<{ read: [notification: Notification]; open: [notification: Notification] }>()

const iconByType: Record<NotificationType, Component> = {
  BOOKING_CREATED: ClipboardList,
  BOOKING_ACCEPTED: CheckCircle2,
  BOOKING_REJECTED: XCircle,
  BOOKING_CANCELLED: XCircle,
  BOOKING_COMPLETED: CalendarDays,
  REVIEW_RECEIVED: Star,
  PROVIDER_VERIFIED: CheckCircle2,
  PROVIDER_VERIFICATION_REJECTED: XCircle,
  PROVIDER_VERIFICATION_REVOKED: XCircle,
}

const toneByType: Record<NotificationType, string> = {
  BOOKING_CREATED: 'bg-green-50 text-linkops-green',
  BOOKING_ACCEPTED: 'bg-green-50 text-linkops-green',
  BOOKING_REJECTED: 'bg-red-50 text-red-500',
  BOOKING_CANCELLED: 'bg-red-50 text-red-500',
  BOOKING_COMPLETED: 'bg-violet-50 text-violet-600',
  REVIEW_RECEIVED: 'bg-amber-50 text-amber-500',
  PROVIDER_VERIFIED: 'bg-green-50 text-linkops-green',
  PROVIDER_VERIFICATION_REJECTED: 'bg-red-50 text-red-500',
  PROVIDER_VERIFICATION_REVOKED: 'bg-red-50 text-red-500',
}

const actionByType: Record<NotificationType, string> = {
  BOOKING_CREATED: 'Ver pedido',
  BOOKING_ACCEPTED: 'Ver pedido',
  BOOKING_REJECTED: 'Ver pedido',
  BOOKING_CANCELLED: 'Ver detalhes',
  BOOKING_COMPLETED: 'Ver pedido',
  REVIEW_RECEIVED: 'Ver avaliação',
  PROVIDER_VERIFIED: 'Ver perfil',
  PROVIDER_VERIFICATION_REJECTED: 'Ver perfil',
  PROVIDER_VERIFICATION_REVOKED: 'Ver perfil',
}

function relativeDate(value: string) {
  const elapsed = Math.max(0, Date.now() - new Date(value).getTime())
  const minutes = Math.floor(elapsed / 60000)
  if (minutes < 1) return 'Agora'
  if (minutes < 60) return `${minutes} min`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours} ${hours === 1 ? 'hora' : 'horas'}`
  const days = Math.floor(hours / 24)
  return `${days} ${days === 1 ? 'dia' : 'dias'}`
}

function markRead() {
  if (!props.notification.read) emit('read', props.notification)
}
</script>

<template>
  <article class="group relative flex min-w-0 items-start gap-4 rounded-lg border px-3.5 py-3 transition-colors" :class="notification.read ? 'border-linkops-slate-200 bg-white' : 'border-green-50 bg-green-50/55'">
    <span class="flex size-11 shrink-0 items-center justify-center rounded-full" :class="toneByType[notification.type]">
      <component :is="iconByType[notification.type]" class="size-5.5" :stroke-width="2" aria-hidden="true" />
    </span>
    <div class="min-w-0 flex-1 pr-8">
      <h2 class="m-0 flex items-center gap-2 text-[13px] leading-5 font-semibold text-deep-navy">
        <span class="truncate">{{ notification.title }}</span>
        <span v-if="!notification.read" class="size-1.5 shrink-0 rounded-full bg-linkops-green" aria-label="Não lida"></span>
      </h2>
      <p class="m-0 text-[12px] leading-4.5 text-linkops-slate-700">{{ notification.message }}</p>
      <button type="button" class="mt-0.5 border-0 bg-transparent p-0 text-[11px] leading-4 text-linkops-slate-700 underline-offset-2 hover:text-linkops-green hover:underline" :disabled="marking" @click="markRead">{{ actionByType[notification.type] }}</button>
    </div>
    <time class="shrink-0 pt-0.5 text-[11px] text-linkops-slate-500" :datetime="notification.createdAt">{{ relativeDate(notification.createdAt) }}</time>
    <button type="button" class="absolute right-3 top-1/2 -translate-y-1/2 border-0 bg-transparent p-0 text-linkops-slate-500 opacity-0 transition-opacity group-hover:opacity-100 focus-visible:opacity-100" :aria-label="notification.read ? 'Abrir notificação' : 'Marcar notificação como lida'" :disabled="marking" @click="notification.read ? $emit('open', notification) : markRead()">
      <ChevronRight class="size-4.5" aria-hidden="true" />
    </button>
  </article>
</template>
