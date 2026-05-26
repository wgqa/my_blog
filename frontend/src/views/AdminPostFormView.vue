<script setup lang="ts">
import axios from 'axios'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MdEditor } from 'md-editor-v3'
import AdminLayout from './AdminLayout.vue'
import { fetchAdminCategories } from '../api/admin'
import { fetchAdminPostDetail, updateAdminPost } from '../api/admin'
import { uploadMyImage } from '../api/me'
import type { AdminCategory } from '../types/admin'
import type { SaveMePostRequest } from '../types/me'

const route = useRoute()
const router = useRouter()

const postId = computed(() => {
  const raw = route.params.id
  if (typeof raw !== 'string') {
    return null
  }
  const value = Number(raw)
  return Number.isInteger(value) && value > 0 ? value : null
})

const loading = ref(true)
const saving = ref(false)
const uploading = ref(false)
const categories = ref<AdminCategory[]>([])
const error = ref('')
const success = ref('')
const form = reactive({
  title: '',
  slug: '',
  summary: '',
  contentMarkdown: '',
  coverImageUrl: '',
  categorySlug: '',
  tagSlugsText: '',
})

const tagPreview = computed(() => toPayload().tagSlugs)

const toPayload = (): SaveMePostRequest => ({
  title: form.title.trim(),
  slug: form.slug.trim() || null,
  summary: form.summary.trim() || null,
  contentMarkdown: form.contentMarkdown.trim(),
  coverImageUrl: form.coverImageUrl.trim() || null,
  categorySlug: form.categorySlug,
  tagSlugs: form.tagSlugsText.split(',').map((item) => item.trim()).filter(Boolean),
})

const applyDetail = (data: {
  title: string
  slug: string
  summary: string | null
  contentMarkdown: string
  coverImageUrl: string | null
  categorySlug: string
  tagSlugs: string[]
}) => {
  form.title = data.title
  form.slug = data.slug
  form.summary = data.summary ?? ''
  form.contentMarkdown = data.contentMarkdown
  form.coverImageUrl = data.coverImageUrl ?? ''
  form.categorySlug = data.categorySlug
  form.tagSlugsText = data.tagSlugs.join(', ')
}

const uploadEditorImage = async (files: File[], callback: (urls: string[]) => void) => {
  uploading.value = true
  error.value = ''
  success.value = ''

  try {
    const urls = await Promise.all(files.map(async (file) => (await uploadMyImage(file)).url))
    callback(urls)
    success.value = '编辑器图片上传成功。'
  } catch (err) {
    if (axios.isAxiosError(err)) {
      error.value = err.response?.data?.message ?? '编辑器图片上传失败，请稍后重试。'
    } else {
      error.value = '编辑器图片上传失败，请稍后重试。'
    }
  } finally {
    uploading.value = false
  }
}

