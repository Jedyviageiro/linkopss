<script setup lang="ts">
import { computed } from 'vue'
import Pill from '@/app/components/common/misc/Pill.vue'
import { bookingStatusInfo, type BookingStatus } from '@/features/bookings/types/booking'

const props = defineProps<{ status: BookingStatus }>()
const info = computed(() => bookingStatusInfo[props.status])
const variantByTone = {
  waiting: 'warning',
  progress: 'info',
  success: 'primary',
  muted: 'gray',
} as const
</script>

<template>
  <Pill :variant="variantByTone[info.tone]" :label="info.label" class="max-[720px]:mt-[8px]" />
</template>
