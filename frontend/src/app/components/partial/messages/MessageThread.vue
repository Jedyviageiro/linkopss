<script setup lang="ts">
import { MoreVertical, Phone, Paperclip, Send, Video } from 'lucide-vue-next'
import type { ChatMessage, Conversation } from '@/features/messages/types/message'

defineProps<{
  conversation: Conversation
  messages: ChatMessage[]
  draft: string
}>()

defineEmits<{
  'update:draft': [value: string]
  send: []
  call: []
}>()

function time(value: string) {
  return new Intl.DateTimeFormat('pt-MZ', { hour: '2-digit', minute: '2-digit' }).format(new Date(value))
}
</script>

<template>
  <section class="flex min-h-135 flex-col overflow-hidden rounded-lg border border-linkops-slate-200 bg-white">
    <header class="flex items-center gap-3 border-b border-slate-100 px-4 py-3">
      <span class="size-10 shrink-0 overflow-hidden rounded-full bg-green-100"><img :src="conversation.avatar" :alt="conversation.participantName" class="size-full object-cover" /></span>
      <div class="min-w-0 flex-1"><h2 class="m-0 truncate text-[13px] font-semibold text-deep-navy">{{ conversation.participantName }} <span class="text-green-600">✦</span></h2><p class="m-0 text-[10px] text-linkops-slate-500">{{ conversation.participantRole }}</p></div>
      <div class="flex items-center gap-1"><button type="button" class="grid size-9 place-items-center rounded-md border border-slate-200 bg-white text-deep-navy hover:bg-slate-50" aria-label="Iniciar chamada" @click="$emit('call')"><Phone class="size-4" /></button><button type="button" class="grid size-9 place-items-center rounded-md border border-slate-200 bg-white text-deep-navy hover:bg-slate-50" aria-label="Iniciar videochamada" @click="$emit('call')"><Video class="size-4" /></button><button type="button" class="grid size-9 place-items-center rounded-md border border-slate-200 bg-white text-deep-navy hover:bg-slate-50" aria-label="Mais opções"><MoreVertical class="size-4" /></button></div>
    </header>
    <div class="flex flex-1 flex-col gap-3 overflow-y-auto bg-white px-4 py-4">
      <span class="mx-auto rounded-full bg-slate-100 px-3 py-1 text-[10px] text-linkops-slate-500">Hoje</span>
      <div v-for="message in messages" :key="message.id" class="flex max-w-[78%] items-end gap-2" :class="message.sender === 'user' ? 'ml-auto flex-row-reverse' : ''"><span v-if="message.sender === 'participant'" class="size-7 shrink-0 overflow-hidden rounded-full bg-green-100"><img :src="conversation.avatar" :alt="conversation.participantName" class="size-full object-cover" /></span><div class="rounded-xl px-3 py-2 text-[11px] leading-4" :class="message.sender === 'user' ? 'rounded-br-sm bg-green-50 text-deep-navy' : 'rounded-bl-sm bg-slate-100 text-deep-navy'"><p class="m-0">{{ message.body }}</p><time class="mt-1 block text-right text-[9px] text-linkops-slate-500">{{ time(message.sentAt) }}<span v-if="message.sender === 'user'" class="ml-1 text-green-600">✓✓</span></time></div></div>
    </div>
    <form class="flex items-center gap-2 border-t border-slate-100 px-3 py-3" @submit.prevent="$emit('send')"><button type="button" class="grid size-9 place-items-center rounded-full border border-slate-200 bg-white text-linkops-slate-700" aria-label="Anexar ficheiro"><Paperclip class="size-4" /></button><input :value="draft" type="text" placeholder="Escreva uma mensagem..." aria-label="Mensagem" class="h-10 min-w-0 flex-1 rounded-md border border-slate-200 px-3 text-xs outline-none focus:border-green-600 focus:ring-2 focus:ring-green-100" @input="$emit('update:draft', ($event.target as HTMLInputElement).value)" /><button type="submit" class="grid size-10 shrink-0 place-items-center rounded-full border-0 bg-linkops-green text-white hover:bg-emerald-green" aria-label="Enviar mensagem"><Send class="size-4" /></button></form>
  </section>
</template>
