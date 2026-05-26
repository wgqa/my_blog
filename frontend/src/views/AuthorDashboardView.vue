<script setup lang="ts">
import axios from 'axios'
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import PublicLayout from '../components/public/PublicLayout.vue'
import { fetchMyPosts, fetchMyProfile, updateMyProfile } from '../api/me'
import { useAuthStore } from '../stores/auth'
import type { MeProfile } from '../types/me'

const authStore = useAuthStore()
const profile = ref<MeProfile | null>(null)
const postCount = ref(0)
const loading = ref(true)
const saving = ref(false)
const error = ref('')
const success = ref('')
const form = reactive({
  nickname: '',
  avatarUrl: '',
  bio: '',
})

const displayName = computed(() => profile.value?.nickname || authStore.user?.nickname || '作者')
const avatarPreview = computed(() => form.avatarUrl.trim() || profile.value?.avatarUrl || '')

const applyProfile = (value: MeProfile) => {
  profile.value = value
  form.nickname = value.nickname
  form.avatarUrl = value.avatarUrl ?? ''
  form.bio = value.bio ?? ''
}

const loadProfile = async () => {
  loading.value = true
  error.value = ''

  try {
    const [profileData, postData] = await Promise.all([
      fetchMyProfile(),
      fetchMyPosts({ page: 0, size: 1 }),
    ])
    applyProfile(profileData)
    postCount.value = postData.total
  } catch (err) {
    if (axios.isAxiosError(err)) {
      error.value = err.response?.data?.message ?? '个人资料加载失败，请稍后重试。'
    } else {
      error.value = '个人资料加载失败，请稍后重试。'
    }
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  saving.value = true
  error.value = ''
  success.value = ''

  try {
    const data = await updateMyProfile({
      nickname: form.nickname.trim(),
      avatarUrl: form.avatarUrl.trim() || null,
      bio: form.bio.trim() || null,
    })
    applyProfile(data)
    authStore.updateUserProfile({
      nickname: data.nickname,
      avatarUrl: data.avatarUrl,
    })
    success.value = '个人资料已保存。'
  } catch (err) {
    if (axios.isAxiosError(err)) {
      error.value = err.response?.data?.message ?? '个人资料保存失败，请稍后重试。'
    } else {
      error.value = '个人资料保存失败，请稍后重试。'
    }
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  void loadProfile()
})
</script>

<template>
  <PublicLayout>
    <section class="space-y-6 rounded-3xl border border-zinc-200 bg-white p-8 shadow-sm">
      <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
        <div>
          <p class="text-sm font-medium text-emerald-600">作者工作区</p>
          <h1 class="mt-2 text-3xl font-bold text-zinc-900">你好，{{ displayName }}</h1>
          <p class="mt-2 text-sm text-zinc-500">在这里维护作者资料，并快速进入文章管理流程。</p>
        </div>
        <RouterLink
          :to="{ name: 'author-post-create' }"
          class="rounded-xl bg-zinc-900 px-4 py-3 text-sm font-medium text-white transition hover:bg-zinc-700"
        >
          写新文章
        </RouterLink>
      </div>

      <p v-if="error" class="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
        {{ error }}
      </p>
      <p v-if="success" class="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-600">
        {{ success }}
      </p>

      <div v-if="loading" class="rounded-2xl border border-zinc-200 bg-zinc-50 px-4 py-5 text-sm text-zinc-500">
        正在加载个人资料。
      </div>
      <div v-else class="space-y-6">
        <div class="grid gap-4 lg:grid-cols-[minmax(0,2fr)_minmax(0,1fr)_minmax(0,1fr)]">
          <div class="rounded-2xl border border-zinc-200 bg-zinc-50 p-5">
            <div class="flex items-center gap-4">
              <div class="flex h-16 w-16 items-center justify-center overflow-hidden rounded-full bg-zinc-200 text-lg font-semibold text-zinc-600">
                <img v-if="avatarPreview" :src="avatarPreview" alt="头像预览" class="h-full w-full object-cover" />
                <span v-else>{{ displayName.slice(0, 1) }}</span>
              </div>
              <div>
                <h2 class="text-lg font-semibold text-zinc-900">{{ displayName }}</h2>
                <p class="mt-1 text-sm text-zinc-500">@{{ profile?.username ?? authStore.user?.username ?? '' }}</p>
              </div>
            </div>
            <p class="mt-4 text-sm leading-7 text-zinc-600">
              {{ form.bio.trim() || '你还没有填写个人简介。' }}
            </p>
          </div>

          <RouterLink
            :to="{ name: 'author-posts' }"
            class="rounded-2xl border border-zinc-200 bg-zinc-50 p-5 transition hover:border-zinc-400 hover:bg-white"
          >
            <p class="text-sm font-medium text-zinc-500">我的文章</p>
            <p class="mt-3 text-3xl font-bold text-zinc-900">{{ postCount }}</p>
            <p class="mt-2 text-sm text-zinc-500">查看、编辑和删除已发布文章。</p>
          </RouterLink>

          <RouterLink
            :to="{ name: 'author-post-create' }"
            class="rounded-2xl border border-zinc-200 bg-zinc-50 p-5 transition hover:border-zinc-400 hover:bg-white"
          >
            <p class="text-sm font-medium text-zinc-500">快速开始</p>
            <p class="mt-3 text-lg font-semibold text-zinc-900">立即写作</p>
            <p class="mt-2 text-sm text-zinc-500">进入新建文章页面，直接发布新内容。</p>
          </RouterLink>
        </div>

        <div class="grid gap-5 md:grid-cols-2">
          <label class="block text-sm font-medium text-zinc-700">
            用户名
            <input
              :value="profile?.username ?? authStore.user?.username ?? ''"
              type="text"
              disabled
              class="mt-2 w-full rounded-xl border border-zinc-200 bg-zinc-100 px-4 py-3 text-sm text-zinc-500"
            />
          </label>
          <label class="block text-sm font-medium text-zinc-700">
            角色
            <input
              :value="profile?.role ?? authStore.user?.role ?? ''"
              type="text"
              disabled
              class="mt-2 w-full rounded-xl border border-zinc-200 bg-zinc-100 px-4 py-3 text-sm text-zinc-500"
            />
          </label>
        </div>

        <form class="space-y-5" @submit.prevent="submit">
          <label class="block text-sm font-medium text-zinc-700">
            昵称
            <input
              v-model="form.nickname"
              type="text"
              maxlength="64"
              class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500"
            />
          </label>

          <label class="block text-sm font-medium text-zinc-700">
            头像链接
            <input
              v-model="form.avatarUrl"
              type="url"
              maxlength="512"
              placeholder="https://example.com/avatar.png"
              class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500"
            />
          </label>

          <label class="block text-sm font-medium text-zinc-700">
            个人简介
            <textarea
              v-model="form.bio"
              rows="6"
              maxlength="2000"
              class="mt-2 w-full rounded-2xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500"
            />
          </label>

          <button
            type="submit"
            :disabled="saving"
            class="rounded-xl bg-zinc-900 px-5 py-3 text-sm font-medium text-white transition hover:bg-zinc-700 disabled:cursor-not-allowed disabled:bg-zinc-400"
          >
            {{ saving ? '保存中...' : '保存资料' }}
          </button>
        </form>
      </div>
    </section>
  </PublicLayout>
</template>
