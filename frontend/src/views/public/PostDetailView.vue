<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { fetchPostDetail } from '../../api/public'
import MarkdownContent from '../../components/markdown/MarkdownContent.vue'
import PublicLayout from '../../components/public/PublicLayout.vue'
import StateBlock from '../../components/public/StateBlock.vue'
import type { PublicPostDetail } from '../../types/public'

const route = useRoute()
const post = ref<PublicPostDetail | null>(null)
const loading = ref(true)
const error = ref('')

const load = async () => {
  const slug = String(route.params.slug)
  loading.value = true
  error.value = ''
  try {
    post.value = await fetchPostDetail(slug)
  } catch (err) {
    error.value = '文章详情加载失败，请确认文章是否存在。'
  } finally {
    loading.value = false
  }
}

watch(
  () => route.params.slug,
  () => {
    void load()
  },
  { immediate: true },
)
</script>

<template>
  <PublicLayout>
    <StateBlock v-if="loading" title="正在加载" message="正在获取文章详情。" />
    <StateBlock v-else-if="error" title="加载失败" :message="error" tone="error" />
    <article v-else-if="post" class="rounded-3xl border border-zinc-200 bg-white p-6 shadow-sm md:p-10">
      <header class="border-b border-zinc-100 pb-8">
        <div class="flex flex-wrap items-center gap-3 text-sm text-zinc-500">
          <span class="rounded-full bg-zinc-100 px-3 py-1 text-zinc-700">{{ post.category }}</span>
          <RouterLink :to="`/authors/${post.authorUsername}`" class="hover:text-zinc-900">
            {{ post.author }}
          </RouterLink>
          <span v-if="post.publishedAt">{{ new Date(post.publishedAt).toLocaleString('zh-CN') }}</span>
        </div>
        <h1 class="mt-5 text-4xl font-bold tracking-tight text-zinc-900">{{ post.title }}</h1>
        <p v-if="post.summary" class="mt-4 max-w-3xl text-base leading-8 text-zinc-600">
          {{ post.summary }}
        </p>
        <div v-if="post.tags.length" class="mt-5 flex flex-wrap gap-2">
          <span
            v-for="tag in post.tags"
            :key="tag"
            class="rounded-full border border-zinc-200 px-3 py-1 text-sm text-zinc-600"
          >
            # {{ tag }}
          </span>
        </div>
      </header>

      <div class="mt-10">
        <MarkdownContent :content-html="post.contentHtml" />
      </div>
    </article>
  </PublicLayout>
</template>
