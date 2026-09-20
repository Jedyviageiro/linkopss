<script setup lang="ts">
import { MapPin, X } from 'lucide-vue-next'
import MarketplaceAvatar from '@/app/components/MarketplaceAvatar.vue'
import type { Provider } from '@/features/providers/types/provider'
import type { ServiceOffering } from '@/features/services/types/service'

interface Result { service: ServiceOffering; provider: Provider | null; photo: string | null }
defineProps<{ item: Result | null }>()
defineEmits<{ close: [] }>()
function price(service: ServiceOffering) { return service.priceType === 'NEGOTIABLE' ? 'Preço negociável' : `Desde ${new Intl.NumberFormat('pt-MZ', { maximumFractionDigits: 0 }).format(service.price ?? 0)} MT` }
</script>

<template>
  <dialog v-if="item" open class="fixed inset-0 m-auto max-h-[85vh] w-[min(480px,90vw)] overflow-auto rounded-xl border border-linkops-slate-200 bg-white p-7 text-deep-navy backdrop:bg-slate-900/40" @click="(event) => { if (event.target === event.currentTarget) $emit('close') }">
    <button type="button" class="absolute top-3 right-3 grid size-8 place-items-center border-0 bg-transparent text-slate-500 hover:text-deep-navy" aria-label="Fechar perfil" @click="$emit('close')"><X class="size-5" /></button><MarketplaceAvatar :provider="item.provider" :fallback-name="item.service.providerName" class="size-16 text-lg" /><h2 class="mt-3 mb-1 text-lg font-semibold">{{ item.service.providerName }}</h2><p class="m-0 text-sm text-linkops-slate-500">{{ item.provider?.bio || 'Profissional de confiança na sua região.' }}</p><h3 class="mt-5 mb-1 text-base font-semibold">{{ item.service.title }}</h3><p class="m-0 text-sm text-linkops-slate-500">{{ item.service.description || 'Consulte o perfil para conhecer este serviço.' }}</p><p class="mt-3 flex items-center gap-1 text-xs text-linkops-slate-500"><MapPin class="size-4" />{{ item.service.city }}</p><strong class="mt-4 block text-base text-linkops-green">{{ price(item.service) }}</strong>
  </dialog>
</template>
