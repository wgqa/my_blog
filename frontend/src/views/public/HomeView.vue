<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchCategories, fetchPosts } from '../../api/public'
import CategoryList from '../../components/public/CategoryList.vue'
import PaginationBar from '../../components/public/PaginationBar.vue'
import PostCard from '../../components/public/PostCard.vue'
import PublicLayout from '../../components/public/PublicLayout.vue'
import StateBlock from '../../components/public/StateBlock.vue'
import type { PageResponse, PublicCategoryItem, PublicPostListItem } from '../../types/public'

const posts = ref<PageResponse<PublicPostListItem> | null>(null)
const categories = ref<PublicCategoryItem[]>([])
const loading = ref(true)
const error = ref('')

const load = async (page = 0) => {
  loading.value = true
  error.value = ''
  try {
    const [postData, categoryData] = await Promise.all([
      fetchPosts({ page, size: 10 }),
      fetchCategories(),
    ])
    posts.value = postData
    categories.value = categoryData
  } catch (err) {
    error.value = '公开文章列表加载失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <PublicLayout>
    <div class="grid gap-8 lg:grid-cols-[minmax(0,1fr)_280px]">
      <section>
        <div class="mb-6">
          <h1 class="text-3xl font-bold text-zinc-900">最新文章</h1>
          <p class="mt-2 text-sm text-zinc-500">浏览所有已公开发布的文章。</p>
        </div>

        <StateBlock v-if="loading" title="正在加载" message="正在获取文章与分类数据。" />
        <StateBlock v-else-if="error" title="加载失败" :message="error" tone="error" />
        <StateBlock
          v-else-if="posts && posts.items.length === 0"
          title="暂无文章"
          message="当前还没有已公开的文章。"
        />
        <div v-else-if="posts" class="space-y-5">
          <PostCard v-for="post in posts.items" :key="post.slug" :post="post" />
          <PaginationBar :page="posts.page" :total-pages="posts.totalPages" @change="load" />
        </div>
      </section>

      <aside>
        <CategoryList :categories="categories" />
      </aside>
    </div>
  </PublicLayout>
</template>
