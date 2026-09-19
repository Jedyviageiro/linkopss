<script setup lang="ts">
const props = withDefaults(defineProps<{
	variant?: 'primary' | 'info' | 'warning' | 'gray'
	label: string
}>(), {
	variant: 'primary',
})

const variantClasses: Record<NonNullable<typeof props.variant>, string> = {
	primary: 'bg-[#DCF5E5] text-[#07833B]',
	info: 'bg-[#E1F3FF] text-[#087BD2]',
	warning: 'bg-[#FFF1D8] text-[#CF7A00]',
	gray: 'bg-[#E9EDF2] text-[#64748B]',
}
</script>

<template>
	<span
		class="inline-flex shrink-0 items-center gap-[6px] rounded-full px-[9px] py-[4px] text-[10px] font-medium leading-[13px]"
		:class="variantClasses[variant]"
	>
		<i class="size-[7px] rounded-full bg-current" aria-hidden="true"></i>
		{{ label }}
	</span>
</template>
