<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import Icon from '@/app/components/DashboardIcon.vue'
import SelectInput from '@/app/components/common/input/SelectInput.vue'
import { mozambiqueCities } from '@/app/data'

defineProps<{
  fullName: string
  initials: string
  unread: number
}>()

const router = useRouter()
const selectedCity = ref('Maputo')

function updateCity(city: string) {
  void router.push({ name: 'services', query: { city } })
}
</script>

<template>
  <header class="flex h-[72px] items-center justify-between gap-[24px] px-[15px] py-[18px] max-[600px]:gap-[12px] max-[600px]:px-[16px]">
    <SelectInput v-model="selectedCity" aria-label="Localização" placeholder="Selecione a cidade" :options="mozambiqueCities" has-icon class="w-48" @change="updateCity">
      <template #icon><Icon name="pin" class="size-4" /></template>
    </SelectInput>
    <!-- Seletor de localização anterior, substituído pelo SelectInput. -->
    <!--
      <Icon name="pin" class="size-[16px] text-[#0FA24A]" />Maputo, Moçambique<Icon name="chevron-down" class="size-[13px] text-[#6B7280]" />
    -->
    <div class="flex items-center gap-[18px] max-[600px]:gap-[4px]">
      <RouterLink to="/notifications" class="relative inline-flex size-[38px] items-center justify-center rounded-[8px] text-[#111827] hover:bg-[#F1F8F3]" :aria-label="unread ? `Notificações: ${unread} por ler` : 'Notificações'">
        <Icon name="bell" class="size-[22px]" />
        <span v-if="unread" class="absolute top-[-2px] right-[-1px] size-[13px] rounded-full border-2 border-white bg-[#008C39] text-[0px]">{{ unread }}</span>
      </RouterLink>
      <RouterLink to="/profile" class="inline-flex items-center gap-[9px] text-[13px] text-[#374151] max-[430px]:gap-[4px]">
        <span class="grid size-[38px] place-items-center rounded-full bg-[#DFF6E6] text-[12px] text-[#0C7C3A]">{{ initials }}</span>
        <strong class="font-medium max-[430px]:hidden">{{ fullName || 'Minha conta' }}</strong>
        <Icon name="chevron-down" class="size-[14px] text-[#9CA3AF]" />
      </RouterLink>
    </div>
  </header>
</template>
