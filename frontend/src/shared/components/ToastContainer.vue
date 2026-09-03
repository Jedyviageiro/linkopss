<script setup lang="ts">
import { useNotificationStore, type NotificationKind } from '@/shared/notifications/notification-store'

const toasts = useNotificationStore()

const styles: Record<NotificationKind, { title: string; icon: string }> = {
  info: { title: 'text-sky-500', icon: 'text-sky-500' },
  success: { title: 'text-linkops-green', icon: 'text-linkops-green' },
  error: { title: 'text-red-500', icon: 'text-red-500' },
  warning: { title: 'text-amber-500', icon: 'text-amber-500' },
}
</script>

<template>
  <Teleport to="body">
    <div class="pointer-events-none fixed top-5 right-5 z-[1000] flex w-[min(345px,calc(100vw-40px))] flex-col gap-3" aria-live="polite" aria-atomic="false">
      <TransitionGroup name="toast">
        <article
          v-for="item in toasts.items"
          :key="item.id"
          class="pointer-events-auto flex min-h-[80px] items-start gap-3 rounded-lg border border-linkops-slate-200 bg-white px-4 py-3.5 shadow-[0_10px_30px_rgba(15,23,42,0.10)]"
          :role="item.kind === 'error' || item.kind === 'warning' ? 'alert' : 'status'"
        >
          <span class="mt-0.5 flex size-5 shrink-0 items-center justify-center" :class="styles[item.kind].icon" aria-hidden="true">
            <svg v-if="item.kind === 'success'" class="size-[18px]" viewBox="0 0 20 20" fill="none">
              <circle cx="10" cy="10" r="9" fill="currentColor" />
              <path d="m5.8 10.2 2.7 2.7 5.8-6" stroke="white" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
            <svg v-else-if="item.kind === 'error'" class="size-[18px]" viewBox="0 0 20 20" fill="none">
              <circle cx="10" cy="10" r="9" fill="currentColor" />
              <path d="m7 7 6 6m0-6-6 6" stroke="white" stroke-width="1.8" stroke-linecap="round" />
            </svg>
            <svg v-else-if="item.kind === 'warning'" class="size-[18px]" viewBox="0 0 20 20" fill="none">
              <circle cx="10" cy="10" r="9" fill="currentColor" />
              <path d="M10 5.5v5.8m0 2.7v.1" stroke="white" stroke-width="1.8" stroke-linecap="round" />
            </svg>
            <svg v-else class="size-[18px]" viewBox="0 0 20 20" fill="none">
              <circle cx="10" cy="10" r="9" fill="currentColor" />
              <path d="M10 8.5V14m0-8v.1" stroke="white" stroke-width="1.8" stroke-linecap="round" />
            </svg>
          </span>

          <div class="min-w-0 flex-1">
            <h2 class="m-0 text-[12px] leading-5 font-semibold" :class="styles[item.kind].title">{{ item.title }}</h2>
            <p class="mt-1 mb-0 text-caption leading-[18px] text-linkops-slate-500">{{ item.message }}</p>
          </div>

          <button type="button" class="mt-[-2px] !min-h-0 !shrink-0 !rounded !bg-transparent !p-1 !text-linkops-slate-300 hover:!bg-soft-background hover:!text-linkops-slate-500" aria-label="Fechar notificação" @click="toasts.dismiss(item.id)">
            <svg class="size-4" viewBox="0 0 20 20" fill="none" aria-hidden="true"><path d="m6 6 8 8m0-8-8 8" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" /></svg>
          </button>
        </article>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: opacity 180ms ease, transform 180ms ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(16px);
}

@media (prefers-reduced-motion: reduce) {
  .toast-enter-active,
  .toast-leave-active {
    transition: none;
  }
}
</style>
