<script setup lang="ts">
import { Search, SlidersHorizontal } from 'lucide-vue-next'
import AppButton from '@/app/components/common/misc/AppButton.vue'
import TextInput from '@/app/components/common/input/TextInput.vue'
import banner from '@/assets/photos/explorer-banner.png'

defineProps<{ search: string }>()
defineEmits<{ 'update:search': [value: string]; submit: []; filters: []; popular: [value: string] }>()

const popularTerms = ['Canalizador', 'Limpeza de casa', 'Eletricista', 'Ar-condicionado', 'Fotógrafo', 'Babá', 'Pintor']
</script>

<template>
  <section class="relative isolate overflow-hidden px-7 pt-3.5 max-[600px]:px-4">
    <img :src="banner" alt="Ponte de Maputo. Serviços locais, mais oportunidades." class="pointer-events-none absolute top-0 right-0 -z-10 h-auto w-[58%] max-w-180 mask-[linear-gradient(to_right,transparent,#000_22%)] max-[760px]:w-180 max-[760px]:opacity-15 max-[760px]:mask-[linear-gradient(to_right,transparent_30%,#000_85%)]" />
    <div class="max-w-2xl pt-8 max-[600px]:pt-4">
      <h1 class="m-0 text-[clamp(28px,2.5vw,36px)] leading-tight font-semibold tracking-[-1px] text-deep-navy">Explore serviços perto de você</h1>
      <p class="mt-1 mb-0 text-sm leading-5 text-linkops-slate-500">Encontre profissionais de confiança para o que precisa, quando precisa.</p>
    </div>
    <div class="mt-5 w-4/5 flex flex-col gap-2.5 items-stretch rounded-tr-lg bg-white py-4 pr-3 max-[760px]:w-full max-[760px]:pr-0">
        <form class="flex gap-2.5 max-[600px]:flex-wrap" @submit.prevent="$emit('submit')">
          <TextInput :model-value="search" type="text" placeholder="De que serviço precisa?" aria-label="Pesquisar serviços" has-icon class="min-w-0 flex-1 max-[600px]:basis-full" @update:model-value="$emit('update:search', $event.trim())"><template #icon><Search class="size-4.5" /></template></TextInput>
          <AppButton type="submit" size="sm" class="min-w-28">Buscar</AppButton>
          <AppButton type="button" variant="secondary" size="sm" class="min-w-28" @click="$emit('filters')"><template #icon><SlidersHorizontal class="size-4" /></template>Filtros</AppButton>
        </form>
        <div class="flex w-4/5 flex-wrap items-center gap-2 pb-4 text-[11px] text-linkops-slate-500 max-[760px]:w-full max-[600px]:flex-nowrap max-[600px]:overflow-x-auto">
          <span class="mr-0.5 shrink-0 font-medium">Popular:</span><button v-for="term in popularTerms" :key="term" type="button" class="shrink-0 rounded-full border border-slate-200 bg-white px-2.5 py-1 text-[11px] text-linkops-slate-700 transition-colors hover:border-linkops-green" @click="$emit('popular', term)">{{ term }}</button>
        </div>
    </div>
  </section>
</template>
