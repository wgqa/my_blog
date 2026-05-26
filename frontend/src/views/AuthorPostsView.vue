<script setup lang="ts">
import axios from 'axios'
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { deleteMyPost, fetchMyPosts } from '../api/me'
import PublicLayout from '../components/public/PublicLayout.vue'
import PaginationBar from '../components/public/PaginationBar.vue'
import StateBlock from '../components/public/StateBlock.vue'
import type { MePostListItem } from '../types/me'
import type { PageResponse } from '../types/public'

const posts = ref<PageResponse<MePostListItem> | null>(null)
const loading = ref(true)
const deletingId = ref<number | null>(null)
const error = ref('')
const success = ref('')

const postCountLabel = computed(() => {
  const total = posts.value?.total ?? 0
  return `共 ${total} 篇已发布文章`
})

const load = async (page = 0) => {
  loading.value = true
  error.value = ''

  try {
    posts.value = await fetchMyPosts({ page, size: 10 })
  } catch (err) {
    if (axios.isAxiosError(err)) {
      error.value = err.response?.data?.message ?? '文章列表加载失败，请稍后重试。'
    } else {
      error.value = '文章列表加载失败，请稍后重试。'
    }
  } finally {
    loading.value = false
  }
}

const removePost = async (post: MePostListItem) => {
  if (!window.confirm(`确认删除《${post.title}》吗？`)) {
    return
  }

  deletingId.value = post.id
  error.value = ''
  success.value = ''

  try {
    await deleteMyPost(post.id)
    success.value = '文章已删除。'
    const previousTotal = posts.value?.total ?? 0
    const currentPage = posts.value?.page ?? 0
    await load(currentPage)

    if (posts.value && posts.value.items.length === 0 && currentPage > 0 && previousTotal > 1) {
      await load(currentPage - 1)
    }
  } catch (err) {
    if (axios.isAxiosError(err)) {
      error.value = err.response?.data?.message ?? '文章删除失败，请稍后重试。'
    } else {
      error.value = '文章删除失败，请稍后重试。'
    }
  } finally {
    deletingId.value = null
  }
}

const formatTime = (value: string | null) => {
  if (!value) {
    return '—'
  }
  return new Date(value).toLocaleString('zh-CN')
}

onMounted(() => {
  void load()
})
</script>

<template>
  <PublicLayout>
    <section class="space-y-6 rounded-3xl border border-zinc-200 bg-white p-8 shadow-sm">
      <div class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <p class="text-sm font-medium text-emerald-600">作者工作区</p>
          <h1 class="mt-2 text-3xl font-bold text-zinc-900">我的文章</h1>
          <p class="mt-2 text-sm text-zinc-500">{{ postCountLabel }}</p>
        </div>
        <div class="flex flex-wrap gap-3">
          <RouterLink
            :to="{ name: 'author-dashboard' }"
            class="rounded-xl border border-zinc-300 px-4 py-3 text-sm font-medium text-zinc-700 transition hover:border-zinc-500 hover:text-zinc-900"
          >
            返回工作区首页
          </RouterLink>
          <RouterLink
            :to="{ name: 'author-post-create' }"
            class="rounded-xl bg-zinc-900 px-4 py-3 text-sm font-medium text-white transition hover:bg-zinc-700"
          >
            新建文章
          </RouterLink>
        </div>
      </div>

      <p v-if="error" class="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
        {{ error }}
      </p>
      <p v-if="success" class="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-600">
        {{ success }}
      </p>

      <StateBlock v-if="loading" title="正在加载" message="正在获取你的文章列表。" />
      <StateBlock v-else-if="!posts" title="暂无数据" message="当前无法获取文章列表。" tone="error" />
      <StateBlock
        v-else-if="posts.items.length === 0"
        title="还没有文章"
        message="你还没有发布任何文章，现在可以开始写第一篇。"
      />
      <div v-else class="space-y-4">
        <article
          v-for="post in posts.items"
          :key="post.id"
          class="rounded-2xl border border-zinc-200 bg-zinc-50 p-5"
        >
          <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
            <div class="min-w-0 flex-1">
              <div class="flex flex-wrap items-center gap-3">
                <h2 class="text-xl font-semibold text-zinc-900">{{ post.title }}</h2>
                <span class="rounded-full bg-emerald-100 px-3 py-1 text-xs font-medium text-emerald-700">
                  {{ post.status }}
                </span>
              </div>
              <p class="mt-2 text-sm text-zinc-500">
                Slug：{{ post.slug }} · 分类：{{ post.categoryName }}
              </p>
              <p v-if="post.summary" class="mt-3 text-sm leading-7 text-zinc-600">
                {{ post.summary }}
              </p>
              <dl class="mt-4 grid gap-2 text-sm text-zinc-500 md:grid-cols-2">
                <div>
                  <dt class="inline font-medium text-zinc-700">发布时间：</dt>
                  <dd class="inline">{{ formatTime(post.publishedAt) }}</dd>
                </div>
                <div>
                  <dt class="inline font-medium text-zinc-700">更新时间：</dt>
                  <dd class="inline">{{ formatTime(post.updatedAt) }}</dd>
                </div>
              </dl>
            </div>
            <div class="flex shrink-0 flex-wrap gap-3">
              <RouterLink
                :to="{ name: 'post-detail', params: { slug: post.slug } }"
                class="rounded-xl border border-zinc-300 px-4 py-2 text-sm font-medium text-zinc-700 transition hover:border-zinc-500 hover:text-zinc-900"
              >
                预览
              </RouterLink>
              <RouterLink
                :to="{ name: 'author-post-edit', params: { id: post.id } }"
                class="rounded-xl border border-zinc-300 px-4 py-2 text-sm font-medium text-zinc-700 transition hover:border-zinc-500 hover:text-zinc-900"
              >
                编辑
              </RouterLink>
              <button
                type="button"
                :disabled="deletingId === post.id"
                class="rounded-xl bg-red-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-red-500 disabled:cursor-not-allowed disabled:bg-red-300"
                @click="removePost(post)"
              >
                {{ deletingId === post.id ? '删除中...' : '删除' }}
              </button>
            </div>
          </div>
        </article>

        <PaginationBar :page="posts.page" :total-pages="posts.totalPages" @change="load" />
      </div>
    </section>
  </PublicLayout>
</template>
