<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, useId, watch } from 'vue'
import Icon from './DashboardIcon.vue'

const props = withDefaults(defineProps<{
  title: string
  count: number
  variant: 'services' | 'providers'
  hasMore?: boolean
  loadingMore?: boolean
}>(), { hasMore: false, loadingMore: false })
const emit = defineEmits<{ loadMore: [] }>()
const track = ref<HTMLElement | null>(null)
const trackId = useId()
const carousel = computed(() => props.count > 4)
const atStart = ref(true)
const atEnd = ref(false)
const visibleRange = ref('')
let observer: ResizeObserver | undefined
let advanceAfterLoad = false

const layout = computed(() => {
  if (!carousel.value) return props.variant === 'services'
    ? 'grid grid-cols-4 gap-[14px] max-[1100px]:grid-cols-2 max-[480px]:grid-cols-1'
    : 'grid grid-cols-4 gap-[10px] max-[600px]:grid-cols-2'
  return props.variant === 'services'
    ? 'flex gap-[14px] [&>*]:basis-[calc((100%-42px)/4)] max-[1100px]:[&>*]:basis-[calc((100%-14px)/2)] max-[480px]:[&>*]:basis-full'
    : 'flex gap-[10px] [&>*]:basis-[calc((100%-30px)/4)] max-[600px]:[&>*]:basis-[calc((100%-10px)/2)]'
})

function measure() {
  const el = track.value
  if (!el) return
  atStart.value = el.scrollLeft <= 2
  atEnd.value = el.scrollLeft + el.clientWidth >= el.scrollWidth - 2
  const children = Array.from(el.children) as HTMLElement[]
  const bounds = el.getBoundingClientRect()
  const shown = children.map((child, index) => ({ index, rect: child.getBoundingClientRect() }))
    .filter(({ rect }) => rect.right > bounds.left + 2 && rect.left < bounds.right - 2)
  visibleRange.value = shown.length
    ? `A mostrar ${shown[0]!.index + 1} a ${shown[shown.length - 1]!.index + 1} de ${props.count}`
    : ''
}
function move(direction: number) {
  const el = track.value
  if (!el) return
  const gap = parseFloat(getComputedStyle(el).columnGap) || 0
  el.scrollBy({
    left: direction * (el.clientWidth + gap),
    behavior: matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth',
  })
}
function next() {
  if (props.loadingMore) return
  if (atEnd.value && props.hasMore) {
    advanceAfterLoad = true
    emit('loadMore')
  } else move(1)
}
function onKeydown(event: KeyboardEvent) {
  if (event.target !== track.value || !carousel.value) return
  if (event.key === 'ArrowLeft') { event.preventDefault(); move(-1) }
  if (event.key === 'ArrowRight') { event.preventDefault(); next() }
}
watch(() => [props.count, props.loadingMore], async () => {
  await nextTick()
  measure()
  if (advanceAfterLoad && !props.loadingMore) { advanceAfterLoad = false; move(1) }
})
onMounted(() => {
  observer = new ResizeObserver(measure)
  if (track.value) observer.observe(track.value)
  measure()
})
onBeforeUnmount(() => observer?.disconnect())
</script>

<template>
  <section class="flex min-w-0 flex-col" :aria-label="title">
    <div class="mb-[8px] flex min-h-[28px] items-center justify-between gap-[10px]">
      <h2 class="m-0 text-[13px] leading-[20px] font-medium tracking-[-.01em] text-[#111827]">{{ title }}</h2>
      <div class="flex shrink-0 items-center gap-[10px]">
        <div v-if="carousel" class="flex items-center gap-[5px]">
          <button type="button" :aria-label="`Anteriores: ${title}`" :aria-controls="trackId" :disabled="atStart"
            class="!size-[28px] !min-h-0 !rounded-full !border !border-[#E7E9EC] !bg-white !p-0 !text-[#374151] hover:!bg-[#F1F8F3] disabled:!cursor-default disabled:!opacity-35"
            @click="move(-1)"><Icon name="arrow-right" class="size-[14px] rotate-180" /></button>
          <button type="button" :aria-label="`Seguintes: ${title}`" :aria-controls="trackId" :disabled="loadingMore || (atEnd && !hasMore)"
            class="!size-[28px] !min-h-0 !rounded-full !border !border-[#E7E9EC] !bg-white !p-0 !text-[#374151] hover:!bg-[#F1F8F3] disabled:!cursor-default disabled:!opacity-35"
            @click="next"><Icon name="arrow-right" class="size-[14px]" /></button>
        </div>
        <slot name="actions" />
      </div>
    </div>
    <div :id="trackId" ref="track" :class="[layout, carousel ? 'snap-x snap-mandatory overflow-x-auto overscroll-x-contain [scrollbar-width:none] [&::-webkit-scrollbar]:hidden [&>*]:shrink-0 [&>*]:snap-start' : '']"
      class="min-w-0 flex-1 p-px" :tabindex="carousel ? 0 : undefined" :role="carousel ? 'group' : undefined"
      :aria-roledescription="carousel ? 'carrossel' : undefined" :aria-label="carousel ? title : undefined"
      :aria-busy="loadingMore" @scroll.passive="measure" @keydown="onKeydown">
      <slot v-if="count" />
      <div v-else class="col-span-full"><slot name="empty" /></div>
    </div>
    <span v-if="carousel" class="sr-only" aria-live="polite" aria-atomic="true">{{ loadingMore ? 'A carregar mais itens…' : visibleRange }}</span>
  </section>
</template>
