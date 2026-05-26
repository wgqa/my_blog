<script setup lang="ts">
defineProps<{
  contentHtml: string
}>()
</script>

<template>
  <div class="markdown-content" v-html="contentHtml" />
</template>
