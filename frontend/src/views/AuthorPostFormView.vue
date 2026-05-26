<script setup lang="ts">
import axios from 'axios'
import { MdEditor } from 'md-editor-v3'
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { createMyPost, fetchMyPostDetail, updateMyPost, uploadMyImage } from '../api/me'
import { fetchCategories } from '../api/public'
import PublicLayout from '../components/public/PublicLayout.vue'
import type { SaveMePostRequest } from '../types/me'
import type { PublicCategoryItem } from '../types/public'

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
const isEdit = computed(() => postId.value !== null)

const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const categoriesLoading = ref(true)
const categories = ref<PublicCategoryItem[]>([])
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

const pageTitle = computed(() => (isEdit.value ? '编辑文章' : '新建文章'))
const pageDescription = computed(() => (isEdit.value ? '修改你已经发布的文章内容。' : '填写文章基础信息并立即公开发布。'))
const submitLabel = computed(() => {
  if (saving.value) {
    return isEdit.value ? '保存中...' : '发布中...'
  }
  return isEdit.value ? '保存修改' : '立即发布'
})
const tagPreview = computed(() => toPayload().tagSlugs)

const uploadCover = async (event: Event) => {
  const input = event.target as HTMLInputElement | null
  const file = input?.files?.[0]
  if (!file) {
    return
  }

  uploading.value = true
  error.value = ''
  success.value = ''

  try {
    const data = await uploadMyImage(file)
    form.coverImageUrl = data.url
    success.value = '图片上传成功。'
  } catch (err) {
    if (axios.isAxiosError(err)) {
      error.value = err.response?.data?.message ?? '图片上传失败，请稍后重试。'
    } else {
      error.value = '图片上传失败，请稍后重试。'
    }
  } finally {
    uploading.value = false
    if (input) {
      input.value = ''
    }
  }
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

const toPayload = (): SaveMePostRequest => ({
  title: form.title.trim(),
  slug: form.slug.trim() || null,
  summary: form.summary.trim() || null,
  contentMarkdown: form.contentMarkdown.trim(),
  coverImageUrl: form.coverImageUrl.trim() || null,
  categorySlug: form.categorySlug,
  tagSlugs: form.tagSlugsText
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean),
})

