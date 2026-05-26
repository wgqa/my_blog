<script setup lang="ts">
const props = defineProps<{
  page: number
  totalPages: number
}>()

const emit = defineEmits<{
  change: [page: number]
}>()

const go = (page: number) => {
  if (page < 0 || page >= props.totalPages || page === props.page) {
    return
  }
  emit('change', page)
}
</script>

<template>
  <div v-if="totalPages > 1" class="mt-8 flex items-center justify-center gap-3">
    <button
      class="rounded-lg border border-zinc-300 px-4 py-2 text-sm text-zinc-700 disabled:cursor-not-allowed disabled:opacity-40"
      :disabled="page <= 0"
      @click="go(page - 1)"
    >
      上一页
    </button>
    <span class="text-sm text-zinc-500">第 {{ page + 1 }} / {{ totalPages }} 页</span>
    <button
      class="rounded-lg border border-zinc-300 px-4 py-2 text-sm text-zinc-700 disabled:cursor-not-allowed disabled:opacity-40"
      :disabled="page >= totalPages - 1"
      @click="go(page + 1)"
    >
      下一页
    </button>
  </div>
</template>
