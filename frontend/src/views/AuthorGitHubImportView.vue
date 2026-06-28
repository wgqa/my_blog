<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getApiErrorMessage } from '../api/error'
import { importFromGitHub } from '../api/me'
import PublicLayout from '../components/public/PublicLayout.vue'

const router = useRouter()

const form = reactive({
  token: '',
  repo: '',
  path: '',
  branch: 'main',
})

const loading = ref(false)
const error = ref('')
const success = ref('')
const createdPostId = ref<number | null>(null)

const submit = async () => {
  loading.value = true
  error.value = ''
  success.value = ''
  createdPostId.value = null

  try {
    const result = await importFromGitHub({
      token: form.token.trim(),
      repo: form.repo.trim(),
      path: form.path.trim(),
      branch: form.branch.trim() || undefined,
    })
    createdPostId.value = result.id
    success.value = '文章导入成功！'
  } catch (err) {
    error.value = getApiErrorMessage(err, '导入失败，请检查 Token、仓库地址和文件路径是否正确。')
  } finally {
    loading.value = false
  }
}

const goEdit = () => {
  if (createdPostId.value) {
    router.push({ name: 'author-post-edit', params: { id: createdPostId.value } })
  }
}
</script>

<template>
  <PublicLayout>
    <section class="space-y-6 rounded-3xl border border-zinc-200 bg-white p-8 shadow-sm">
      <div>
        <p class="text-sm font-medium text-emerald-600">作者工作区</p>
        <h1 class="mt-2 text-3xl font-bold text-zinc-900">从 GitHub 导入</h1>
        <p class="mt-2 text-sm text-zinc-500">通过 GitHub Token 从指定仓库拉取 Markdown 文件，自动发布为文章。</p>
      </div>

      <p v-if="error" class="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">{{ error }}</p>
      <p v-if="success" class="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-600">{{ success }}</p>

      <form class="space-y-5" @submit.prevent="submit">
        <label class="block text-sm font-medium text-zinc-700">
          GitHub Token
          <input
            v-model="form.token"
            type="password"
            placeholder="ghp_xxxxxxxxxxxxxxxxxxxx"
            required
            class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500"
          />
          <p class="mt-1 text-xs text-zinc-400">需要 repo 或 contents 权限。</p>
        </label>

        <label class="block text-sm font-medium text-zinc-700">
          仓库地址
          <input
            v-model="form.repo"
            type="text"
            placeholder="owner/repo"
            required
            class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500"
          />
        </label>

        <label class="block text-sm font-medium text-zinc-700">
          文件路径
          <input
            v-model="form.path"
            type="text"
            placeholder="path/to/file.md"
            required
            class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500"
          />
        </label>

        <label class="block text-sm font-medium text-zinc-700">
          分支
          <input
            v-model="form.branch"
            type="text"
            placeholder="main"
            class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500"
          />
        </label>

        <div class="flex flex-wrap gap-3">
          <button
            type="submit"
            :disabled="loading"
            class="rounded-xl bg-zinc-900 px-5 py-3 text-sm font-medium text-white transition hover:bg-zinc-700 disabled:cursor-not-allowed disabled:bg-zinc-400"
          >
            {{ loading ? '导入中...' : '导入' }}
          </button>
          <button
            v-if="createdPostId"
            type="button"
            class="rounded-xl bg-emerald-600 px-5 py-3 text-sm font-medium text-white transition hover:bg-emerald-500"
            @click="goEdit"
          >
            编辑文章
          </button>
        </div>
      </form>
    </section>
  </PublicLayout>
</template>
