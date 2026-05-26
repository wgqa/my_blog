<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import PublicLayout from '../components/public/PublicLayout.vue'

const route = useRoute()

const links = [
  { path: '/admin', label: '仪表盘' },
  { path: '/admin/users', label: '用户管理' },
  { path: '/admin/posts', label: '文章管理' },
  { path: '/admin/categories', label: '分类管理' },
  { path: '/admin/tags', label: '标签管理' },
]

const currentPath = computed(() => route.path)
</script>

<template>
  <PublicLayout>
    <section class="space-y-6">
      <div class="rounded-3xl border border-zinc-200 bg-white p-8 shadow-sm">
        <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <p class="text-sm font-medium text-violet-600">管理后台</p>
            <h1 class="mt-2 text-3xl font-bold text-zinc-900">站点管理</h1>
            <p class="mt-2 text-sm text-zinc-500">维护用户、文章、分类、标签与站点统计数据。</p>
          </div>
          <nav class="flex flex-wrap gap-3">
            <RouterLink
              v-for="link in links"
              :key="link.path"
              :to="link.path"
              class="rounded-xl px-4 py-3 text-sm font-medium transition"
              :class="currentPath === link.path
                ? 'bg-zinc-900 text-white'
                : 'border border-zinc-300 text-zinc-700 hover:border-zinc-500 hover:text-zinc-900'"
            >
              {{ link.label }}
            </RouterLink>
          </nav>
        </div>
      </div>

      <slot />
    </section>
  </PublicLayout>
</template>
