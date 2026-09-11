<script setup lang="ts">
import { computed } from 'vue'
import type { Provider } from '@/features/providers/types/provider'

const props = defineProps<{ provider: Provider | null; fallbackName?: string }>()
// Browser-only crops of the local demo photographs, not fabricated real-user portraits.
// An uploaded profile picture always takes precedence over a demo crop.
const demoPortraits: Record<string, { src: string; left: string; top: string; width: string }> = {
  '21000000-0000-0000-0000-000000000001': { src: '/images/plumber-service-demo.jpg', width: '307%', left: '-87%', top: '-4%' },
  '21000000-0000-0000-0000-000000000002': { src: '/images/ac-service-demo.jpg', width: '307%', left: '-92%', top: '-4%' },
  '21000000-0000-0000-0000-000000000003': { src: '/images/cleaning-service-demo.jpg', width: '341%', left: '-141%', top: '1%' },
  '21000000-0000-0000-0000-000000000004': { src: '/images/photography-service-demo.jpg', width: '307%', left: '-75%', top: '1%' },
}
const portrait = computed(() => props.provider ? demoPortraits[props.provider.id] : undefined)
const name = computed(() => props.provider ? `${props.provider.firstName} ${props.provider.lastName}` : props.fallbackName ?? '')
const initials = computed(() => name.value.split(/\s+/).filter(Boolean).slice(0, 2).map(part => part[0]).join(''))
</script>

<template>
  <span class="relative inline-flex shrink-0 items-center justify-center overflow-hidden rounded-full bg-[#EDF4EF] font-medium text-[#0C7C3A]" aria-hidden="true">
    <img v-if="provider?.profileImageUrl" :src="provider.profileImageUrl" alt="" class="size-full object-cover" />
    <img v-else-if="portrait" :src="portrait.src" alt="" class="absolute max-w-none" :style="{ width: portrait.width, left: portrait.left, top: portrait.top, height: 'auto' }" />
    <span v-else>{{ initials }}</span>
  </span>
</template>
