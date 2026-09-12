<script setup lang="ts">
import ServiceIcon from './ServiceIcon.vue'
export type DashboardIconName =
  | 'truck' | 'arrow-right' | 'bell' | 'camera' | 'card' | 'care' | 'chevron-down'
  | 'clipboard' | 'crown' | 'heart' | 'help' | 'history' | 'home' | 'logout' | 'message'
  | 'pin' | 'search' | 'settings' | 'shield' | 'sliders' | 'sparkles'
  | 'spray' | 'star' | 'wrench' | 'beauty' | 'send' | 'bolt' | 'snowflake' | 'pipe' | 'appliance' | 'air'

defineProps<{ name: DashboardIconName }>()
</script>

<template>
  <ServiceIcon v-if="['wrench', 'spray', 'care', 'camera', 'beauty', 'pipe', 'appliance', 'air', 'bolt'].includes(name)" :name="name" />
  <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
    <template v-if="name === 'truck'"><path d="M3 17H2V4h13v13H8m7-9h4l3 4v5h-2m-5 0h-2M15 12h7" /><circle cx="6" cy="17" r="2" /><circle cx="18" cy="17" r="2" /></template>
    <template v-else-if="name === 'home'"><path d="m3 10 9-7 9 7v10H5V10" /><path d="M9 20v-6h6v6" /></template>
    <template v-else-if="name === 'search'"><circle cx="10.5" cy="10.5" r="6.5" /><path d="m15.5 15.5 4 4" /></template>
    <template v-else-if="name === 'clipboard'"><rect x="5" y="4" width="14" height="17" rx="2" /><path d="M9 4.5V3h6v1.5M9 10h6m-6 4h6" /></template>
    <template v-else-if="name === 'message'"><path d="M21 11.5a8 8 0 0 1-8.5 8A8.7 8.7 0 0 1 8 18.3L3 20l1.7-5A8.5 8.5 0 1 1 21 11.5Z" /><path d="M8 12h.1m3.9 0h.1m3.9 0h.1" /></template>
    <template v-else-if="name === 'bell'"><path d="M18 8a6 6 0 0 0-12 0c0 6-2 7-2 9h16c0-2-2-3-2-9Z" /><path d="M10 20a2 2 0 0 0 4 0" /></template>
    <template v-else-if="name === 'history'"><path d="M3 11a9 9 0 1 1 2.6 7.4M3 5v6h6M12 7v5l3 2" /></template>
    <template v-else-if="name === 'heart'"><path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8l1.1 1.1L12 21l7.8-7.5 1.1-1.1a5.5 5.5 0 0 0-.1-7.8Z" /></template>
    <template v-else-if="name === 'card'"><rect x="3" y="5" width="18" height="14" rx="2" /><path d="M3 9h18m-5 6h2" /></template>
    <template v-else-if="name === 'settings'"><circle cx="12" cy="12" r="3" /><path d="M19.4 15a1.7 1.7 0 0 0 .3 1.9l.1.1-2.8 2.8-.1-.1a1.7 1.7 0 0 0-1.9-.3 1.7 1.7 0 0 0-1 1.6v.2h-4V21a1.7 1.7 0 0 0-1-1.6 1.7 1.7 0 0 0-1.9.3l-.1.1L4.2 17l.1-.1a1.7 1.7 0 0 0 .3-1.9A1.7 1.7 0 0 0 3 14H2.8v-4H3a1.7 1.7 0 0 0 1.6-1 1.7 1.7 0 0 0-.3-1.9L4.2 7 7 4.2l.1.1a1.7 1.7 0 0 0 1.9.3A1.7 1.7 0 0 0 10 3V2.8h4V3a1.7 1.7 0 0 0 1 1.6 1.7 1.7 0 0 0 1.9-.3l.1-.1L19.8 7l-.1.1a1.7 1.7 0 0 0-.3 1.9 1.7 1.7 0 0 0 1.6 1h.2v4H21a1.7 1.7 0 0 0-1.6 1Z" /></template>
    <template v-else-if="name === 'help'"><circle cx="12" cy="12" r="9" /><path d="M9.7 9a2.5 2.5 0 1 1 4.1 1.9c-1.1.8-1.8 1.3-1.8 2.6M12 17h.1" /></template>
    <template v-else-if="name === 'crown'"><path d="m3 7 4 4 5-7 5 7 4-4-2 11H5L3 7Z" /><path d="M6 21h12" /></template>
    <template v-else-if="name === 'pin'"><path d="M20 10c0 5-8 11-8 11S4 15 4 10a8 8 0 1 1 16 0Z" /><circle cx="12" cy="10" r="2.5" /></template>
    <template v-else-if="name === 'sliders'"><path d="M4 6h10m4 0h2M4 12h3m4 0h9M4 18h7m4 0h5" /><circle cx="16" cy="6" r="2" /><circle cx="9" cy="12" r="2" /><circle cx="13" cy="18" r="2" /></template>
    <template v-else-if="name === 'wrench'"><path d="M20 7.5a5.5 5.5 0 0 1-7.4 5.2L6.3 19 3 15.7l6.3-6.3A5.5 5.5 0 0 1 16.5 2L13 5.5l1.5 2L16.5 9 20 5.5v2Z" /></template>
    <template v-else-if="name === 'spray'"><path d="M12 2v11M9 13h6l2 3 2 5H5l2-5 2-3ZM8 18l-1 3m5-4v4m4-3 1 3M10 2h4" /></template>
    <template v-else-if="name === 'care'"><path d="M5 9a7 7 0 0 1 14 0c4-1 4 6 0 6a7 7 0 0 1-14 0c-4 0-4-6 0-6ZM12 3c-3-4-6 1-3 3 2 1 4-1 3-3" /><path d="M9 11h.1m5.9 0h.1M9 15q3 3 6 0" /></template>
    <template v-else-if="name === 'beauty'"><path d="M9 21v-4H6l-1-4H3l3-5c0-8 13-8 14 1 1 5-5 7-1 12M10 3c8 2 2 11 6 17M8 5c7 3 0 9 5 16" /></template>
    <template v-else-if="name === 'send'"><path d="m22 2-7 20-4-9-9-4 20-7ZM22 2 11 13" /></template>
    <template v-else-if="name === 'bolt'"><path d="m13 2-9 12h7l-1 8 10-13h-7l0-7Z" /></template>
    <template v-else-if="name === 'snowflake'"><path d="M12 2v20M3.3 7l17.4 10M3.3 17 20.7 7M9 4l3 3 3-3M9 20l3-3 3 3M4 10l4-1-1-4M17 19l-1-4 4-1M4 14l4 1-1 4M17 5l-1 4 4 1" /></template>
    <template v-else-if="name === 'camera'"><path d="M4 7h4l1.5-2h5L16 7h4v12H4V7Z" /><circle cx="12" cy="13" r="3.5" /></template>
    <template v-else-if="name === 'sparkles'"><path d="M8 3 9.3 6.7 13 8 9.3 9.3 8 13 6.7 9.3 3 8l3.7-1.3L8 3Z" /><path d="m17 12 .9 2.6L21 16l-3.1 1.4L17 20l-.9-2.6L13 16l3.1-1.4L17 12Z" /></template>
    <template v-else-if="name === 'star'"><path d="m12 3 2.7 5.5 6 .9-4.4 4.2 1.1 6-5.4-2.8-5.4 2.8 1.1-6-4.4-4.2 6-.9L12 3Z" /></template>
    <template v-else-if="name === 'shield'"><path d="M12 3 19 6v5c0 4.5-3 7.8-7 10-4-2.2-7-5.5-7-10V6l7-3Z" /><path d="m9 12 2 2 4-4" /></template>
    <template v-else-if="name === 'chevron-down'"><path d="m6 9 6 6 6-6" /></template>
    <template v-else-if="name === 'arrow-right'"><path d="M5 12h14m-5-5 5 5-5 5" /></template>
    <template v-else-if="name === 'logout'"><path d="M10 17l5-5-5-5m5 5H3" /><path d="M14 4h5a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-5" /></template>
  </svg>
</template>
