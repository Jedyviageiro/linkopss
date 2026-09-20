<script setup lang="ts">
import { MessageCircle, Star } from 'lucide-vue-next'
import AppButton from '@/app/components/common/misc/AppButton.vue'
import { canCancelBooking, type Booking } from '@/features/bookings/types/booking'

defineProps<{
  booking: Booking
  cancelling: boolean
}>()

defineEmits<{ cancel: []; message: []; review: []; details: [] }>()

</script>

<template>
  <div class="flex flex-wrap justify-end gap-2.25 max-[720px]:justify-stretch max-[430px]:flex-col">
    <template v-if="booking.status === 'COMPLETED'">
      <AppButton variant="gray" size="sm" class="min-w-34.25 max-[430px]:w-full" @click="$emit('details')">Ver detalhes</AppButton>
      <AppButton variant="primary" size="sm" class="min-w-35 max-[430px]:w-full" @click="$emit('review')">
        <template #icon><Star class="size-3.25" :stroke-width="2" /></template>
        Avaliar serviço
      </AppButton>
    </template>
    <template v-else-if="booking.status === 'CANCELLED' || booking.status === 'REJECTED'">
      <AppButton variant="gray" size="sm" class="min-w-34.25 max-[430px]:w-full" @click="$emit('details')">Ver detalhes</AppButton>
    </template>
    <template v-else>
      <AppButton variant="gray" size="sm" class="min-w-34.25 max-[430px]:w-full" @click="$emit('message')">
        <template #icon><MessageCircle class="size-3.25" :stroke-width="2" /></template>
        Mensagem
      </AppButton>
      <AppButton v-if="canCancelBooking(booking)" variant="danger-outline" size="sm" class="min-w-35 max-[430px]:w-full" :disabled="cancelling" @click="$emit('cancel')">Cancelar pedido</AppButton>
    </template>
  </div>
</template>