const applyDetail = (data: {
  title: string
  slug: string
  summary: string | null
  contentMarkdown: string
  contentHtml: string
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

const loadCategories = async () => {
  categoriesLoading.value = true

  try {
    categories.value = await fetchCategories()
    if (!form.categorySlug && categories.value.length > 0) {
      form.categorySlug = categories.value[0].slug
    }
  } finally {
    categoriesLoading.value = false
  }
}

const loadPost = async () => {
  if (!isEdit.value || postId.value === null) {
    return
  }

  loading.value = true
  error.value = ''

  try {
    const data = await fetchMyPostDetail(postId.value)
    applyDetail(data)
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
  saving.value = true
  error.value = ''
  success.value = ''

  try {
    const payload = toPayload()
    const data = isEdit.value && postId.value !== null
      ? await updateMyPost(postId.value, payload)
      : await createMyPost(payload)

    success.value = isEdit.value ? '文章已保存。' : '文章已发布。'
    applyDetail(data)
    await router.replace({ name: 'author-post-edit', params: { id: data.id } })
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
  void loadCategories()
  void loadPost()
})
</script>

<template>
  <PublicLayout>
    <section class="space-y-6 rounded-3xl border border-zinc-200 bg-white p-8 shadow-sm">
      <div class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <p class="text-sm font-medium text-emerald-600">作者工作区</p>
          <h1 class="mt-2 text-3xl font-bold text-zinc-900">{{ pageTitle }}</h1>
          <p class="mt-2 text-sm text-zinc-500">{{ pageDescription }}</p>
        </div>
        <div class="flex flex-wrap gap-3">
          <RouterLink
            :to="{ name: 'author-posts' }"
            class="rounded-xl border border-zinc-300 px-4 py-3 text-sm font-medium text-zinc-700 transition hover:border-zinc-500 hover:text-zinc-900"
          >
            返回我的文章
          </RouterLink>
          <RouterLink
            :to="{ name: 'author-dashboard' }"
            class="rounded-xl border border-zinc-300 px-4 py-3 text-sm font-medium text-zinc-700 transition hover:border-zinc-500 hover:text-zinc-900"
          >
            返回工作区首页
          </RouterLink>
        </div>
      </div>

      <div
        v-if="loading || categoriesLoading"
        class="rounded-2xl border border-zinc-200 bg-zinc-50 px-4 py-5 text-sm text-zinc-500"
      >
        正在加载表单数据。
      </div>
      <form v-else class="space-y-5" @submit.prevent="submit">
        <div class="grid gap-5 lg:grid-cols-[minmax(0,2fr)_minmax(0,1fr)]">
          <div class="space-y-5">
            <div class="grid gap-5 md:grid-cols-2">
              <label class="block text-sm font-medium text-zinc-700">
                标题
                <input
                  v-model="form.title"
                  type="text"
                  maxlength="255"
                  class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500"
                />
              </label>

              <label class="block text-sm font-medium text-zinc-700">
                Slug
                <input
                  v-model="form.slug"
                  type="text"
                  maxlength="128"
                  placeholder="留空时根据标题自动生成"
                  class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500"
                />
              </label>
            </div>

            <label class="block text-sm font-medium text-zinc-700">
              摘要
              <textarea
                v-model="form.summary"
                rows="3"
                maxlength="512"
                class="mt-2 w-full rounded-2xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500"
              />
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
              <h2 class="text-lg font-semibold text-zinc-900">发布设置</h2>
              <p class="mt-2 text-sm text-zinc-500">当前保存后会直接公开发布。</p>
              <p v-if="uploading" class="mt-3 text-sm text-emerald-600">正在上传图片，请稍候。</p>
            </div>

            <label class="block text-sm font-medium text-zinc-700">
              分类
              <select
                v-model="form.categorySlug"
                class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500"
              >
                <option v-for="category in categories" :key="category.slug" :value="category.slug">
                  {{ category.name }}
                </option>
              </select>
            </label>

            <label class="block text-sm font-medium text-zinc-700">
              标签 slug
              <input
                v-model="form.tagSlugsText"
                type="text"
                placeholder="多个标签用英文逗号分隔"
                class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500"
              />
            </label>
            <div v-if="tagPreview.length > 0" class="flex flex-wrap gap-2">
              <span
                v-for="tag in tagPreview"
                :key="tag"
                class="rounded-full bg-zinc-100 px-3 py-1 text-xs font-medium text-zinc-600"
              >
                {{ tag }}
              </span>
            </div>

            <label class="block text-sm font-medium text-zinc-700">
              封面图链接
              <input
                v-model="form.coverImageUrl"
                type="url"
                maxlength="512"
                placeholder="https://example.com/cover.png"
                class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500"
              />
            </label>

            <div class="rounded-2xl border border-zinc-200 bg-zinc-50 p-4">
              <label class="block text-sm font-medium text-zinc-700">
                上传封面图
                <input
                  type="file"
                  accept="image/png,image/jpeg,image/gif,image/webp,image/svg+xml"
                  :disabled="uploading"
                  class="mt-2 block w-full text-sm text-zinc-600 file:mr-4 file:rounded-xl file:border-0 file:bg-zinc-900 file:px-4 file:py-2 file:text-sm file:font-medium file:text-white hover:file:bg-zinc-700 disabled:cursor-not-allowed"
                  @change="uploadCover"
                />
              </label>
              <p class="mt-2 text-sm text-zinc-500">
                支持 JPG、PNG、GIF、WEBP、SVG，大小不超过 5MB。
              </p>
            </div>

            <div v-if="form.coverImageUrl" class="overflow-hidden rounded-2xl border border-zinc-200 bg-white">
              <img :src="form.coverImageUrl" alt="封面图预览" class="h-48 w-full object-cover" />
            </div>
          </div>
        </div>

        <p v-if="error" class="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
          {{ error }}
        </p>
        <p v-if="success" class="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-600">
          {{ success }}
        </p>

        <button
          type="submit"
          :disabled="saving"
          class="rounded-xl bg-zinc-900 px-5 py-3 text-sm font-medium text-white transition hover:bg-zinc-700 disabled:cursor-not-allowed disabled:bg-zinc-400"
        >
          {{ submitLabel }}
        </button>
      </form>
    </section>
  </PublicLayout>
</template>
