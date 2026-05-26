<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { fetchCategories, fetchPostsByCategory } from '../../api/public'
import CategoryList from '../../components/public/CategoryList.vue'
import PaginationBar from '../../components/public/PaginationBar.vue'
import PostCard from '../../components/public/PostCard.vue'
import PublicLayout from '../../components/public/PublicLayout.vue'
import StateBlock from '../../components/public/StateBlock.vue'
import type { PageResponse, PublicCategoryItem, PublicPostListItem } from '../../types/public'

const route = useRoute()
const posts = ref<PageResponse<PublicPostListItem> | null>(null)
const categories = ref<PublicCategoryItem[]>([])
const categoryName = ref('')
const loading = ref(true)
const error = ref('')

const load = async (page = 0) => {
  const slug = String(route.params.slug)
  loading.value = true
  error.value = ''
  try {
    const [categoryData, postData] = await Promise.all([
      fetchCategories(),
      fetchPostsByCategory(slug, { page, size: 10 }),
    ])
    categories.value = categoryData
    posts.value = postData
    categoryName.value = categoryData.find((item) => item.slug === slug)?.name ?? slug
  } catch (err) {
    error.value = '分类文章加载失败，请确认分类是否存在。'
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

onMounted(() => {
  void load()
})
</script>

<template>
  <PublicLayout>
    <div class="grid gap-8 lg:grid-cols-[280px_minmax(0,1fr)]">
      <aside>
        <CategoryList :categories="categories" />
      </aside>

      <section>
        <div class="mb-6">
          <h1 class="text-3xl font-bold text-zinc-900">分类：{{ categoryName }}</h1>
          <p class="mt-2 text-sm text-zinc-500">查看该分类下所有已公开文章。</p>
        </div>

        <StateBlock v-if="loading" title="正在加载" message="正在获取分类文章。" />
        <StateBlock v-else-if="error" title="加载失败" :message="error" tone="error" />
        <StateBlock
          v-else-if="posts && posts.items.length === 0"
          title="暂无文章"
          message="该分类下暂时还没有公开文章。"
        />
        <div v-else-if="posts" class="space-y-5">
          <PostCard v-for="post in posts.items" :key="post.slug" :post="post" />
          <PaginationBar :page="posts.page" :total-pages="posts.totalPages" @change="load" />
        </div>
      </section>
    </div>
  </PublicLayout>
</template>
