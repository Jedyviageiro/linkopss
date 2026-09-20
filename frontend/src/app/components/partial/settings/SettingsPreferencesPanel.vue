<script setup lang="ts">
import { Bell, ChevronRight, Globe2, MapPin, Sun } from 'lucide-vue-next'

defineProps<{ notificationsEnabled: boolean }>()
defineEmits<{ 'update:notificationsEnabled': [value: boolean]; help: [] }>()
</script>

<template>
  <section class="rounded-lg border border-linkops-slate-200 bg-white p-3.5">
    <div class="mb-3.5"><h2 class="m-0 text-[14px] font-semibold leading-5 text-deep-navy">Preferências</h2><p class="m-0 text-[11px] leading-4 text-linkops-slate-500">Personalize como você usa a LinkOps.</p></div>
    <div class="divide-y divide-slate-100">
      <button type="button" class="flex min-h-14 w-full items-center gap-3 border-0 bg-transparent py-2.5 text-left hover:bg-slate-50" @click="$emit('help')"><MapPin class="size-5 shrink-0 text-deep-navy" /><span class="flex-1"><strong class="block text-[12px] leading-4 text-deep-navy">Cidade</strong><small class="block text-[10px] leading-4 text-linkops-slate-500">Maputo, Moçambique</small></span><ChevronRight class="size-4 shrink-0 text-linkops-slate-500" /></button>
      <button type="button" class="flex min-h-14 w-full items-center gap-3 border-0 bg-transparent py-2.5 text-left hover:bg-slate-50" @click="$emit('update:notificationsEnabled', !notificationsEnabled)"><Bell class="size-5 shrink-0 text-deep-navy" /><span class="flex-1"><strong class="block text-[12px] leading-4 text-deep-navy">Preferências de notificações</strong><small class="block text-[10px] leading-4 text-linkops-slate-500">Escolha o que deseja receber.</small></span><ChevronRight class="size-4 shrink-0 text-linkops-slate-500" /></button>
      <button type="button" class="flex min-h-14 w-full items-center gap-3 border-0 bg-transparent py-2.5 text-left hover:bg-slate-50" @click="$emit('help')"><Globe2 class="size-5 shrink-0 text-deep-navy" /><span class="flex-1"><strong class="block text-[12px] leading-4 text-deep-navy">Idioma</strong><small class="block text-[10px] leading-4 text-linkops-slate-500">Português (PT)</small></span><ChevronRight class="size-4 shrink-0 text-linkops-slate-500" /></button>
      <button type="button" class="flex min-h-14 w-full items-center gap-3 border-0 bg-transparent py-2.5 text-left hover:bg-slate-50" @click="$emit('help')"><Sun class="size-5 shrink-0 text-deep-navy" /><span class="flex-1"><strong class="block text-[12px] leading-4 text-deep-navy">Aparência</strong><small class="block text-[10px] leading-4 text-linkops-slate-500">Modo claro</small></span><ChevronRight class="size-4 shrink-0 text-linkops-slate-500" /></button>
    </div>
  </section>
</template>
