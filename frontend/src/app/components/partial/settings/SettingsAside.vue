<script setup lang="ts">
import { CheckCircle2, ChevronRight, CircleHelp, FileText, LockKeyhole, ShieldCheck, Trash2, UserRound } from 'lucide-vue-next'
import { useRouter } from 'vue-router'
import type { User } from '@/features/users/types/user'

const props = defineProps<{ user: User | null }>()
defineEmits<{ delete: [] }>()
const router = useRouter()
const accountType = { CLIENT: 'Cliente', PROVIDER: 'Prestador', ADMIN: 'Administrador' } as const
</script>

<template>
  <aside class="grid gap-3.5">
    <section class="rounded-lg border border-linkops-slate-200 bg-white p-3.5">
      <div class="mb-4 flex items-center justify-between"><h2 class="m-0 text-[13px] font-semibold text-deep-navy">A sua conta</h2><span class="rounded-full bg-green-50 px-2 py-1 text-[9px] font-semibold text-green-800">Conta gratuita</span></div>
      <div class="grid gap-3 text-[11px]"><div class="flex items-center gap-3"><UserRound class="size-4" /><span class="flex-1 font-medium">Membro desde</span><span class="text-linkops-slate-500">{{ user ? new Date(user.createdAt).toLocaleDateString('pt-PT') : '-' }}</span></div><div class="flex items-center gap-3"><ShieldCheck class="size-4" /><span class="flex-1 font-medium">Tipo de conta</span><span class="rounded-full bg-slate-50 px-2 py-1 text-linkops-slate-500">{{ user ? accountType[user.role] : '-' }}</span></div><div class="flex items-center gap-3"><CheckCircle2 class="size-4 text-linkops-green" /><span class="flex-1 font-medium">Conta verificada</span><span class="text-linkops-slate-500">{{ user?.status === 'ACTIVE' ? 'Sim' : 'Não' }}</span></div></div>
      <div class="mt-4 rounded-md bg-green-50 p-3"><strong class="block text-[11px] text-green-800">A sua conta está em dia!</strong><p class="m-0 text-[10px] leading-4 text-linkops-slate-700">Continue a explorar e encontrar os melhores serviços na sua cidade.</p></div>
    </section>
    <section class="rounded-lg border border-linkops-slate-200 bg-white p-3.5"><h2 class="mb-3 text-[13px] font-semibold text-deep-navy">Privacidade</h2><div class="grid divide-y divide-slate-100"><button type="button" class="flex items-center gap-3 border-0 bg-transparent py-2 text-left"><LockKeyhole class="size-4" /><span class="flex-1"><strong class="block text-[11px]">Quem pode ver o seu perfil</strong><small class="text-[10px] text-linkops-slate-500">Todos</small></span><ChevronRight class="size-3.5" /></button><button type="button" class="flex items-center gap-3 border-0 bg-transparent py-2 text-left"><ShieldCheck class="size-4" /><span class="flex-1"><strong class="block text-[11px]">Mostrar a sua atividade</strong><small class="text-[10px] text-linkops-slate-500">Sim</small></span><ChevronRight class="size-3.5" /></button></div></section>
    <section class="rounded-lg border border-linkops-slate-200 bg-white p-3.5"><h2 class="mb-3 text-[13px] font-semibold text-deep-navy">Suporte</h2><div class="grid gap-3 text-[11px]"><button type="button" class="flex items-center gap-2 border-0 bg-transparent p-0 text-left" @click="router.push({ name: 'help' })"><CircleHelp class="size-4" /><span class="flex-1">Central de ajuda</span><ChevronRight class="size-3.5" /></button><button type="button" class="flex items-center gap-2 border-0 bg-transparent p-0 text-left" @click="router.push({ name: 'help' })"><CircleHelp class="size-4" /><span class="flex-1">Contactar suporte</span><ChevronRight class="size-3.5" /></button><button type="button" class="flex items-center gap-2 border-0 bg-transparent p-0 text-left" @click="router.push({ name: 'help' })"><FileText class="size-4" /><span class="flex-1">Termos e políticas</span><ChevronRight class="size-3.5" /></button></div></section>
    <button type="button" class="flex items-center gap-3 rounded-lg border border-red-300 bg-white p-3 text-left text-red-600 hover:bg-red-50" @click="$emit('delete')"><Trash2 class="size-5" /><span class="flex-1"><strong class="block text-[11px]">Eliminar conta</strong><small class="text-[10px]">Esta ação é permanente e não pode ser desfeita.</small></span><ChevronRight class="size-4" /></button>
  </aside>
</template>
