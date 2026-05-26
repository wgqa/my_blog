<script setup lang="ts">
import type { PublicCategoryItem } from '../../types/public'

defineProps<{
  categories: PublicCategoryItem[]
}>()
</script>

<template>
  <div class="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
    <h2 class="text-base font-semibold text-zinc-900">分类目录</h2>
    <div class="mt-4 flex flex-wrap gap-2">
      <RouterLink
        v-for="category in categories"
        :key="category.slug"
        :to="`/categories/${category.slug}`"
        class="rounded-full border border-zinc-200 px-3 py-2 text-sm text-zinc-600 transition hover:border-zinc-900 hover:text-zinc-900"
      >
        {{ category.name }}
      </RouterLink>
    </div>
  </div>
</template>
