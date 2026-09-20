export interface Conversation {
  id: string
  participantName: string
  participantRole: string
  avatar: string
  preview: string
  lastMessageAt: string
  unreadCount: number
  archived: boolean
}

export interface ChatMessage {
  id: string
  conversationId: string
  body: string
  sentAt: string
  sender: 'user' | 'participant'
}
