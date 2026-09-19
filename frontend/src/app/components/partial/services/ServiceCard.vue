<script setup lang="ts">
import { Heart, MapPin, MoreVertical, Star } from 'lucide-vue-next'
import MarketplaceAvatar from '@/app/components/MarketplaceAvatar.vue'
import type { Provider } from '@/features/providers/types/provider'
import type { ServiceOffering } from '@/features/services/types/service'

defineProps<{
  service: ServiceOffering
  provider: Provider | null
  photo: string | null
  saved: boolean
}>()

defineEmits<{ remove: []; open: [] }>()

function price(service: ServiceOffering) {
  return service.priceType === 'NEGOTIABLE'
    ? 'Preço negociável'
    : `Desde ${new Intl.NumberFormat('pt-MZ', { maximumFractionDigits: 0 }).format(service.price ?? 0)} MT`
}

function providerName(provider: Provider | null, fallback: string) {
  return provider ? `${provider.firstName} ${provider.lastName.charAt(0)}.` : fallback
}
</script>

<template>
  <article class="group overflow-hidden rounded-lg border border-linkops-slate-200 bg-white shadow-sm transition-shadow hover:shadow-md">
    <div class="relative aspect-[1.72] overflow-hidden bg-green-50">
      <img v-if="photo" :src="photo" :alt="service.title" loading="lazy" class="size-full object-cover object-center transition-transform duration-300 group-hover:scale-105" />
      <div v-else class="grid size-full place-items-center text-linkops-green"><Heart class="size-10" :stroke-width="1.4" /></div>
      <button type="button" class="absolute top-2 right-2 grid size-7 place-items-center rounded-full border-0 bg-white text-red-500 shadow-sm transition-transform hover:scale-105" :aria-pressed="saved" aria-label="Remover dos favoritos" @click="$emit('remove')">
        <Heart class="size-4 fill-current" :stroke-width="2" />
      </button>
    </div>
    <div class="flex min-h-32 flex-col gap-1.5 p-2.75">
      <div class="flex items-start justify-between gap-2">
        <button type="button" class="min-w-0 flex-1 truncate border-0 bg-transparent p-0 text-left text-[12px] leading-4 font-semibold text-deep-navy hover:text-linkops-green" :title="service.title" @click="$emit('open')">{{ service.title }}</button>
        <button type="button" class="shrink-0 border-0 bg-transparent p-0 text-linkops-slate-500" aria-label="Mais opções" @click="$emit('open')"><MoreVertical class="size-4" /></button>
      </div>
      <div class="flex min-w-0 items-center gap-1.5 text-[10px] text-linkops-slate-700">
        <MarketplaceAvatar :provider="provider" :fallback-name="service.providerName" class="size-5 text-[8px]" />
        <span class="min-w-0 flex-1 truncate">{{ providerName(provider, service.providerName) }}</span>
        <span v-if="provider?.averageRating" class="flex shrink-0 items-center gap-0.5 text-linkops-slate-500"><Star class="size-3 fill-linkops-green text-linkops-green" />{{ provider.averageRating.toLocaleString('pt-MZ', { minimumFractionDigits: 1 }) }}<span v-if="provider.completedJobs">({{ provider.completedJobs }})</span></span>
      </div>
      <p class="m-0 flex items-center gap-1 text-[10px] text-linkops-slate-500"><MapPin class="size-3" />{{ service.city }}</p>
      <strong class="mt-auto text-[12px] font-semibold text-linkops-green">{{ price(service) }}</strong>
      <div class="flex flex-wrap gap-1">
        <span class="rounded bg-slate-50 px-1.5 py-1 text-[9px] text-linkops-slate-500">{{ service.categoryName }}</span>
        <span class="rounded bg-slate-50 px-1.5 py-1 text-[9px] text-linkops-slate-500">{{ service.priceType === 'FIXED' ? 'Preço fixo' : 'Negociável' }}</span>
      </div>
    </div>
  </article>
</template>
