<script setup lang="ts">
import { Camera } from 'lucide-vue-next'
import AppButton from '@/app/components/common/misc/AppButton.vue'
import TextInput from '@/app/components/common/input/TextInput.vue'

const props = defineProps<{
  fullName: string
  email: string
  phone: string
  saving: boolean
}>()

defineEmits<{
  'update:fullName': [value: string]
  'update:phone': [value: string]
  save: []
}>()
</script>

<template>
  <section class="rounded-lg border border-linkops-slate-200 bg-white p-3.5">
    <div class="mb-4 flex items-start justify-between gap-4">
      <div><h2 class="m-0 text-[14px] font-semibold text-deep-navy">Informações do perfil</h2><p class="m-0 text-[11px] text-linkops-slate-500">Atualize as suas informações pessoais.</p></div>
      <AppButton variant="secondary" size="sm" :disabled="saving" @click="$emit('save')">{{ saving ? 'A guardar…' : 'Guardar alterações' }}</AppButton>
    </div>
    <div class="grid grid-cols-[64px_repeat(3,minmax(0,1fr))] items-end gap-3 max-[700px]:grid-cols-2 max-[420px]:grid-cols-1">
      <div class="relative mx-auto max-[420px]:mx-0"><span class="grid size-16 place-items-center rounded-full bg-green-100 text-lg font-semibold text-green-800">{{ fullName.split(' ').map(part => part.charAt(0)).slice(0, 2).join('') }}</span><button type="button" class="absolute right-[-2px] bottom-[-2px] grid size-5 place-items-center rounded-full border border-white bg-white text-linkops-slate-700 shadow-sm" aria-label="Alterar fotografia" title="Alterar fotografia"><Camera class="size-3" /></button></div>
      <TextInput :model-value="fullName" label="Nome completo" aria-label="Nome completo" class="[&>div>input]:pl-3" @update:model-value="$emit('update:fullName', $event)" />
      <TextInput :model-value="email" label="Email" aria-label="Email" class="[&>div>input]:pl-3" disabled />
      <TextInput :model-value="phone" label="Número de telefone" aria-label="Número de telefone" class="[&>div>input]:pl-3" @update:model-value="$emit('update:phone', $event)" />
    </div>
  </section>
</template>
