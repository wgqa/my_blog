<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { getApiErrorMessage } from '../api/error'
import AdminLayout from './AdminLayout.vue'
import PaginationBar from '../components/public/PaginationBar.vue'
import StateBlock from '../components/public/StateBlock.vue'
import { createAdminAuthor, fetchAdminUsers, updateAdminUserStatus } from '../api/admin'
import type { AdminUserItem, CreateAuthorRequest } from '../types/admin'
import type { PageResponse } from '../types/public'

const users = ref<PageResponse<AdminUserItem> | null>(null)
const loading = ref(true)
const submitting = ref(false)
const togglingId = ref<number | null>(null)
const error = ref('')
const success = ref('')
const form = reactive<CreateAuthorRequest>({
  username: '',
  password: '',
  nickname: '',
  email: null,
})

const userCountLabel = computed(() => `共 ${users.value?.total ?? 0} 个作者账号`)

const load = async (page = 0) => {
  loading.value = true
  error.value = ''

  try {
    users.value = await fetchAdminUsers({ page, size: 10 })
  } catch (err) {
    error.value = getApiErrorMessage(err, '作者列表加载失败，请稍后重试。')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.username = ''
  form.password = ''
  form.nickname = ''
  form.email = null
}

const submit = async () => {
  submitting.value = true
  error.value = ''
  success.value = ''

  try {
    await createAdminAuthor({
      username: form.username.trim(),
      password: form.password,
      nickname: form.nickname.trim(),
      email: form.email?.trim() || null,
    })
    success.value = '作者账号已创建。'
    resetForm()
    await load(users.value?.page ?? 0)
  } catch (err) {
    error.value = getApiErrorMessage(err, '作者账号创建失败，请稍后重试。')
  } finally {
    submitting.value = false
  }
}

const toggleStatus = async (user: AdminUserItem) => {
  togglingId.value = user.id
  error.value = ''
  success.value = ''

  try {
    await updateAdminUserStatus(user.id, { enabled: user.status !== 'ENABLED' })
    success.value = user.status === 'ENABLED' ? '作者账号已禁用。' : '作者账号已启用。'
    await load(users.value?.page ?? 0)
  } catch (err) {
    error.value = getApiErrorMessage(err, '作者状态更新失败，请稍后重试。')
  } finally {
    togglingId.value = null
  }
}

const formatTime = (value: string | null) => value ? new Date(value).toLocaleString('zh-CN') : '—'

onMounted(() => {
  void load()
})
</script>

<template>
  <AdminLayout>
    <section class="grid gap-6 xl:grid-cols-[minmax(0,2fr)_minmax(360px,1fr)]">
      <div class="space-y-6">
        <div class="rounded-3xl border border-zinc-200 bg-white p-8 shadow-sm">
          <h2 class="text-2xl font-bold text-zinc-900">用户管理</h2>
          <p class="mt-2 text-sm text-zinc-500">{{ userCountLabel }}</p>
        </div>

        <p v-if="error" class="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">{{ error }}</p>
        <p v-if="success" class="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-600">{{ success }}</p>

        <StateBlock v-if="loading" title="正在加载" message="正在获取作者账号列表。" />
        <StateBlock v-else-if="!users" title="暂无数据" message="当前无法获取作者账号列表。" tone="error" />
        <StateBlock v-else-if="users.items.length === 0" title="暂无作者" message="当前还没有作者账号，可以先创建第一个作者。" />
        <div v-else class="space-y-4">
          <article v-for="user in users.items" :key="user.id" class="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
            <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
              <div>
                <div class="flex flex-wrap items-center gap-3">
                  <h3 class="text-lg font-semibold text-zinc-900">{{ user.nickname }}</h3>
                  <span class="rounded-full px-3 py-1 text-xs font-medium" :class="user.status === 'ENABLED' ? 'bg-emerald-100 text-emerald-700' : 'bg-zinc-200 text-zinc-700'">
                    {{ user.status }}
                  </span>
                </div>
                <p class="mt-2 text-sm text-zinc-500">@{{ user.username }} · {{ user.email || '未填写邮箱' }}</p>
                <p class="mt-3 text-sm text-zinc-500">创建时间：{{ formatTime(user.createdAt) }} · 更新时间：{{ formatTime(user.updatedAt) }}</p>
              </div>
              <button
                type="button"
                :disabled="togglingId === user.id"
                class="rounded-xl px-4 py-2 text-sm font-medium text-white transition disabled:cursor-not-allowed"
                :class="user.status === 'ENABLED' ? 'bg-red-600 hover:bg-red-500 disabled:bg-red-300' : 'bg-emerald-600 hover:bg-emerald-500 disabled:bg-emerald-300'"
                @click="toggleStatus(user)"
              >
                {{ togglingId === user.id ? '提交中...' : user.status === 'ENABLED' ? '禁用账号' : '启用账号' }}
              </button>
            </div>
          </article>

          <PaginationBar :page="users.page" :total-pages="users.totalPages" @change="load" />
        </div>
      </div>

      <form class="space-y-5 rounded-3xl border border-zinc-200 bg-white p-8 shadow-sm" @submit.prevent="submit">
        <div>
          <h2 class="text-2xl font-bold text-zinc-900">创建作者</h2>
          <p class="mt-2 text-sm text-zinc-500">新账号创建后默认启用，可立即登录作者工作区。</p>
        </div>

        <label class="block text-sm font-medium text-zinc-700">
          用户名
          <input v-model="form.username" type="text" maxlength="64" class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500" />
        </label>

        <label class="block text-sm font-medium text-zinc-700">
          昵称
          <input v-model="form.nickname" type="text" maxlength="64" class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500" />
        </label>

        <label class="block text-sm font-medium text-zinc-700">
          密码
          <input v-model="form.password" type="password" maxlength="64" class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500" />
        </label>

        <label class="block text-sm font-medium text-zinc-700">
          邮箱
          <input v-model="form.email" type="email" maxlength="128" class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500" />
        </label>

        <button type="submit" :disabled="submitting" class="w-full rounded-xl bg-zinc-900 px-5 py-3 text-sm font-medium text-white transition hover:bg-zinc-700 disabled:cursor-not-allowed disabled:bg-zinc-400">
          {{ submitting ? '创建中...' : '创建作者账号' }}
        </button>
      </form>
    </section>
  </AdminLayout>
</template>
