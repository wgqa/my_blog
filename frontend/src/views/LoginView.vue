<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getApiErrorMessage } from '../api/error'
import PublicLayout from '../components/public/PublicLayout.vue'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const form = reactive({
  username: '',
  password: '',
})
const loading = ref(false)
const error = ref('')

const redirectTarget = computed(() => {
  const redirect = route.query.redirect
  if (typeof redirect === 'string' && redirect) {
    return redirect
  }
  return authStore.isAdmin ? '/admin' : '/me'
})

const submit = async () => {
  if (!form.username.trim() || !form.password.trim()) {
    error.value = '请输入用户名和密码。'
    return
  }

  loading.value = true
  error.value = ''

  try {
    await authStore.login({
      username: form.username.trim(),
      password: form.password,
    })
    await router.push(redirectTarget.value)
  } catch (err) {
    error.value = getApiErrorMessage(err, '登录失败，请检查用户名和密码。')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <PublicLayout>
    <div class="mx-auto max-w-md rounded-3xl border border-zinc-200 bg-white p-8 shadow-sm">
      <div class="mb-8 text-center">
        <h1 class="text-3xl font-bold text-zinc-900">登录</h1>
        <p class="mt-2 text-sm text-zinc-500">登录后进入作者工作区或管理后台。</p>
      </div>

      <form class="space-y-5" @submit.prevent="submit">
        <label class="block text-sm font-medium text-zinc-700">
          用户名
          <input
            v-model="form.username"
            type="text"
            autocomplete="username"
            class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500"
          />
        </label>

        <label class="block text-sm font-medium text-zinc-700">
          密码
          <input
            v-model="form.password"
            type="password"
            autocomplete="current-password"
            class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500"
          />
        </label>

        <p v-if="error" class="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
          {{ error }}
        </p>

        <button
          type="submit"
          :disabled="loading"
          class="w-full rounded-xl bg-zinc-900 px-5 py-3 text-sm font-medium text-white transition hover:bg-zinc-700 disabled:cursor-not-allowed disabled:bg-zinc-400"
        >
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
    </div>
  </PublicLayout>
</template>
