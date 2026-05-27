<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const keyword = ref('')

const dashboardLabel = computed(() => (authStore.isAdmin ? '进入后台' : '进入工作区'))
const dashboardTarget = computed(() => (authStore.isAdmin ? '/admin' : '/me'))
const showDashboardLink = computed(() => {
  const target = dashboardTarget.value
  return route.path !== target && !route.path.startsWith(`${target}/`)
})

watch(
  () => route.query.keyword,
  (value) => {
    keyword.value = typeof value === 'string' ? value : ''
  },
  { immediate: true },
)

const search = () => {
  const trimmed = keyword.value.trim()
  router.push({
    name: 'search',
    query: trimmed ? { keyword: trimmed } : {},
  })
}

const logout = () => {
  authStore.logout()
  void router.push({ name: 'home' })
}
</script>

<template>
  <div class="min-h-screen bg-zinc-50 text-zinc-900">
    <header class="border-b border-zinc-200 bg-white/90 backdrop-blur">
      <div class="mx-auto flex max-w-6xl flex-col gap-4 px-4 py-5 md:flex-row md:items-center md:justify-between">
        <div>
          <RouterLink to="/" class="text-2xl font-bold tracking-tight text-zinc-900">
            序章
          </RouterLink>
          <p class="mt-1 text-sm text-zinc-500">记录技术，也记录思路的生长</p>
        </div>
        <div class="flex w-full max-w-3xl flex-col gap-3 md:flex-row md:items-center md:justify-end">
          <form class="flex w-full max-w-xl gap-3" @submit.prevent="search">
            <input
              v-model="keyword"
              type="search"
              placeholder="搜索标题、摘要或正文关键词"
              class="w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500"
            />
            <button
              type="submit"
              class="rounded-xl bg-zinc-900 px-5 py-3 text-sm font-medium text-white transition hover:bg-zinc-700"
            >
              搜索
            </button>
          </form>
          <div class="flex items-center justify-end gap-3">
            <template v-if="authStore.isAuthenticated && authStore.user">
              <RouterLink
                v-if="showDashboardLink"
                :to="dashboardTarget"
                class="rounded-xl border border-zinc-300 px-4 py-3 text-sm font-medium text-zinc-700 transition hover:border-zinc-500 hover:text-zinc-900"
              >
                {{ dashboardLabel }}
              </RouterLink>
              <button
                type="button"
                class="rounded-xl bg-zinc-900 px-4 py-3 text-sm font-medium text-white transition hover:bg-zinc-700"
                @click="logout"
              >
                退出登录
              </button>
            </template>
            <RouterLink
              v-else
              :to="{ name: 'login' }"
              class="rounded-xl bg-zinc-900 px-4 py-3 text-sm font-medium text-white transition hover:bg-zinc-700"
            >
              登录
            </RouterLink>
          </div>
        </div>
      </div>
    </header>
    <main class="mx-auto max-w-6xl px-4 py-8">
      <slot />
    </main>
  </div>
</template>
