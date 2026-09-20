<script setup lang="ts">
import { CheckCircle2, CircleX, Clock3, ReceiptText, WalletMinimal } from 'lucide-vue-next'

defineProps<{
  completed: number
  active: number
  cancelled: number
  totalSpent: number
}>()

function formatPrice(value: number) {
  return `${new Intl.NumberFormat('pt-MZ', { maximumFractionDigits: 0 }).format(value)} MT`
}
</script>

<template>
  <aside class="grid gap-3.5">
    <section class="rounded-lg bg-green-50/80 p-4">
      <div class="flex items-center gap-3">
        <span class="grid size-12 place-items-center rounded-full bg-green-100 text-linkops-green"><WalletMinimal class="size-6" /></span>
        <div><p class="m-0 text-[11px] font-semibold text-deep-navy">Total gasto</p><strong class="block text-[24px] leading-7 text-deep-navy">{{ formatPrice(totalSpent) }}</strong><p class="m-0 text-[11px] text-linkops-slate-500">em serviços concluídos</p></div>
      </div>
    </section>
    <section class="rounded-lg border border-linkops-slate-200 bg-white p-4">
      <h2 class="mb-4 text-[13px] font-semibold text-deep-navy">Resumo</h2>
      <dl class="m-0 grid gap-4 text-[11px]">
        <div class="flex items-center gap-3"><CheckCircle2 class="size-5 text-linkops-green" /><dt class="flex-1 text-linkops-slate-700">Serviços concluídos</dt><dd class="m-0 font-semibold text-deep-navy">{{ completed }}</dd></div>
        <div class="flex items-center gap-3"><Clock3 class="size-5 text-linkops-slate-500" /><dt class="flex-1 text-linkops-slate-700">Em andamento</dt><dd class="m-0 font-semibold text-deep-navy">{{ active }}</dd></div>
        <div class="flex items-center gap-3"><CircleX class="size-5 text-red-500" /><dt class="flex-1 text-linkops-slate-700">Cancelados</dt><dd class="m-0 font-semibold text-deep-navy">{{ cancelled }}</dd></div>
        <div class="flex items-center gap-3 border-t border-slate-100 pt-3"><ReceiptText class="size-5 text-linkops-slate-500" /><dt class="flex-1 text-linkops-slate-700">Total gasto</dt><dd class="m-0 font-semibold text-deep-navy">{{ formatPrice(totalSpent) }}</dd></div>
      </dl>
    </section>
  </aside>
</template>
