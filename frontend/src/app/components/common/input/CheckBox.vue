<script setup lang="ts">
import { Check } from 'lucide-vue-next'

withDefaults(defineProps<{
  modelValue?: boolean
  label?: string
  disabled?: boolean
  ariaLabel?: string
}>(), {
  modelValue: false,
  label: '',
  disabled: false,
  ariaLabel: undefined,
})

defineEmits<{ 'update:modelValue': [value: boolean] }>()
</script>

<template>
  <label class="checkbox" :class="{ 'checkbox--disabled': disabled }">
    <input
      :checked="modelValue"
      type="checkbox"
      class="checkbox__input"
      :disabled="disabled"
      :aria-label="ariaLabel || label"
      @change="$emit('update:modelValue', ($event.target as HTMLInputElement).checked)"
    />

    <span class="checkbox__box" aria-hidden="true">
      <Check class="checkbox__icon" :stroke-width="3" />
    </span>

    <span v-if="label" class="checkbox__label">{{ label }}</span>
    <slot />
  </label>
</template>

<style scoped>
.checkbox {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
  gap: 0.5rem;
  width: 100%;
  min-width: 0;
  margin: 0;
  cursor: pointer;
  text-align: left;
  font-size: 0.6875rem;
  line-height: 1rem;
  font-weight: 400;
  color: #64748b;
}

.checkbox--disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.checkbox__input {
  position: absolute;
  width: 1px;
  height: 1px;
  margin: -1px;
  padding: 0;
  border: 0;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  appearance: none;
}

.checkbox__box {
  display: grid;
  place-items: center;
  flex-shrink: 0;
  width: 0.875rem;  /* 14px */
  height: 0.875rem;
  border: 1px solid #cbd5e1;
  border-radius: 3px;
  background: #fff;
  color: #fff;
  transition: background-color 0.15s, border-color 0.15s;
}

.checkbox__icon {
  width: 0.625rem;
  height: 0.625rem;
  opacity: 0;
}

.checkbox__input:checked + .checkbox__box {
  border-color: var(--color-linkops-green, #16a34a);
  background: var(--color-linkops-green, #16a34a);
}

.checkbox__input:checked + .checkbox__box .checkbox__icon {
  opacity: 1;
}

.checkbox__input:focus-visible + .checkbox__box {
  outline: 2px solid var(--color-linkops-green, #16a34a);
  outline-offset: 2px;
}

.checkbox__label {
  min-width: 0;
}
</style>
