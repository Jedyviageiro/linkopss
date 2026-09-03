import { ref } from 'vue'
import { defineStore } from 'pinia'

export type NotificationKind = 'success' | 'error' | 'warning' | 'info'

export interface AppNotification {
  id: number
  kind: NotificationKind
  title: string
  message: string
}

const defaultTitles: Record<NotificationKind, string> = {
  success: 'Tudo certo!',
  error: 'Algo correu mal',
  warning: 'Atenção',
  info: 'Informação',
}

let nextId = 0

export const useNotificationStore = defineStore('notifications-ui', () => {
  const items = ref<AppNotification[]>([])

  function dismiss(id: number) {
    items.value = items.value.filter((item) => item.id !== id)
  }

  function show(kind: NotificationKind, message: string, title = defaultTitles[kind], duration = 5000) {
    const id = ++nextId
    items.value.push({ id, kind, title, message })

    if (duration > 0) globalThis.setTimeout(() => dismiss(id), duration)
    return id
  }

  return {
    items,
    dismiss,
    show,
    success: (message: string, title?: string) => show('success', message, title),
    error: (message: string, title?: string) => show('error', message, title),
    warning: (message: string, title?: string) => show('warning', message, title),
    info: (message: string, title?: string) => show('info', message, title),
  }
})
