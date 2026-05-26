<script setup lang="ts">
import type { PublicPostListItem } from '../../types/public'

defineProps<{
  post: PublicPostListItem
}>()
</script>

<template>
  <article class="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm transition hover:-translate-y-0.5 hover:shadow-md">
    <div class="flex flex-wrap items-center gap-3 text-sm text-zinc-500">
      <span class="rounded-full bg-zinc-100 px-3 py-1 text-zinc-700">
        {{ post.category }}
      </span>
      <span>{{ post.author }}</span>
      <span v-if="post.publishedAt">{{ new Date(post.publishedAt).toLocaleDateString('zh-CN') }}</span>
    </div>
    <h2 class="mt-4 text-2xl font-semibold text-zinc-900">
      <RouterLink :to="`/posts/${post.slug}`" class="hover:text-zinc-600">
        {{ post.title }}
      </RouterLink>
    </h2>
    <p class="mt-3 line-clamp-3 text-sm leading-7 text-zinc-600">
      {{ post.summary || '暂无摘要。' }}
    </p>
  </article>
</template>
