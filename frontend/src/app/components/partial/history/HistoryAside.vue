<script setup lang="ts">
import { ArrowRight, Building2, CircleHelp, CreditCard, Smartphone } from 'lucide-vue-next'
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <aside class="grid gap-3.5">
    <section class="rounded-lg border border-linkops-slate-200 bg-white p-4">
      <h2 class="mb-3 text-[13px] font-semibold text-deep-navy">Formas de pagamento</h2>
      <div class="grid gap-3">
        <button type="button" class="flex items-center gap-3 border-0 bg-transparent p-0 text-left"><span class="grid size-10 place-items-center rounded-full bg-green-50 text-linkops-green"><Smartphone class="size-5" /></span><span class="flex-1"><strong class="block text-[11px] text-deep-navy">M-Pesa</strong><small class="text-[10px] text-linkops-slate-500">84 123 4567</small></span><ArrowRight class="size-4 text-linkops-slate-500" /></button>
        <button type="button" class="flex items-center gap-3 border-0 bg-transparent p-0 text-left"><span class="grid size-10 place-items-center rounded-full bg-blue-50 text-blue-800"><Building2 class="size-5" /></span><span class="flex-1"><strong class="block text-[11px] text-deep-navy">Conta bancária</strong><small class="text-[10px] text-linkops-slate-500">BCI – 123456789012345</small></span><ArrowRight class="size-4 text-linkops-slate-500" /></button>
      </div>
    </section>
    <section class="rounded-lg border border-linkops-slate-200 bg-white p-4">
      <h2 class="mb-3 text-[13px] font-semibold text-deep-navy">Precisa de ajuda?</h2>
      <div class="grid gap-3 text-[11px] text-linkops-slate-700">
        <button type="button" class="flex items-center gap-2 border-0 bg-transparent p-0 text-left" @click="router.push({ name: 'help' })"><CircleHelp class="size-4" /><span class="flex-1">Como funciona o pagamento?</span><ArrowRight class="size-3.5" /></button>
        <button type="button" class="flex items-center gap-2 border-0 bg-transparent p-0 text-left" @click="router.push({ name: 'help' })"><CircleHelp class="size-4" /><span class="flex-1">Posso pedir reembolso?</span><ArrowRight class="size-3.5" /></button>
        <button type="button" class="flex items-center gap-2 border-0 bg-transparent p-0 text-left" @click="router.push({ name: 'help' })"><CircleHelp class="size-4" /><span class="flex-1">O que fazer em caso de problema?</span><ArrowRight class="size-3.5" /></button>
      </div>
    </section>
  </aside>
</template>
