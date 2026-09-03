import { ref } from 'vue'
import { defineStore } from 'pinia'

export type ModalKind = 'success' | 'error' | 'warning' | 'info'

export interface ActionModalOptions {
  kind?: ModalKind
  title: string
  message: string
  confirmLabel?: string
  cancelLabel?: string
}

export interface ActionModal extends Required<Omit<ActionModalOptions, 'cancelLabel'>> {
  cancelLabel?: string
}

export const useModalStore = defineStore('action-modal-ui', () => {
  const current = ref<ActionModal | null>(null)
  let resolveCurrent: ((confirmed: boolean) => void) | null = null

  function open(options: ActionModalOptions): Promise<boolean> {
    if (resolveCurrent) resolveCurrent(false)

    current.value = {
      kind: options.kind ?? 'info',
      title: options.title,
      message: options.message,
      confirmLabel: options.confirmLabel ?? 'Continuar',
      cancelLabel: options.cancelLabel,
    }

    return new Promise((resolve) => {
      resolveCurrent = resolve
    })
  }

  function settle(confirmed: boolean) {
    const resolve = resolveCurrent
    resolveCurrent = null
    current.value = null
    resolve?.(confirmed)
  }

  return {
    current,
    open,
    confirm: () => settle(true),
    cancel: () => settle(false),
  }
})
