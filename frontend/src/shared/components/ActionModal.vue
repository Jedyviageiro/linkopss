<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useModalStore, type ModalKind } from '@/shared/modals/modal-store'

const modals = useModalStore()
const dialog = ref<HTMLElement | null>(null)

const styles: Record<ModalKind, { icon: string; button: string }> = {
  success: { icon: 'bg-green-50 text-linkops-green', button: '!bg-linkops-green hover:!bg-deep-navy' },
  error: { icon: 'bg-red-50 text-red-500', button: '!bg-red-500 hover:!bg-red-600' },
  warning: { icon: 'bg-amber-50 text-amber-500', button: '!bg-amber-500 hover:!bg-amber-600' },
  info: { icon: 'bg-sky-50 text-sky-500', button: '!bg-sky-500 hover:!bg-sky-600' },
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && modals.current) modals.cancel()
}

watch(() => modals.current, async (current) => {
  if (!current) return
  await nextTick()
  dialog.value?.focus()
})

onMounted(() => document.addEventListener('keydown', handleKeydown))
onBeforeUnmount(() => document.removeEventListener('keydown', handleKeydown))
</script>

<template>
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="modals.current" class="fixed inset-0 z-[1100] flex items-center justify-center bg-deep-navy/35 p-5" @click.self="modals.cancel">
        <article ref="dialog" class="relative w-full max-w-[360px] rounded-xl bg-white px-6 pt-7 pb-5 text-center shadow-[0_24px_70px_rgba(15,23,42,0.25)] outline-none" role="dialog" aria-modal="true" :aria-labelledby="`action-modal-title`" :aria-describedby="`action-modal-message`" tabindex="-1">
          <button type="button" class="absolute top-3 right-3 !min-h-0 !rounded !bg-transparent !p-1 !text-linkops-slate-500 hover:!bg-soft-background hover:!text-deep-navy" aria-label="Fechar" @click="modals.cancel">
            <svg class="size-4" viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="m6 6 8 8m0-8-8 8" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" /></svg>
          </button>

          <div class="mx-auto mb-4 flex size-12 items-center justify-center rounded-full" :class="styles[modals.current.kind].icon" aria-hidden="true">
            <svg v-if="modals.current.kind === 'success'" class="size-7" viewBox="0 0 32 32" fill="none"><circle cx="16" cy="16" r="11" stroke="currentColor" stroke-width="2" /><path d="m11 16.5 3.2 3.2 6.8-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" /></svg>
            <svg v-else-if="modals.current.kind === 'error'" class="size-7" viewBox="0 0 32 32" fill="none"><circle cx="16" cy="16" r="11" stroke="currentColor" stroke-width="2" /><path d="m12.5 12.5 7 7m0-7-7 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" /></svg>
            <svg v-else-if="modals.current.kind === 'warning'" class="size-7" viewBox="0 0 32 32" fill="none"><path d="M16 6 27 25H5L16 6Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round" /><path d="M16 12v6m0 3v.1" stroke="currentColor" stroke-width="2" stroke-linecap="round" /></svg>
            <svg v-else class="size-7" viewBox="0 0 32 32" fill="none"><circle cx="16" cy="16" r="11" stroke="currentColor" stroke-width="2" /><path d="M16 14v7m0-11v.1" stroke="currentColor" stroke-width="2" stroke-linecap="round" /></svg>
          </div>

          <h2 id="action-modal-title" class="m-0 text-[17px] leading-6 font-semibold text-deep-navy">{{ modals.current.title }}</h2>
          <p id="action-modal-message" class="mx-auto mt-2 mb-6 max-w-[290px] text-caption leading-[18px] text-linkops-slate-500">{{ modals.current.message }}</p>

          <div class="flex gap-3" :class="modals.current.cancelLabel ? '' : 'justify-center'">
            <button v-if="modals.current.cancelLabel" type="button" class="!h-[40px] !min-h-[40px] flex-1 !border !border-linkops-slate-200 !bg-white !text-linkops-slate-700 hover:!bg-soft-background" @click="modals.cancel">
              {{ modals.current.cancelLabel }}
            </button>
            <button type="button" class="!h-[40px] !min-h-[40px] flex-1 !text-white" :class="styles[modals.current.kind].button" @click="modals.confirm">
              {{ modals.current.confirmLabel }}
            </button>
          </div>
        </article>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 160ms ease;
}

.modal-fade-enter-active article,
.modal-fade-leave-active article {
  transition: transform 160ms ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-from article,
.modal-fade-leave-to article {
  transform: translateY(8px) scale(0.98);
}
</style>
