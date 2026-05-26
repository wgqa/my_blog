<script setup lang="ts">
defineProps<{
  title?: string
  message: string
  tone?: 'default' | 'error'
}>()
</script>

<template>
  <div class="rounded-2xl border p-6 shadow-sm" :class="tone === 'error' ? 'border-red-200 bg-red-50' : 'border-zinc-200 bg-white'">
    <h2 v-if="title" class="text-lg font-semibold text-zinc-900">{{ title }}</h2>
    <p class="mt-2 text-sm leading-7 text-zinc-600">{{ message }}</p>
  </div>
</template>
