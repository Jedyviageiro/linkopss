<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/features/auth/stores/auth-store'
import MarketplaceSidebar from '../components/layout/MarketplaceSidebar.vue'
import AppTopBar from '../components/layout/AppTopBar.vue'

const auth = useAuthStore()
const route = useRoute()
const fullName = computed(() => [auth.user?.firstName, auth.user?.lastName].filter(Boolean).join(' '))
const initials = computed(() => fullName.value.split(' ').map(part => part[0]).slice(0, 2).join('').toUpperCase() || 'LO')
</script>

<template>
  <RouterView v-if="route.meta.fullBleed" />
  <div v-else class="grid min-h-screen grid-cols-[230px_minmax(0,1fr)] bg-white font-sans text-[14px] leading-[1.4] text-[#111827] max-[960px]:grid-cols-1 [&_svg]:block [&_svg]:shrink-0 [&_button]:transition-colors [&_a]:transition-colors **:focus-visible:outline-2 **:focus-visible:outline-offset-2 **:focus-visible:outline-[#0FA24A]">
    <MarketplaceSidebar />
    <main class="min-w-0">
      <AppTopBar :full-name="fullName" :initials="initials" :unread="0" />
      <RouterView />
    </main>
  </div>
</template>
