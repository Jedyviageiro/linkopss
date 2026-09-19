<script setup lang="ts">
import { computed, ref } from 'vue'
import { Eye, EyeOff } from 'lucide-vue-next'

const props = withDefaults(
	defineProps<{
		modelValue?: string
		label?: string
		placeholder?: string
		type?: 'text' | 'number' | 'password'
		hasIcon?: boolean
		disabled?: boolean
		required?: boolean
		error?: string
		ariaLabel?: string
		id?: string
		name?: string
	}>(),
	{
		modelValue: '',
		label: '',
		placeholder: '',
		type: 'text',
		hasIcon: false,
		disabled: false,
		required: false,
		error: '',
		ariaLabel: undefined,
		id: undefined,
		name: undefined,
	},
)

const emit = defineEmits<{
	'update:modelValue': [value: string]
	change: [value: string]
	input: [value: string]
}>()

const isPasswordVisible = ref(false)

const inputType = computed(() => {
	if (props.type === 'password') {
		return isPasswordVisible.value ? 'text' : 'password'
	}

	return props.type
})

function sanitizeValue(value: string): string {
	if (props.type !== 'number') {
		return value
	}

	return value.replace(/\D/g, '')
}

function onInput(event: Event) {
	const target = event.target as HTMLInputElement
	const rawValue = target.value
	const nextValue = sanitizeValue(rawValue)

	if (nextValue !== rawValue) {
		target.value = nextValue
	}

	emit('update:modelValue', nextValue)
	emit('input', nextValue)
}

function onChange(event: Event) {
	const target = event.target as HTMLInputElement
	const value = sanitizeValue(target.value)

	emit('update:modelValue', value)
	emit('change', value)
}

function togglePasswordVisibility() {
	if (props.type !== 'password' || props.disabled) {
		return
	}

	isPasswordVisible.value = !isPasswordVisible.value
}
</script>

<template>
	<div class="min-w-0">
		<label v-if="label" :for="id" class="mb-1.5 block text-xs font-medium text-slate-700">
			{{ label }}
		</label>

		<div
			class="relative h-11.25 w-full min-w-0 rounded-lg border border-slate-200 bg-white transition-colors focus-within:border-green-600 focus-within:ring-2 focus-within:ring-green-100"
			:class="{
				'border-red-500': error,
				'cursor-not-allowed bg-slate-50 opacity-70': disabled,
			}"
		>
			<span
				v-if="hasIcon"
				class="pointer-events-none absolute top-1/2 left-3 z-10 grid -translate-y-1/2 place-items-center text-slate-400"
				aria-hidden="true"
			>
				<slot name="icon" />
			</span>

			<input
				:id="id"
				:name="name"
				:type="inputType"
				:value="modelValue"
				:placeholder="placeholder"
				:disabled="disabled"
				:required="required"
				:aria-label="ariaLabel || label || placeholder"
				:aria-invalid="Boolean(error)"
				:inputmode="type === 'number' ? 'numeric' : undefined"
				:pattern="type === 'number' ? '[0-9]*' : undefined"
				class="h-full w-full rounded-lg border-0 bg-transparent text-sm text-slate-700 outline-none placeholder:text-slate-400"
				:class="{
					'pl-10': hasIcon,
					'pr-10': type === 'password',
				}"
				@input="onInput"
				@change="onChange"
			/>

			<button
				v-if="type === 'password'"
				type="button"
				class="absolute top-1/2 right-3 grid -translate-y-1/2 place-items-center text-slate-500 transition-colors hover:text-slate-700"
				:class="{ 'cursor-not-allowed opacity-50': disabled }"
				:disabled="disabled"
				aria-label="Mostrar ou esconder senha"
				tabindex="-1"
				@click="togglePasswordVisibility"
			>
				<Eye v-if="!isPasswordVisible" class="size-4" :stroke-width="2" />
				<EyeOff v-else class="size-4" :stroke-width="2" />
			</button>
		</div>

		<p v-if="error" class="mt-1 mb-0 text-xs leading-4 text-red-600">{{ error }}</p>
	</div>
</template>
