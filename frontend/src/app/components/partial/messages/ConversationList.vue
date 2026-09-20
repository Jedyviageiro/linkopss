<script setup lang="ts">
import { Edit3, Search } from 'lucide-vue-next'
import TextInput from '@/app/components/common/input/TextInput.vue'
import type { Conversation } from '@/features/messages/types/message'

export type ConversationFilter = 'all' | 'unread' | 'archived'

defineProps<{
  conversations: Conversation[]
  activeId: string
  filter: ConversationFilter
  search: string
}>()

defineEmits<{
  'update:filter': [value: ConversationFilter]
  'update:search': [value: string]
  select: [conversation: Conversation]
  compose: []
}>()

const filters = [
  { id: 'all' as const, label: 'Todas' },
  { id: 'unread' as const, label: 'Não lidas' },
  { id: 'archived' as const, label: 'Arquivadas' },
]
</script>

<template>
  <section class="flex min-h-0 flex-col overflow-hidden rounded-lg border border-linkops-slate-200 bg-white">
    <div class="flex gap-2 border-b border-slate-100 p-3"><TextInput :model-value="search" placeholder="Buscar conversas..." aria-label="Buscar conversas" has-icon class="min-w-0 flex-1" @update:model-value="$emit('update:search', $event)"><template #icon><Search class="size-4" /></template></TextInput><button type="button" class="grid size-11 shrink-0 place-items-center rounded-md border-0 bg-linkops-green text-white hover:bg-emerald-green" aria-label="Nova conversa" @click="$emit('compose')"><Edit3 class="size-4.5" /></button></div>
    <nav class="flex border-b border-slate-100 px-3" aria-label="Filtros de conversas"><button v-for="item in filters" :key="item.id" type="button" class="relative min-h-10 flex-1 border-0 bg-transparent px-1 text-[11px] text-linkops-slate-500" :class="filter === item.id ? 'font-semibold text-deep-navy after:absolute after:right-2 after:bottom-[-1px] after:left-2 after:h-0.5 after:bg-linkops-green after:content-[\'\']' : ''" @click="$emit('update:filter', item.id)">{{ item.label }}<span v-if="item.id === 'unread'" class="ml-1 rounded-full bg-green-100 px-1.5 py-0.5 text-[9px] text-green-800">{{ conversations.filter(conversation => conversation.unreadCount > 0).length }}</span></button></nav>
    <div class="min-h-0 flex-1 overflow-y-auto">
      <button v-for="conversation in conversations" :key="conversation.id" type="button" class="flex w-full items-center gap-3 border-0 border-b border-slate-100 px-3 py-3 text-left transition-colors last:border-b-0 hover:bg-green-50/50" :class="activeId === conversation.id ? 'bg-green-50' : 'bg-white'" @click="$emit('select', conversation)">
        <span class="size-10 shrink-0 overflow-hidden rounded-full bg-green-100"><img :src="conversation.avatar" :alt="conversation.participantName" class="size-full object-cover" /></span>
        <span class="min-w-0 flex-1"><strong class="block truncate text-[12px] font-semibold text-deep-navy">{{ conversation.participantName }}</strong><small class="block truncate text-[10px] text-linkops-slate-500">{{ conversation.preview }}</small></span>
        <span class="flex shrink-0 flex-col items-end gap-1"><time class="text-[10px] text-linkops-slate-500">{{ conversation.lastMessageAt }}</time><span v-if="conversation.unreadCount" class="grid size-4.5 place-items-center rounded-full bg-linkops-green text-[9px] font-semibold text-white">{{ conversation.unreadCount }}</span></span>
      </button>
      <p v-if="!conversations.length" class="p-6 text-center text-xs text-linkops-slate-500">Nenhuma conversa encontrada.</p>
    </div>
  </section>
</template>
