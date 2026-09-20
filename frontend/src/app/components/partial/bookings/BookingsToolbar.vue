<script setup lang="ts">
import Icon from '@/app/components/DashboardIcon.vue'
import TextInput from '@/app/components/common/input/TextInput.vue'
import SelectInput from '@/app/components/common/input/SelectInput.vue'
type BookingSort = 'recent' | 'oldest'

defineProps<{
  search: string
  sort: BookingSort
  showFilters: boolean
  resultCount: number
}>()

defineEmits<{
  'update:search': [value: string]
  'update:sort': [value: BookingSort]
  'toggle-filters': []
  'clear-filters': []
}>()

const sortOptions = [
  { label: 'Mais recentes', value: 'recent' },
  { label: 'Mais antigos', value: 'oldest' },
]
</script>

<template>
  <section class="grid grid-cols-[minmax(0,1fr)_162px_116px] gap-4 py-5 max-[720px]:grid-cols-[minmax(0,1fr)_42px] max-[720px]:gap-2">
    <TextInput
      :model-value="search"
      type="text"
      placeholder="Buscar por serviço, profissional ou número do pedido..."
      aria-label="Buscar pedidos"
      has-icon
      class="max-[720px]:col-span-full"
      @update:model-value="$emit('update:search', $event.trim())"
    >
      <template #icon>
        <Icon name="search" class="size-4.75 text-[#55657A]" />
      </template>
    </TextInput>
    <SelectInput
      :model-value="sort"
      :options="sortOptions"
      aria-label="Ordenar pedidos"
      class="h-full! w-full"
      @update:model-value="$emit('update:sort', $event as BookingSort)"
    />
    <button type="button" class="inline-flex min-h-11.25 items-center justify-center gap-[8px] rounded-[7px] border border-[#DDE3E8] bg-white text-[12px] font-semibold text-[#374151] max-[720px]:text-[0px]" :aria-pressed="showFilters" @click="$emit('toggle-filters')">
      <Icon name="sliders" class="size-4.25" />Filtros
    </button>
  </section>
  <div v-if="showFilters" class="flex justify-between border-t border-[#F2F4F6] py-[9px] text-[11px] text-[#6B7280]">
    <span>{{ resultCount }} pedidos nesta página</span>
    <button type="button" class="border-0 bg-transparent text-[11px] font-semibold text-[#008C39]" @click="$emit('clear-filters')">Limpar filtros</button>
  </div>
</template>
