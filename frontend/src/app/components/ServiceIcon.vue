<script setup lang="ts">
// Tabler Icons, MIT © 2020–2026 Paweł Kuna. See public/licenses/tabler-icons.txt.
// Vendored official SVG paths, with reference-specific category illustrations.
// Category drawings below share the same rounded outline treatment.
const categoryPaths: Record<string, string[]> = {
  wrench: ['M14 9a5 5 0 0 1 6-6l-3 3 1 2 3-3a5 5 0 0 1-6 6L6 21a2 2 0 0 1-3-3L14 9Z', 'M5 3 3 5l3 4 2-2-3-4Z', 'm8 8 3 3', 'm14 14 5 7 2-2-5-7', 'm4.5 19.5.01 0'],
  spray: ['M11 3a1 1 0 0 1 2 0v10h-2V3Z', 'M9 13h6l1 3 3 4a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1l3-4 1-3Z', 'M9 16h6', 'm9 18-1 3', 'M12 18v3', 'm15 18 1 3'],
  care: ['M10 5c-4 1-6 4-6 7-3 0-3 4 0 4 1 4 4 6 8 6s7-2 8-6c3 0 3-4 0-4 0-3-2-6-6-7', 'M10 5c0-4 4-4 4-1 0 4-4 4-4 1Z', 'M8 12h.01', 'M16 12h.01', 'M9 16q3 3 6 0'],
  beauty: ['M8 20v-3H5l-1-2 2-3c-1-5 2-9 6-9 6 0 9 5 8 10-1 4 0 6 2 8', 'M8 5c-1 3 4 4 5 7 2 4-2 6 1 10', 'M11 4c0 4 5 4 5 9-1 4-1 6 1 9', 'M6 11h2', 'M5 14h2', 'M8 20h3'],
  air: ['M12 2v20', 'm8 4 4 3 4-3', 'm8 20 4-3 4 3', 'M3.3 7 20.7 17', 'm3 11 4.6-2 .4-5', 'm16 20 .4-5 4.6-2', 'M3.3 17 20.7 7', 'm3 13 4.6 2 .4 5', 'm16 4 .4 5 4.6 2'],
}
const paths: Record<string, string[]> = {
  wrench: ['M7 10h3v-3l-3.5 -3.5a6 6 0 0 1 8 8l6 6a2 2 0 0 1 -3 3l-6 -6a6 6 0 0 1 -8 -8l3.5 3.5'],
  spray: ['M5 16l1.465 1.638a2 2 0 1 1 -3.015 .099l1.55 -1.737', 'M13.737 9.737c2.299 -2.3 3.23 -5.095 2.081 -6.245c-1.15 -1.15 -3.945 -.217 -6.244 2.082c-2.3 2.299 -3.231 5.095 -2.082 6.244c1.15 1.15 3.946 .218 6.245 -2.081', 'M7.492 11.818c.362 .362 .768 .676 1.208 .934l6.895 4.047c1.078 .557 2.255 -.075 3.692 -1.512c1.437 -1.437 2.07 -2.614 1.512 -3.692c-.372 -.718 -1.72 -3.017 -4.047 -6.895a6.015 6.015 0 0 0 -.934 -1.208'],
  care: ['M3 12a9 9 0 1 0 18 0a9 9 0 1 0 -18 0', 'M9 10l.01 0', 'M15 10l.01 0', 'M9.5 15a3.5 3.5 0 0 0 5 0', 'M12 3a2 2 0 0 0 0 4'],
  camera: ['M5 7h1a2 2 0 0 0 2 -2a1 1 0 0 1 1 -1h6a1 1 0 0 1 1 1a2 2 0 0 0 2 2h1a2 2 0 0 1 2 2v9a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-9a2 2 0 0 1 2 -2', 'M9 13a3 3 0 1 0 6 0a3 3 0 0 0 -6 0'],
  beauty: ['M3 7a3 3 0 1 0 6 0a3 3 0 1 0 -6 0', 'M3 17a3 3 0 1 0 6 0a3 3 0 1 0 -6 0', 'M8.6 8.6l10.4 10.4', 'M8.6 15.4l10.4 -10.4'],
  pipe: ['M3 4h8', 'M4 4v5a6 6 0 0 0 6 6h3a1 1 0 0 1 1 1v4', 'M10 4v4a1 1 0 0 0 1 1h3a6 6 0 0 1 6 6v5', 'M13 20h8', 'M12 9v6'],
  appliance: ['M5 5a2 2 0 0 1 2 -2h10a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-10a2 2 0 0 1 -2 -2l0 -14', 'M8 14a4 4 0 1 0 8 0a4 4 0 1 0 -8 0', 'M8 6h.01', 'M11 6h.01', 'M14 6h2', 'M8 14c1.333 -.667 2.667 -.667 4 0c1.333 .667 2.667 .667 4 0'],
  air: ['M8 16a3 3 0 0 1 -3 3', 'M16 16a3 3 0 0 0 3 3', 'M12 16v4', 'M3 7a2 2 0 0 1 2 -2h14a2 2 0 0 1 2 2v4a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2l0 -4', 'M7 13v-3a1 1 0 0 1 1 -1h8a1 1 0 0 1 1 1v3'],
  bolt: ['M13 3l0 7l6 0l-8 11l0 -7l-6 0l8 -11'],
}
defineProps<{ name: string }>()
</script>

<template>
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">
    <path v-for="(d, i) in (categoryPaths[name] ?? paths[name])" :key="i" :d="d" />
  </svg>
</template>
