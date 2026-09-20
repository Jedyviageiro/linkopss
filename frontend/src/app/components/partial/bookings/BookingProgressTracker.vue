<script setup lang="ts">
import { Check } from 'lucide-vue-next'

defineProps<{ progress: number }>()

const steps = ['Pedido confirmado', 'Profissional a caminho', 'Em execução', 'Concluído']
</script>

<template>
  <div class="grid grid-cols-4 gap-0">
    <div
      v-for="(step, index) in steps"
      :key="step"
      class="relative flex min-w-0 flex-col items-center gap-1 text-center text-[9px] leading-2.75 text-[#A3ABB5]"
      :class="{ 'text-[#07833B]': index + 1 <= progress }"
    >
      <span
        class="relative z-10 grid size-6 place-items-center rounded-full border-2 bg-white"
        :class="{
          'border-[#07833B] bg-[#07833B]! text-white': index + 1 < progress,
          'border-[#07833B] text-[#07833B]': index + 1 === progress,
          'border-[#CBD2D9] bg-[#CBD2D9]': index + 1 > progress,
        }"
      >
        <Check v-if="index + 1 < progress" class="size-3.5 text-white" :stroke-width="3" aria-hidden="true" />
        <span v-else-if="index + 1 === progress" class="block size-2 rounded-full bg-[#07833B]" aria-hidden="true"></span>
        <span v-else class="block size-1.5 rounded-full bg-white" aria-hidden="true"></span>
      </span>
      <small
        class="max-w-19.5"
        :class="{ 'font-semibold text-[#07833B]': index + 1 === progress }"
      >
        {{ step }}
      </small>
      <i
        v-if="index < steps.length - 1"
        class="absolute top-3 left-1/2 h-px w-full bg-[#DFE4E8]"
        :class="{ 'bg-[#07833B]': index + 1 < progress }"
        aria-hidden="true"
      ></i>
    </div>
  </div>
</template>
