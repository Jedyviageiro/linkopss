<script setup lang="ts">
import { CalendarDays, ChevronRight, MapPin, MoreVertical, Star } from 'lucide-vue-next'
import Pill from '@/app/components/common/misc/Pill.vue'
import type { Booking } from '@/features/bookings/types/booking'
import { bookingStatusInfo } from '@/features/bookings/types/booking'

defineProps<{
  booking: Booking
  imageUrl?: string
  rating?: number
}>()

defineEmits<{ details: [] }>()

function formatDate(value: string) {
  return new Intl.DateTimeFormat('pt-MZ', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' }).format(new Date(value)).replace('.', '')
}

function formatPrice(value: number | null) {
  return value === null ? 'Preço negociável' : `${new Intl.NumberFormat('pt-MZ', { maximumFractionDigits: 0 }).format(value)} MT`
}

const variantByTone = { waiting: 'warning', progress: 'info', success: 'primary', muted: 'gray' } as const
</script>

<template>
  <article class="group relative flex min-w-0 items-center gap-4 rounded-lg border border-linkops-slate-200 bg-white p-2 shadow-sm transition-shadow hover:shadow-md max-[700px]:items-start max-[560px]:flex-wrap">
    <div class="size-23 shrink-0 overflow-hidden rounded-md bg-green-50 max-[560px]:size-20">
      <img v-if="imageUrl" :src="imageUrl" :alt="booking.serviceTitle" loading="lazy" class="size-full object-cover" />
      <span v-else class="grid size-full place-items-center text-linkops-green"><CalendarDays class="size-8" /></span>
    </div>
    <div class="min-w-0 flex-1 self-stretch py-0.5">
      <h2 class="m-0 truncate text-[13px] leading-5 font-semibold text-deep-navy">{{ booking.serviceTitle }}</h2>
      <p class="m-0 truncate text-[11px] text-linkops-slate-500">{{ booking.providerName }} · {{ booking.categoryName }}</p>
      <div class="mt-3 flex flex-wrap items-center gap-x-5 gap-y-1 text-[10px] text-linkops-slate-500 max-[560px]:mt-2">
        <span class="inline-flex items-center gap-1"><CalendarDays class="size-3.5" />{{ formatDate(booking.scheduledAt) }}</span>
        <span class="inline-flex items-center gap-1"><MapPin class="size-3.5" />{{ booking.address.split(',')[booking.address.split(',').length - 1]?.trim() || 'Maputo' }}</span>
        <span v-if="rating" class="inline-flex items-center gap-1 text-linkops-slate-700"><Star class="size-3 fill-linkops-green text-linkops-green" />{{ rating.toLocaleString('pt-MZ', { minimumFractionDigits: 1 }) }}</span>
      </div>
    </div>
    <div class="flex w-28 shrink-0 flex-col items-end gap-1.5 pr-2 max-[560px]:ml-auto max-[560px]:w-auto">
      <strong class="text-[13px] font-semibold text-deep-navy">{{ formatPrice(booking.price) }}</strong>
      <Pill :label="bookingStatusInfo[booking.status].label === 'Concluído' ? 'Concluído' : bookingStatusInfo[booking.status].label === 'Cancelado' ? 'Cancelado' : 'Em andamento'" :variant="variantByTone[booking.status === 'CANCELLED' ? 'muted' : booking.status === 'COMPLETED' ? 'success' : 'progress']" />
      <button type="button" class="inline-flex items-center justify-center rounded-md border border-linkops-slate-200 bg-white px-3 py-1 text-[10px] font-medium text-linkops-slate-700 hover:border-linkops-green hover:text-linkops-green" @click="$emit('details')">Ver detalhes</button>
    </div>
    <button type="button" class="absolute top-2 right-1 border-0 bg-transparent p-0 text-linkops-slate-500 opacity-0 transition-opacity group-hover:opacity-100 focus-visible:opacity-100" aria-label="Mais opções" @click="$emit('details')"><MoreVertical class="size-4" /></button>
    <button type="button" class="sr-only" aria-label="Ver detalhes do serviço" @click="$emit('details')"><ChevronRight /></button>
  </article>
</template>
