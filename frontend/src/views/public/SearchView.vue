<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchPosts } from '../../api/public'
import PaginationBar from '../../components/public/PaginationBar.vue'
import PostCard from '../../components/public/PostCard.vue'
import PublicLayout from '../../components/public/PublicLayout.vue'
import StateBlock from '../../components/public/StateBlock.vue'
import type { PageResponse, PublicPostListItem } from '../../types/public'

const route = useRoute()
const router = useRouter()
const posts = ref<PageResponse<PublicPostListItem> | null>(null)
const loading = ref(false)
const error = ref('')

const keyword = computed(() => {
  const value = route.query.keyword
  return typeof value === 'string' ? value : ''
})

const load = async (page = 0) => {
  if (!keyword.value.trim()) {
    posts.value = null
    error.value = ''
    return
  }

  loading.value = true
  error.value = ''
  try {
    posts.value = await searchPosts(keyword.value, { page, size: 10 })
  } catch (err) {
    error.value = '搜索失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}

watch(
  () => route.query.keyword,
  () => {
    void load()
  },
  { immediate: true },
)

const updatePage = (page: number) => {
  router.replace({ query: { ...route.query, page: String(page) } })
  void load(page)
}
</script>

<template>
  <PublicLayout>
    <section>
      <div class="mb-6">
        <h1 class="text-3xl font-bold text-zinc-900">搜索结果</h1>
        <p class="mt-2 text-sm text-zinc-500">
          当前关键词：<span class="font-medium text-zinc-900">{{ keyword || '未填写' }}</span>
        </p>
      </div>

      <StateBlock
        v-if="!keyword"
        title="请输入关键词"
        message="你可以在顶部搜索框中输入标题、摘要或正文关键词。"
      />
      <StateBlock v-else-if="loading" title="正在搜索" message="正在为你查找相关文章。" />
      <StateBlock v-else-if="error" title="搜索失败" :message="error" tone="error" />
      <StateBlock
        v-else-if="posts && posts.items.length === 0"
        title="没有找到结果"
        message="试试更换关键词，或者返回首页继续浏览。"
      />
      <div v-else-if="posts" class="space-y-5">
        <PostCard v-for="post in posts.items" :key="post.slug" :post="post" />
        <PaginationBar :page="posts.page" :total-pages="posts.totalPages" @change="updatePage" />
      </div>
    </section>
  </PublicLayout>
</template>
