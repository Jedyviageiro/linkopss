<script setup lang="ts">
import { ChevronDown } from 'lucide-vue-next'

export interface SelectInputOption {
  label: string
  value: string
  disabled?: boolean
}

withDefaults(defineProps<{
  modelValue?: string
  label?: string
  placeholder?: string
  options: SelectInputOption[]
  hasIcon?: boolean
  disabled?: boolean
  required?: boolean
  error?: string
  ariaLabel?: string
  id?: string
  name?: string
}>(), {
  modelValue: '',
  label: '',
  placeholder: 'Selecione uma opção',
  hasIcon: false,
  disabled: false,
  required: false,
  error: '',
  ariaLabel: undefined,
  id: undefined,
  name: undefined,
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  change: [value: string]
}>()

function onChange(event: Event) {
  const value = (event.target as HTMLSelectElement).value
  emit('update:modelValue', value)
  emit('change', value)
}
</script>

<template>
  <div class="min-w-0">
    <label v-if="label" :for="id" class="mb-1.5 block text-xs font-medium text-slate-700">{{ label }}</label>

    <div
      class="relative h-11.25 w-full min-w-0 rounded-lg border border-slate-200 bg-white transition-colors focus-within:border-green-600 focus-within:ring-2 focus-within:ring-green-100"
      :class="{
        'border-red-500': error,
        'cursor-not-allowed bg-slate-50 opacity-70': disabled,
      }"
    >
      <span
        v-if="hasIcon"
        class="pointer-events-none absolute top-1/2 left-3 z-10 grid -translate-y-1/2 place-items-center text-green-600"
        aria-hidden="true"
      >
        <slot name="icon" />
      </span>

      <select
        :id="id"
        :name="name"
        :value="modelValue"
        :disabled="disabled"
        :required="required"
        :aria-label="ariaLabel || label || placeholder"
        :aria-invalid="Boolean(error)"
        class="select-reset cursor-pointer text-xs font-medium text-slate-600 disabled:cursor-not-allowed"
        :class="{ 'select-reset--icon': hasIcon }"
        @change="onChange"
      >
        <option value="" disabled hidden>{{ placeholder }}</option>
        <option
          v-for="option in options"
          :key="option.value"
          :value="option.value"
          :disabled="option.disabled"
        >
          {{ option.label }}
        </option>
      </select>

      <ChevronDown
        class="chevron pointer-events-none absolute top-1/2 right-3 size-4 -translate-y-1/2 text-slate-700 transition-transform"
        :stroke-width="2"
        aria-hidden="true"
      />
    </div>

    <p v-if="error" class="mt-1 mb-0 text-xs leading-4 text-red-600">{{ error }}</p>
  </div>
</template>

<style scoped>
.select-reset {
  appearance: base-select;
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  min-width: 0;
  margin: 0;
  border: 0;
  border-radius: 0.5rem;
  background: transparent;
  box-shadow: none;
  outline: 0;
  padding-block: 0;
  padding-inline: 0.75rem 2.25rem;
  line-height: normal;
  display: flex;
  align-items: center;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.select-reset--icon {
  padding-inline-start: 2.5rem;
}

.select-reset::picker-icon {
  display: none;
}

.select-reset:open + .chevron {
  transform: translateY(-50%) rotate(180deg);
}

.select-reset::picker(select) {
  appearance: base-select;
  width: anchor-size(width);
  margin-block-start: 0.375rem;
  padding: 0.25rem;
  border: 1px solid #e2e8f0;
  border-radius: 0.5rem;
  background: #fff;
}

.select-reset option {
  display: flex;
  align-items: center;
  padding: 0.5rem 0.75rem;
  border-radius: 0.375rem;
  font-size: 0.75rem;
  color: #334155;
  cursor: pointer;
}

.select-reset option:hover,
.select-reset option:checked {
  background: #f0fdf4;
}

.select-reset option:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.select-reset option::checkmark {
  display: none;
}
</style>