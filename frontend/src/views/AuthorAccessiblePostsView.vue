<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getApiErrorMessage } from '../api/error'
import { fetchAccessiblePostDetail, fetchAccessiblePosts } from '../api/me'
import PublicLayout from '../components/public/PublicLayout.vue'
import MarkdownContent from '../components/markdown/MarkdownContent.vue'
import type { MeAccessiblePostDetail, MeAccessiblePostItem } from '../types/me'

const posts = ref<MeAccessiblePostItem[]>([])
const total = ref(0)
const page = ref(0)
const size = 10
const loading = ref(false)
const error = ref('')

const selected = ref<MeAccessiblePostDetail | null>(null)
const detailLoading = ref(false)

const totalPages = computed(() => Math.ceil(total.value / size))

const loadPosts = async () => {
  loading.value = true
  error.value = ''
  try {
    const data = await fetchAccessiblePosts({ page: page.value, size })
    posts.value = data.content
    total.value = data.total
  } catch (err) {
    error.value = getApiErrorMessage(err, '加载失败，请稍后重试。')
  } finally {
    loading.value = false
  }
}

const showDetail = async (id: number) => {
  detailLoading.value = true
  try {
    selected.value = await fetchAccessiblePostDetail(id)
  } catch (err) {
    error.value = getApiErrorMessage(err, '文章详情加载失败。')
  } finally {
    detailLoading.value = false
  }
}

const back = () => {
  selected.value = null
}

const prevPage = () => {
  if (page.value > 0) {
    page.value--
    loadPosts()
  }
}

const nextPage = () => {
  if (page.value < totalPages.value - 1) {
    page.value++
    loadPosts()
  }
}

onMounted(() => {
  loadPosts()
})
</script>

<template>
  <PublicLayout>
    <section class="space-y-6 rounded-3xl border border-zinc-200 bg-white p-8 shadow-sm">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm font-medium text-emerald-600">作者工作区</p>
          <h1 class="mt-2 text-3xl font-bold text-zinc-900">可访问文章</h1>
          <p class="mt-2 text-sm text-zinc-500">你有权限阅读的非公开文章列表。</p>
        </div>
        <button
          v-if="selected"
          class="rounded-xl border border-zinc-300 px-4 py-3 text-sm font-medium text-zinc-700 transition hover:border-zinc-500"
          @click="back"
        >
          返回列表
        </button>
      </div>

      <p v-if="error" class="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">{{ error }}</p>

      <div v-if="loading" class="rounded-2xl border border-zinc-200 bg-zinc-50 px-4 py-5 text-sm text-zinc-500">
        加载中...
      </div>

      <div v-else-if="selected" class="space-y-4">
        <h2 class="text-2xl font-bold text-zinc-900">{{ selected.title }}</h2>
        <div class="flex flex-wrap gap-4 text-sm text-zinc-500">
          <span>作者: {{ selected.authorNickname }}</span>
          <span>分类: {{ selected.categoryName }}</span>
          <span class="rounded-full bg-amber-100 px-2 py-0.5 text-xs font-medium text-amber-700">
            {{ selected.visibility === 'RESTRICTED' ? '指定用户' : '仅自己' }}
          </span>
        </div>
        <div v-if="detailLoading" class="text-sm text-zinc-500">加载详情中...</div>
        <MarkdownContent v-else :content="selected.contentHtml" />
      </div>

      <div v-else-if="posts.length === 0" class="rounded-2xl border border-zinc-200 bg-zinc-50 px-4 py-5 text-sm text-zinc-500">
        暂无可见的非公开文章。
      </div>

      <div v-else class="space-y-3">
        <div
          v-for="post in posts"
          :key="post.id"
          class="cursor-pointer rounded-2xl border border-zinc-200 bg-zinc-50 p-5 transition hover:border-zinc-400 hover:bg-white"
          @click="showDetail(post.id)"
        >
          <div class="flex items-start justify-between gap-3">
            <div>
              <h3 class="text-lg font-semibold text-zinc-900">{{ post.title }}</h3>
              <p class="mt-2 text-sm text-zinc-500">{{ post.summary || '无摘要' }}</p>
              <div class="mt-3 flex flex-wrap gap-3 text-xs text-zinc-400">
                <span>{{ post.authorNickname }}</span>
                <span>{{ post.categoryName }}</span>
                <span
                  class="rounded-full px-2 py-0.5 font-medium"
                  :class="post.visibility === 'RESTRICTED' ? 'bg-amber-100 text-amber-700' : 'bg-zinc-200 text-zinc-600'"
                >
                  {{ post.visibility === 'RESTRICTED' ? '指定用户' : '仅自己' }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="totalPages > 1" class="flex items-center justify-center gap-4 pt-4">
          <button
            :disabled="page <= 0"
            class="rounded-xl border border-zinc-300 px-4 py-2 text-sm transition hover:border-zinc-500 disabled:cursor-not-allowed disabled:opacity-40"
            @click="prevPage"
          >
            上一页
          </button>
          <span class="text-sm text-zinc-500">{{ page + 1 }} / {{ totalPages }}</span>
          <button
            :disabled="page >= totalPages - 1"
            class="rounded-xl border border-zinc-300 px-4 py-2 text-sm transition hover:border-zinc-500 disabled:cursor-not-allowed disabled:opacity-40"
            @click="nextPage"
          >
            下一页
          </button>
        </div>
      </div>
    </section>
  </PublicLayout>
</template>
