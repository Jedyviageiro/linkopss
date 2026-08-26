<script setup lang="ts">
defineProps<{ title: string; description: string }>()
</script>

<template>
  <section class="placeholder-page">
    <p class="eyebrow">Estrutura inicial</p>
    <h1>{{ title }}</h1>
    <p>{{ description }}</p>
    <slot />
  </section>
</template>
