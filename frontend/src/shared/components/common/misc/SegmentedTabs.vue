<script setup lang="ts" generic="T extends string">
interface Tab<T extends string> {
  id: T
  label: string
  count?: number
}

defineProps<{
  tabs: Tab<T>[]
  modelValue: T
}>()

defineEmits<{ 'update:modelValue': [value: T] }>()
</script>

<template>
  <nav class="flex items-center gap-[27px] overflow-x-auto border-b border-[#EDF0F2]" aria-label="Abas">
    <button v-for="tab in tabs" :key="tab.id" type="button" class="relative min-h-[42px] shrink-0 border-0 bg-transparent text-[12px] text-[#6B7280]" :class="modelValue === tab.id ? 'font-semibold text-[#111827] after:absolute after:right-0 after:bottom-[-1px] after:left-0 after:h-[2px] after:bg-[#008C39] after:content-[\'\']' : ''" :aria-pressed="modelValue === tab.id" @click="$emit('update:modelValue', tab.id)">
      {{ tab.label }}
      <span v-if="tab.count" class="ml-[7px] inline-grid min-w-[18px] h-[18px] place-items-center rounded-full bg-[#DFF6E6] px-[4px] text-[10px] font-medium text-[#0C7C3A]">{{ tab.count }}</span>
    </button>
  </nav>
</template>
