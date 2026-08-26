<script setup lang="ts">
import PagePlaceholder from '@/shared/components/PagePlaceholder.vue'

defineProps<{ title: string; description: string }>()
</script>

<template>
  <PagePlaceholder :title="title" :description="description" />
</template>
