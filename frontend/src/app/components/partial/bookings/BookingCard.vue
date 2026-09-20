<script setup lang="ts">
import { CalendarDays, ChevronRight, MapPin, Star, Tag, Wrench } from 'lucide-vue-next'
import BookingStatusBadge from './BookingStatusBadge.vue'
import BookingProgressTracker from './BookingProgressTracker.vue'
import BookingCardActions from './BookingCardActions.vue'
import { bookingProgressStep, showsBookingProgress, type Booking } from '@/features/bookings/types/booking'

defineProps<{
  booking: Booking
  imageUrl?: string
  rating?: number
  providerPhoto?: string | null
  cancelling: boolean
}>()

defineEmits<{ cancel: []; message: []; review: []; details: [] }>()

function formatDate(value: string) {
  return new Intl.DateTimeFormat('pt-MZ', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' }).format(new Date(value))
}
function formatPrice(booking: Booking) {
  return booking.price === null ? 'Preço negociável' : `${new Intl.NumberFormat('pt-MZ', { maximumFractionDigits: 0 }).format(booking.price)} MT`
}
</script>

<template>
  <article class="relative grid min-h-30 grid-cols-[154px_minmax(350px,1fr)_minmax(410px,1.12fr)] items-start gap-5 rounded-lg border border-linkops-slate-200 px-3.5 py-2.5 shadow-sm max-[1050px]:grid-cols-[130px_minmax(220px,1fr)] max-[720px]:grid-cols-[88px_minmax(0,1fr)] max-[720px]:gap-2.5 max-[720px]:px-2 max-[720px]:py-2">
    <button type="button" class="absolute top-1/2 right-4 -translate-y-1/2 border-0 bg-transparent p-0 text-linkops-slate-500 hover:text-deep-navy" aria-label="Ver detalhes do pedido" @click="$emit('details')">
      <ChevronRight class="size-5" :stroke-width="2" />
    </button>

    <div class="h-26 w-38.5 overflow-hidden rounded-md bg-green-50 max-[1050px]:h-23.5 max-[1050px]:w-32.5 max-[720px]:h-18.5 max-[720px]:w-22">
      <img v-if="imageUrl" :src="imageUrl" :alt="booking.serviceTitle" loading="lazy" class="size-full object-cover" />
      <span v-else class="grid size-full place-items-center text-linkops-green"><Wrench class="size-8.5" :stroke-width="1.8" /></span>
    </div>

    <div class="min-w-0 self-stretch py-px pr-2.5">
      <h2 class="m-0 text-body-base font-bold leading-5 text-deep-navy">{{ booking.serviceTitle }}</h2>
      <p class="mt-0.75 mb-0 flex items-center gap-2.5 text-caption text-linkops-slate-700">
        {{ booking.providerName }}
        <span v-if="rating" class="inline-flex items-center gap-0.75 text-[10px] font-medium text-deep-navy"><Star class="size-2.75 fill-linkops-green text-linkops-green" :stroke-width="2" />{{ rating.toLocaleString('pt-MZ', { minimumFractionDigits: 1, maximumFractionDigits: 1 }) }}</span>
      </p>
      <div class="mt-3 flex flex-wrap gap-x-5 gap-y-1 text-[11px] text-linkops-slate-500 max-[720px]:flex-col max-[720px]:gap-1">
        <span class="inline-flex items-center gap-1"><MapPin class="size-3.5 text-linkops-slate-500" :stroke-width="2" />{{ booking.address || 'Maputo' }}</span>
        <span class="inline-flex items-center gap-1"><CalendarDays class="size-3.5 text-linkops-slate-500" :stroke-width="2" />{{ formatDate(booking.scheduledAt) }}</span>
        <span class="inline-flex items-center gap-1 max-[720px]:hidden"><Tag class="size-3.5 text-linkops-slate-500" :stroke-width="2" />{{ booking.address ? 'Serviço em domicílio' : 'No local' }}</span>
      </div>
      <strong class="mt-2.5 block text-body-base text-linkops-green">{{ formatPrice(booking) }}</strong>
    </div>

    <div class="flex min-w-0 flex-col items-stretch gap-3 border-l border-linkops-slate-200 pl-3.5 pr-8 max-[1050px]:col-span-1 max-[1050px]:col-start-2 max-[720px]:col-span-full max-[720px]:col-start-1 max-[720px]:border-l-0 max-[720px]:pt-0.75 h-full">
      <div class="flex justify-start">
        <BookingStatusBadge :status="booking.status" />
    </div>

      <BookingProgressTracker v-if="showsBookingProgress(booking)" :progress="bookingProgressStep(booking)" />

      <div v-if="showsBookingProgress(booking)" class="flex items-center gap-2.5">
        <span class="size-6.5 shrink-0 overflow-hidden rounded-full bg-green-100">
          <img v-if="providerPhoto" :src="providerPhoto" :alt="booking.providerName" class="size-full object-cover" />
        </span>
        <BookingCardActions class="flex-1" :booking="booking" :cancelling="cancelling" @cancel="$emit('cancel')" @message="$emit('message')" @review="$emit('review')" @details="$emit('details')" />
      </div>
      <BookingCardActions v-else :booking="booking" :cancelling="cancelling" @cancel="$emit('cancel')" @message="$emit('message')" @review="$emit('review')" @details="$emit('details')" />
    </div>
  </article>
</template>
