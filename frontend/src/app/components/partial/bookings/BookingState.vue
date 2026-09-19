<script setup lang="ts">
import { Clipboard } from 'lucide-vue-next'
import AppButton from '@/app/components/common/misc/AppButton.vue'

defineProps<{
  title: string
  message: string
  error?: boolean
  actionLabel?: string
}>()

defineEmits<{ action: [] }>()
</script>

<template>
  <div class="flex min-h-62.5 flex-col items-center justify-center rounded-[9px] border border-dashed px-7.5 text-center" :class="error ? 'border-[#FFD1D1]' : 'border-[#DCE5DF]'" role="status">
    <Clipboard class="size-8.5" :class="error ? 'text-[#EF4444]' : 'text-[#0FA24A]'" :stroke-width="1.8" aria-hidden="true" />
    <h2 class="mt-3.25 mb-1 text-[16px]">{{ title }}</h2>
    <p class="m-0 mb-4 text-[12px] text-[#6B7280]">{{ message }}</p>
    <AppButton v-if="actionLabel" variant="ghost" size="sm" @click="$emit('action')">{{ actionLabel }}</AppButton>
    <slot />
  </div>
</template>
