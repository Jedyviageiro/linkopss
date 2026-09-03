<script setup lang="ts">
export type OnboardingIconName =
  | 'arrow-left'
  | 'bell'
  | 'calendar-check'
  | 'camera'
  | 'child-care'
  | 'grid'
  | 'map-pin'
  | 'search'
  | 'send'
  | 'shield-check'
  | 'sparkles'
  | 'spray-can'
  | 'star'
  | 'wrench'

defineProps<{ name: OnboardingIconName }>()
</script>

<template>
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
    <template v-if="name === 'arrow-left'"><path d="m15 18-6-6 6-6" /><path d="M9 12h10" /></template>
    <template v-else-if="name === 'bell'"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 7h18s-3 0-3-7" /><path d="M10 19h4" /></template>
    <template v-else-if="name === 'calendar-check'"><rect x="3" y="5" width="18" height="16" rx="3" /><path d="M7 3v4M17 3v4M3 10h18m-12 5 2 2 4-4" /></template>
    <template v-else-if="name === 'camera'"><path d="M4 7h4l1.5-2h5L16 7h4v12H4V7Z" /><circle cx="12" cy="13" r="3.5" /></template>
    <template v-else-if="name === 'child-care'"><path d="M9 5.5A3.8 3.8 0 0 1 12 4c1.3 0 2.4.6 3.1 1.5" /><circle cx="12" cy="10" r="4.5" /><path d="M6 21c.5-4.2 2.5-6.3 6-6.3s5.5 2.1 6 6.3M4 10H2m20 0h-2" /></template>
    <template v-else-if="name === 'grid'"><rect x="4" y="4" width="6" height="6" rx="2" /><rect x="14" y="4" width="6" height="6" rx="2" /><rect x="4" y="14" width="6" height="6" rx="2" /><rect x="14" y="14" width="6" height="6" rx="2" /></template>
    <template v-else-if="name === 'map-pin'"><path d="M20 10c0 5-8 11-8 11S4 15 4 10a8 8 0 1 1 16 0Z" /><circle cx="12" cy="10" r="2.5" /></template>
    <template v-else-if="name === 'search'"><circle cx="10.5" cy="10.5" r="6.5" /><path d="m15.5 15.5 4 4" /></template>
    <template v-else-if="name === 'send'"><path d="m3 11 17-8-6.5 18-2.5-7-8-3Z" /><path d="m11 14 4-5" /></template>
    <template v-else-if="name === 'shield-check'"><path d="M12 3 19 6v5c0 4.5-3 7.8-7 10-4-2.2-7-5.5-7-10V6l7-3Z" /><path d="m9 12 2 2 4-4" /></template>
    <template v-else-if="name === 'sparkles'"><path d="M8 3 9.3 6.7 13 8 9.3 9.3 8 13 6.7 9.3 3 8l3.7-1.3L8 3Z" /><path d="m17 12 .9 2.6L21 16l-3.1 1.4L17 20l-.9-2.6L13 16l3.1-1.4L17 12Z" /></template>
    <template v-else-if="name === 'spray-can'"><path d="M9 3h6M10 6h4M8 9h8l1 12H7L8 9Z" /><path d="M14 6c2-2 4-2 6-1m-4 3 4 1m-9 5h3" /></template>
    <template v-else-if="name === 'star'"><path d="m12 3 2.7 5.5 6 .9-4.4 4.2 1.1 6-5.4-2.8-5.4 2.8 1.1-6-4.4-4.2 6-.9L12 3Z" /></template>
    <template v-else><path d="M20 7.5a5.5 5.5 0 0 1-7.4 5.2L6.3 19 3 15.7l6.3-6.3A5.5 5.5 0 0 1 16.5 2L13 5.5l1.5 2L16.5 9 20 5.5v2Z" /></template>
  </svg>
</template>
