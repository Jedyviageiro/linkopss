<script setup lang="ts">
import { Search, SlidersHorizontal } from 'lucide-vue-next'
import TextInput from '@/app/components/common/input/TextInput.vue'
import SelectInput from '@/app/components/common/input/SelectInput.vue'

export type FavoriteSort = 'recent' | 'rating' | 'price'

defineProps<{
  search: string
  sort: FavoriteSort
}>()

defineEmits<{
  'update:search': [value: string]
  'update:sort': [value: FavoriteSort]
}>()

const sortOptions = [
  { label: 'Mais recentes', value: 'recent' },
  { label: 'Melhor avaliados', value: 'rating' },
  { label: 'Menor preço', value: 'price' },
]
</script>

<template>
  <section class="flex w-105 gap-2.5 max-[720px]:w-full">
    <TextInput
      :model-value="search"
      placeholder="Buscar nos favoritos..."
      aria-label="Buscar nos favoritos"
      has-icon
      class="min-w-0 flex-1"
      @update:model-value="$emit('update:search', $event.trim())"
    >
      <template #icon><Search class="size-4" /></template>
    </TextInput>
    <SelectInput
      :model-value="sort"
      :options="sortOptions"
      aria-label="Ordenar favoritos"
      has-icon
      class="w-36 shrink-0"
      @update:model-value="$emit('update:sort', $event as FavoriteSort)"
    >
      <template #icon><SlidersHorizontal class="size-3.5" /></template>
    </SelectInput>
  </section>
</template>
