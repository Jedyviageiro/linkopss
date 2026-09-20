<script setup lang="ts">
import { ChevronRight, KeyRound, Laptop, ShieldCheck } from 'lucide-vue-next'

defineEmits<{ password: []; help: [] }>()
</script>

<template>
  <section class="rounded-lg border border-linkops-slate-200 bg-white p-3.5">
    <div class="mb-3"><h2 class="m-0 text-[14px] font-semibold text-deep-navy">Segurança</h2><p class="m-0 text-[11px] text-linkops-slate-500">Proteja a sua conta com uma palavra-passe forte e autenticação adicional.</p></div>
    <div class="divide-y divide-slate-100">
      <button type="button" class="flex w-full items-center gap-3 border-0 bg-transparent py-3 text-left" @click="$emit('password')"><KeyRound class="size-5 text-deep-navy" /><span class="flex-1"><strong class="block text-[12px] text-deep-navy">Alterar palavra-passe</strong><small class="text-[10px] text-linkops-slate-500">Atualize a sua palavra-passe regularmente.</small></span><ChevronRight class="size-4 text-linkops-slate-500" /></button>
      <button type="button" class="flex w-full items-center gap-3 border-0 bg-transparent py-3 text-left" @click="$emit('help')"><ShieldCheck class="size-5 text-deep-navy" /><span class="flex-1"><strong class="block text-[12px] text-deep-navy">Autenticação em duas etapas</strong><small class="text-[10px] text-linkops-slate-500">Adicione uma camada extra de segurança.</small></span><span class="rounded-full bg-slate-100 px-2 py-1 text-[9px] text-linkops-slate-500">Desativada</span><ChevronRight class="size-4 text-linkops-slate-500" /></button>
      <button type="button" class="flex w-full items-center gap-3 border-0 bg-transparent py-3 text-left" @click="$emit('help')"><Laptop class="size-5 text-deep-navy" /><span class="flex-1"><strong class="block text-[12px] text-deep-navy">Dispositivos conectados</strong><small class="text-[10px] text-linkops-slate-500">Veja e gerencie os dispositivos com acesso à sua conta.</small></span><ChevronRight class="size-4 text-linkops-slate-500" /></button>
    </div>
  </section>
</template>
