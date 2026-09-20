<script setup lang="ts">
import { Bell, CheckCircle2, ClipboardList, Star } from 'lucide-vue-next'

export type NotificationFilter = 'all' | 'bookings' | 'reviews' | 'account'

defineProps<{
  modelValue: NotificationFilter
  counts: Record<NotificationFilter, number>
}>()

defineEmits<{ 'update:modelValue': [value: NotificationFilter] }>()

const filters = [
  { id: 'all' as const, label: 'Todas', icon: Bell },
  { id: 'bookings' as const, label: 'Pedidos', icon: ClipboardList },
  { id: 'reviews' as const, label: 'Avaliações', icon: Star },
  { id: 'account' as const, label: 'Conta', icon: CheckCircle2 },
]
</script>

<template>
  <section class="rounded-lg border border-linkops-slate-200 bg-white p-3.5" aria-labelledby="notification-filters-title">
    <h2 id="notification-filters-title" class="mb-3 text-[13px] leading-5 font-semibold text-deep-navy">Filtrar notificações</h2>
    <div class="grid gap-1">
      <button v-for="filter in filters" :key="filter.id" type="button" class="flex min-h-8.5 items-center gap-2 rounded-md border-0 px-2.5 text-left text-[11px] text-linkops-slate-700 transition-colors hover:bg-green-50" :class="modelValue === filter.id ? 'bg-green-50 font-semibold text-green-800' : 'bg-transparent'" :aria-pressed="modelValue === filter.id" @click="$emit('update:modelValue', filter.id)">
        <component :is="filter.icon" class="size-4 shrink-0" :stroke-width="2" aria-hidden="true" />
        <span class="flex-1">{{ filter.label }}</span>
        <span class="min-w-4.5 rounded-full bg-slate-100 px-1.5 py-0.5 text-center text-[10px] font-medium text-linkops-slate-500" :class="modelValue === filter.id ? 'bg-white text-green-800' : ''">{{ counts[filter.id] }}</span>
      </button>
    </div>
  </section>
</template>