const load = async () => {
  if (postId.value === null) {
    error.value = '文章 ID 无效。'
    loading.value = false
    return
  }

  loading.value = true
  error.value = ''

  try {
    const [categoryData, postData] = await Promise.all([
      fetchAdminCategories(),
      fetchAdminPostDetail(postId.value),
    ])
    categories.value = categoryData
    applyDetail(postData)
    if (!form.categorySlug && categoryData.length > 0) {
      form.categorySlug = categoryData[0].slug
    }
  } catch (err) {
    if (axios.isAxiosError(err)) {
      error.value = err.response?.data?.message ?? '文章详情加载失败，请稍后重试。'
    } else {
      error.value = '文章详情加载失败，请稍后重试。'
    }
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  if (postId.value === null) {
    return
  }

  saving.value = true
  error.value = ''
  success.value = ''

  try {
    const data = await updateAdminPost(postId.value, toPayload())
    applyDetail(data)
    success.value = '文章已保存。'
    await router.replace({ name: 'admin-post-edit', params: { id: data.id } })
  } catch (err) {
    if (axios.isAxiosError(err)) {
      error.value = err.response?.data?.message ?? '文章保存失败，请稍后重试。'
    } else {
      error.value = '文章保存失败，请稍后重试。'
    }
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <AdminLayout>
    <section class="space-y-6 rounded-3xl border border-zinc-200 bg-white p-8 shadow-sm">
      <div>
        <p class="text-sm font-medium text-violet-600">文章管理</p>
        <h2 class="mt-2 text-3xl font-bold text-zinc-900">编辑文章</h2>
        <p class="mt-2 text-sm text-zinc-500">管理员可以修改任意作者文章并直接保存为公开状态。</p>
      </div>

      <div v-if="loading" class="rounded-2xl border border-zinc-200 bg-zinc-50 px-4 py-5 text-sm text-zinc-500">
        正在加载文章数据。
      </div>

      <form v-else class="space-y-5" @submit.prevent="submit">
        <div class="grid gap-5 lg:grid-cols-[minmax(0,2fr)_minmax(0,1fr)]">
          <div class="space-y-5">
            <div class="grid gap-5 md:grid-cols-2">
              <label class="block text-sm font-medium text-zinc-700">
                标题
                <input v-model="form.title" type="text" maxlength="255" class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500" />
              </label>

              <label class="block text-sm font-medium text-zinc-700">
                Slug
                <input v-model="form.slug" type="text" maxlength="128" class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500" />
              </label>
            </div>

            <label class="block text-sm font-medium text-zinc-700">
              摘要
              <textarea v-model="form.summary" rows="3" maxlength="512" class="mt-2 w-full rounded-2xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500" />
            </label>

            <div class="block text-sm font-medium text-zinc-700">
              <div class="flex items-center justify-between gap-3">
                <span>Markdown 内容</span>
                <span class="text-xs text-zinc-500">支持在编辑器中直接上传图片并插入正文</span>
              </div>
              <div class="mt-2 overflow-hidden rounded-2xl border border-zinc-300 bg-white">
                <MdEditor
                  v-model="form.contentMarkdown"
                  language="zh-CN"
                  :toolbars-exclude="['github']"
                  :footers="['markdownTotal', '=', 'scrollSwitch']"
                  :placeholder="'请输入文章正文，支持 Markdown 与图片上传。'"
                  :no-upload-img="false"
                  class="author-md-editor"
                  @on-upload-img="uploadEditorImage"
                />
              </div>
            </div>
          </div>

          <div class="space-y-5">
            <div class="rounded-2xl border border-zinc-200 bg-zinc-50 p-5">
              <h3 class="text-lg font-semibold text-zinc-900">发布设置</h3>
              <p class="mt-2 text-sm text-zinc-500">当前保存后会直接公开发布。</p>
              <p v-if="uploading" class="mt-3 text-sm text-emerald-600">正在上传图片，请稍候。</p>
            </div>

            <label class="block text-sm font-medium text-zinc-700">
              分类
              <select v-model="form.categorySlug" class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500">
                <option v-for="category in categories" :key="category.id" :value="category.slug">
                  {{ category.name }}
                </option>
              </select>
            </label>

            <label class="block text-sm font-medium text-zinc-700">
              标签 slug
              <input v-model="form.tagSlugsText" type="text" placeholder="多个标签用英文逗号分隔" class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500" />
            </label>

            <div v-if="tagPreview.length > 0" class="flex flex-wrap gap-2">
              <span v-for="tag in tagPreview" :key="tag" class="rounded-full bg-zinc-100 px-3 py-1 text-xs font-medium text-zinc-600">{{ tag }}</span>
            </div>

            <label class="block text-sm font-medium text-zinc-700">
              封面图链接
              <input v-model="form.coverImageUrl" type="url" maxlength="512" class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500" />
            </label>

            <div v-if="form.coverImageUrl" class="overflow-hidden rounded-2xl border border-zinc-200 bg-white">
              <img :src="form.coverImageUrl" alt="封面图预览" class="h-48 w-full object-cover" />
            </div>
          </div>
        </div>

        <p v-if="error" class="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">{{ error }}</p>
        <p v-if="success" class="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-600">{{ success }}</p>

        <div class="flex gap-3">
          <RouterLink to="/admin/posts" class="rounded-xl border border-zinc-300 px-5 py-3 text-sm font-medium text-zinc-700 transition hover:border-zinc-500 hover:text-zinc-900">返回文章列表</RouterLink>
          <button type="submit" :disabled="saving" class="rounded-xl bg-zinc-900 px-5 py-3 text-sm font-medium text-white transition hover:bg-zinc-700 disabled:cursor-not-allowed disabled:bg-zinc-400">
            {{ saving ? '保存中...' : '保存修改' }}
          </button>
        </div>
      </form>
    </section>
  </AdminLayout>
</template>
