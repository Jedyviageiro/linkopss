<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  variant?: 'primary' | 'secondary' | 'ghost' | 'gray' | 'danger-outline'
  size?: 'sm' | 'md' | 'lg'
  type?: 'button' | 'submit' | 'reset'
  disabled?: boolean
  fullWidth?: boolean
}>(), {
  variant: 'primary',
  size: 'md',
  type: 'button',
  disabled: false,
  fullWidth: false,
})

const variantClasses: Record<NonNullable<typeof props.variant>, string> = {
  primary: 'bg-[#008C39] text-white hover:bg-[#08742F] shadow-[0_3px_7px_rgba(0,140,57,.15)]',
  secondary: 'border border-[#D8DEE6] bg-white text-[#374151] hover:border-[#0FA24A] hover:text-[#0FA24A]',
  ghost: 'bg-transparent text-[#374151] hover:bg-[#F4F7F7]',
  gray: 'border border-[#AEBAC7] bg-white text-[#374151] hover:border-[#008C39] hover:text-[#008C39]',
  'danger-outline': 'border border-[#FF6A6A] bg-white text-[#EF4444] hover:bg-[#FFF5F5]',
}

const sizeClasses: Record<NonNullable<typeof props.size>, string> = {
  sm: 'min-h-8.5 px-4 text-[12px]',
  md: 'min-h-10.5 px-5 text-[13px]',
  lg: 'min-h-12 px-6 text-[14px]',
}

const classes = computed(() => [
  'inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-md font-semibold transition-colors disabled:cursor-not-allowed disabled:opacity-60',
  variantClasses[props.variant],
  sizeClasses[props.size],
  props.fullWidth ? 'w-full' : '',
].join(' '))
</script>

<template>
  <button :type="type" :disabled="disabled" :class="classes">
    <slot name="icon" />
    <slot />
  </button>
</template>
