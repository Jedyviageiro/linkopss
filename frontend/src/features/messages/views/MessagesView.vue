<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { MessageCircle } from 'lucide-vue-next'
import AppButton from '@/app/components/common/misc/AppButton.vue'
import ConversationList, { type ConversationFilter } from '@/app/components/partial/messages/ConversationList.vue'
import MessageThread from '@/app/components/partial/messages/MessageThread.vue'
import { mockChatMessages, mockConversations } from '@/app/data/mock'
import type { ChatMessage, Conversation } from '../types/message'

const router = useRouter()
const conversations = ref<Conversation[]>([])
const messages = ref<ChatMessage[]>([])
const activeConversationId = ref('')
const filter = ref<ConversationFilter>('all')
const search = ref('')
const draft = ref('')
const loading = ref(true)
const error = ref('')
const feedback = ref('')

const activeConversation = computed(() => conversations.value.find(conversation => conversation.id === activeConversationId.value) ?? conversations.value[0])
const visibleConversations = computed(() => {
  const term = search.value.toLocaleLowerCase('pt-MZ')
  return conversations.value.filter(conversation => {
    const matchesFilter = filter.value === 'all' || (filter.value === 'unread' && conversation.unreadCount > 0) || (filter.value === 'archived' && conversation.archived)
    const matchesSearch = !term || `${conversation.participantName} ${conversation.participantRole} ${conversation.preview}`.toLocaleLowerCase('pt-MZ').includes(term)
    return matchesFilter && matchesSearch
  })
})
const activeMessages = computed(() => messages.value.filter(message => message.conversationId === activeConversationId.value))

function load() {
  loading.value = true
  error.value = ''
  conversations.value = mockConversations.map(conversation => ({ ...conversation }))
  messages.value = mockChatMessages.map(message => ({ ...message }))
  activeConversationId.value = conversations.value[0]?.id ?? ''
  loading.value = false
}

function selectConversation(conversation: Conversation) {
  activeConversationId.value = conversation.id
  conversation.unreadCount = 0
}

function sendMessage() {
  const body = draft.value.trim()
  if (!body || !activeConversation.value) return
  messages.value.push({ id: `message-${Date.now()}`, conversationId: activeConversation.value.id, body, sentAt: new Date().toISOString(), sender: 'user' })
  activeConversation.value.preview = body
  activeConversation.value.lastMessageAt = 'Agora'
  draft.value = ''
}

function showCallFeedback() {
  feedback.value = 'As chamadas estarão disponíveis em breve.'
}

onMounted(load)
</script>

<template>
  <div class="mx-auto max-w-287.5 container px-7 pb-10 max-[600px]:px-4">
    <header class="pt-3.25 pb-5"><h1 class="m-0 text-[28px] leading-[1.15] font-semibold tracking-[-1px] text-deep-navy">Mensagens</h1><p class="mt-1 mb-0 text-sm text-linkops-slate-500">Converse com profissionais, tire dúvidas e acompanhe os seus pedidos.</p></header>
    <p v-if="feedback" class="mb-3 rounded-md border border-green-100 bg-green-50 px-3 py-2 text-xs text-green-800" role="status">{{ feedback }}</p>
    <div v-if="error" class="rounded-lg border border-red-100 bg-red-50 p-6 text-center" role="alert"><p class="m-0 text-sm text-red-700">{{ error }}</p><AppButton class="mt-4" variant="secondary" size="sm" @click="load">Tentar novamente</AppButton></div>
    <div v-else-if="loading" class="grid grid-cols-[348px_minmax(0,1fr)] gap-3.5 max-[850px]:grid-cols-1"><div class="h-135 animate-pulse rounded-lg border border-slate-200 bg-slate-50"></div><div class="h-135 animate-pulse rounded-lg border border-slate-200 bg-slate-50"></div></div>
    <div v-else-if="!conversations.length" class="flex min-h-72 flex-col items-center justify-center rounded-lg border border-dashed border-slate-200 text-center"><MessageCircle class="size-10 text-linkops-green" /><h2 class="mt-3 mb-1 text-base font-semibold text-deep-navy">Ainda não tem mensagens</h2><p class="m-0 text-xs text-linkops-slate-500">As suas conversas com profissionais aparecerão aqui.</p></div>
    <div v-else class="grid grid-cols-[348px_minmax(0,1fr)] items-stretch gap-3.5 max-[850px]:grid-cols-1">
      <ConversationList :conversations="visibleConversations" :active-id="activeConversationId" :filter="filter" :search="search" @update:filter="filter = $event" @update:search="search = $event" @select="selectConversation" @compose="feedback = 'Para iniciar uma conversa, envie primeiro um pedido de serviço.'" />
      <MessageThread v-if="activeConversation" :conversation="activeConversation" :messages="activeMessages" :draft="draft" @update:draft="draft = $event" @send="sendMessage" @call="showCallFeedback" />
      <div v-else class="flex min-h-135 items-center justify-center rounded-lg border border-slate-200 text-sm text-slate-500">Selecione uma conversa.</div>
    </div>
    <div class="mt-3 flex items-center gap-2 text-[10px] text-linkops-slate-500"><span class="size-2 rounded-full bg-green-500"></span> As mensagens são apenas um protótipo local nesta fase.<button type="button" class="ml-auto border-0 bg-transparent p-0 text-linkops-green hover:underline" @click="router.push({ name: 'help' })">Precisa de ajuda?</button></div>
  </div>
</template>
