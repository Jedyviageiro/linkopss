<script setup lang="ts">
import { ChevronDown, MapPin } from 'lucide-vue-next'
import AppButton from '@/app/components/common/misc/AppButton.vue'
import CheckBox from '@/app/components/common/input/CheckBox.vue'
import SelectInput from '@/app/components/common/input/SelectInput.vue'
import TextInput from '@/app/components/common/input/TextInput.vue'
import type { Category } from '@/features/categories/types/category'

export type ServiceFilters = { category: string; city: string; price: string; type: string; rating: number }

const props = defineProps<{
  filters: ServiceFilters
  categories: Category[]
  categoryOptions: Category[]
  allCategories: boolean
  open: boolean
}>()

const emit = defineEmits<{
  'update:filters': [value: ServiceFilters]
  apply: []
  clear: []
  'update:allCategories': [value: boolean]
  back: []
}>()

const priceOptions = [{ value: 'low', label: 'Até 500 MT' }, { value: 'mid', label: '500 – 1.000 MT' }, { value: 'high', label: '1.000 – 2.500 MT' }, { value: 'premium', label: 'Mais de 2.500 MT' }]
const radiusOptions = [{ label: 'Num raio de 10 km', value: '10' }]
const defaultRadius = '10'

function update(key: keyof ServiceFilters, value: string | number) {
  const next = { ...props.filters, [key]: value }
  emit('update:filters', next)
}
</script>

<template>
  <aside id="explore-filters" class="min-w-0 rounded-lg border border-linkops-slate-200 bg-white p-3.5 max-[760px]:hidden" :class="{ 'max-[760px]:block': open }">
    <div class="mb-2.5 flex justify-between"><h2 class="m-0 text-[13px] font-medium text-deep-navy">Filtros</h2><button type="button" class="border-0 bg-transparent p-0 text-[11px] text-linkops-green hover:underline" @click="$emit('clear')">Limpar tudo</button></div>
    <form @submit.prevent="$emit('apply')">
      <details open class="border-b border-slate-100 py-3 first:pt-0"><summary class="flex cursor-pointer list-none justify-between text-xs font-medium text-deep-navy">Categoria<ChevronDown class="size-3.5" /></summary><div class="mt-2 grid gap-1.5"><CheckBox v-for="category in (allCategories ? categoryOptions : categories.slice(0, 5))" :key="category.id" :model-value="filters.category === category.slug" :label="category.name" @update:model-value="update('category', $event ? category.slug : '')" /><button type="button" class="w-fit border-0 bg-transparent p-0 text-[11px] text-linkops-green hover:underline" @click="$emit('update:allCategories', !allCategories)">{{ allCategories ? 'Ver menos categorias' : 'Ver todas as categorias' }}</button></div></details>
      <details open class="border-b border-slate-100 py-3"><summary class="flex cursor-pointer list-none justify-between text-xs font-medium text-deep-navy">Localização<ChevronDown class="size-3.5" /></summary><TextInput :model-value="filters.city" placeholder="Maputo, Moçambique" aria-label="Cidade" has-icon class="mt-2"><template #icon><MapPin class="size-3.5 text-linkops-green" /></template></TextInput><SelectInput :model-value="defaultRadius" :options="radiusOptions" aria-label="Raio de pesquisa" class="mt-1.5" /></details>
      <details open class="border-b border-slate-100 py-3"><summary class="flex cursor-pointer list-none justify-between text-xs font-medium text-deep-navy">Preço<ChevronDown class="size-3.5" /></summary><div class="mt-2 grid gap-1.5"><CheckBox v-for="option in priceOptions" :key="option.value" :model-value="filters.price === option.value" :label="option.label" @update:model-value="update('price', $event ? option.value : '')" /></div></details>
      <details open class="border-b border-slate-100 py-3"><summary class="flex cursor-pointer list-none justify-between text-xs font-medium text-deep-navy">Tipo de preço<ChevronDown class="size-3.5" /></summary><div class="mt-2 grid gap-1.5"><CheckBox label="Preço fixo" :model-value="filters.type === 'FIXED'" @update:model-value="update('type', $event ? 'FIXED' : '')" /><CheckBox label="Negociável" :model-value="filters.type === 'NEGOTIABLE'" @update:model-value="update('type', $event ? 'NEGOTIABLE' : '')" /></div></details>
      <details open class="py-3"><summary class="flex cursor-pointer list-none justify-between text-xs font-medium text-deep-navy">Avaliação<ChevronDown class="size-3.5" /></summary><div class="mt-2 grid gap-1"><button v-for="value in [4, 3, 2]" :key="value" type="button" class="flex w-full gap-1 border-0 bg-transparent p-0 text-left text-[11px] text-linkops-slate-500 hover:text-deep-navy" :class="filters.rating === value ? 'font-semibold text-deep-navy' : ''" :aria-pressed="filters.rating === value" @click="update('rating', filters.rating === value ? 0 : value)"><span class="text-amber-400">{{ '★'.repeat(value) }}</span><span class="text-slate-300">{{ '★'.repeat(5 - value) }}</span><span class="ml-1">{{ value }}+</span></button></div></details>
      <AppButton type="submit" size="sm" full-width>Aplicar filtros</AppButton>
    </form>
    <button type="button" class="mt-3 border-0 bg-transparent p-0 text-[11px] text-linkops-slate-500 hover:text-deep-navy" @click="$emit('back')">← Voltar ao início</button>
  </aside>
</template>
