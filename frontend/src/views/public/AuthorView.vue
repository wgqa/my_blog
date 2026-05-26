<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { fetchAuthorProfile } from '../../api/public'
import PaginationBar from '../../components/public/PaginationBar.vue'
import PostCard from '../../components/public/PostCard.vue'
import PublicLayout from '../../components/public/PublicLayout.vue'
import StateBlock from '../../components/public/StateBlock.vue'
import type { PublicAuthorProfile } from '../../types/public'

const route = useRoute()
const profile = ref<PublicAuthorProfile | null>(null)
const loading = ref(true)
const error = ref('')

const load = async (page = 0) => {
  const username = String(route.params.username)
  loading.value = true
  error.value = ''
  try {
    profile.value = await fetchAuthorProfile(username, { page, size: 10 })
  } catch (err) {
    error.value = '作者主页加载失败，请确认作者是否存在。'
  } finally {
    loading.value = false
  }
}

watch(
  () => route.params.username,
  () => {
    void load()
  },
  { immediate: true },
)
</script>

<template>
  <PublicLayout>
    <StateBlock v-if="loading" title="正在加载" message="正在获取作者资料。" />
    <StateBlock v-else-if="error" title="加载失败" :message="error" tone="error" />
    <div v-else-if="profile" class="space-y-8">
      <section class="rounded-2xl border border-zinc-200 bg-white p-8 shadow-sm">
        <div class="flex flex-col gap-5 md:flex-row md:items-start">
          <div class="flex h-20 w-20 items-center justify-center rounded-full bg-zinc-900 text-2xl font-semibold text-white">
            {{ profile.nickname.slice(0, 1) }}
          </div>
          <div class="flex-1">
            <p class="text-sm uppercase tracking-[0.2em] text-zinc-400">Author</p>
            <h1 class="mt-2 text-3xl font-bold text-zinc-900">{{ profile.nickname }}</h1>
            <p class="mt-2 text-sm text-zinc-500">@{{ profile.username }}</p>
            <p class="mt-4 text-sm leading-7 text-zinc-600">{{ profile.bio || '这个作者还没有填写简介。' }}</p>
          </div>
        </div>
      </section>

      <section>
        <div class="mb-6">
          <h2 class="text-2xl font-semibold text-zinc-900">作者文章</h2>
          <p class="mt-2 text-sm text-zinc-500">该作者当前已公开发布的所有文章。</p>
        </div>
        <StateBlock
          v-if="profile.posts.items.length === 0"
          title="暂无公开文章"
          message="该作者目前还没有对外公开的文章。"
        />
        <div v-else class="space-y-5">
          <PostCard v-for="post in profile.posts.items" :key="post.slug" :post="post" />
          <PaginationBar :page="profile.posts.page" :total-pages="profile.posts.totalPages" @change="load" />
        </div>
      </section>
    </div>
  </PublicLayout>
</template>
