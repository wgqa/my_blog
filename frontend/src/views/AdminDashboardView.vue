<script setup lang="ts">
import axios from 'axios'
import { onMounted, ref } from 'vue'
import AdminLayout from './AdminLayout.vue'
import StateBlock from '../components/public/StateBlock.vue'
import { fetchAdminStats } from '../api/admin'
import type { AdminStats } from '../types/admin'

const stats = ref<AdminStats | null>(null)
const loading = ref(true)
const error = ref('')

const load = async () => {
  loading.value = true
  error.value = ''

  try {
    stats.value = await fetchAdminStats()
  } catch (err) {
    if (axios.isAxiosError(err)) {
      error.value = err.response?.data?.message ?? '统计数据加载失败，请稍后重试。'
    } else {
      error.value = '统计数据加载失败，请稍后重试。'
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <AdminLayout>
    <p v-if="error" class="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
      {{ error }}
    </p>

    <StateBlock v-if="loading" title="正在加载" message="正在获取后台统计数据。" />

    <div v-else-if="stats" class="grid gap-4 md:grid-cols-2 xl:grid-cols-5">
      <article class="rounded-3xl border border-zinc-200 bg-white p-6 shadow-sm">
        <p class="text-sm font-medium text-zinc-500">全部用户</p>
        <p class="mt-3 text-3xl font-bold text-zinc-900">{{ stats.totalUsers }}</p>
      </article>
      <article class="rounded-3xl border border-zinc-200 bg-white p-6 shadow-sm">
        <p class="text-sm font-medium text-zinc-500">作者账号</p>
        <p class="mt-3 text-3xl font-bold text-zinc-900">{{ stats.totalAuthors }}</p>
      </article>
      <article class="rounded-3xl border border-zinc-200 bg-white p-6 shadow-sm">
        <p class="text-sm font-medium text-zinc-500">公开文章</p>
        <p class="mt-3 text-3xl font-bold text-zinc-900">{{ stats.totalPublishedPosts }}</p>
      </article>
      <article class="rounded-3xl border border-zinc-200 bg-white p-6 shadow-sm">
        <p class="text-sm font-medium text-zinc-500">分类总数</p>
        <p class="mt-3 text-3xl font-bold text-zinc-900">{{ stats.totalCategories }}</p>
      </article>
      <article class="rounded-3xl border border-zinc-200 bg-white p-6 shadow-sm">
        <p class="text-sm font-medium text-zinc-500">标签总数</p>
        <p class="mt-3 text-3xl font-bold text-zinc-900">{{ stats.totalTags }}</p>
      </article>
    </div>
  </AdminLayout>
</template>
