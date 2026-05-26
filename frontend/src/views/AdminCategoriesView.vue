<script setup lang="ts">
import axios from 'axios'
import { onMounted, reactive, ref } from 'vue'
import AdminLayout from './AdminLayout.vue'
import StateBlock from '../components/public/StateBlock.vue'
import { createAdminCategory, deleteAdminCategory, fetchAdminCategories, updateAdminCategory } from '../api/admin'
import type { AdminCategory, SaveCategoryRequest } from '../types/admin'

const categories = ref<AdminCategory[]>([])
const loading = ref(true)
const submitting = ref(false)
const deletingId = ref<number | null>(null)
const editingId = ref<number | null>(null)
const error = ref('')
const success = ref('')
const form = reactive<SaveCategoryRequest>({
  name: '',
  slug: null,
  description: null,
})

const load = async () => {
  loading.value = true
  error.value = ''

  try {
    categories.value = await fetchAdminCategories()
  } catch (err) {
    if (axios.isAxiosError(err)) {
      error.value = err.response?.data?.message ?? '分类列表加载失败，请稍后重试。'
    } else {
      error.value = '分类列表加载失败，请稍后重试。'
    }
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.name = ''
  form.slug = null
  form.description = null
  editingId.value = null
}

const startEdit = (category: AdminCategory) => {
  editingId.value = category.id
  form.name = category.name
  form.slug = category.slug
  form.description = category.description
  error.value = ''
  success.value = ''
}

const submit = async () => {
  submitting.value = true
  error.value = ''
  success.value = ''

  const payload: SaveCategoryRequest = {
    name: form.name.trim(),
    slug: form.slug?.trim() || null,
    description: form.description?.trim() || null,
  }

  try {
    if (editingId.value === null) {
      await createAdminCategory(payload)
      success.value = '分类已创建。'
    } else {
      await updateAdminCategory(editingId.value, payload)
      success.value = '分类已更新。'
    }
    resetForm()
    await load()
  } catch (err) {
    if (axios.isAxiosError(err)) {
      error.value = err.response?.data?.message ?? '分类保存失败，请稍后重试。'
    } else {
      error.value = '分类保存失败，请稍后重试。'
    }
  } finally {
    submitting.value = false
  }
}

const removeCategory = async (category: AdminCategory) => {
  if (!window.confirm(`确认删除分类“${category.name}”吗？`)) {
    return
  }

  deletingId.value = category.id
  error.value = ''
  success.value = ''

  try {
    await deleteAdminCategory(category.id)
    success.value = '分类已删除。'
    if (editingId.value === category.id) {
      resetForm()
    }
    await load()
  } catch (err) {
    if (axios.isAxiosError(err)) {
      error.value = err.response?.data?.message ?? '分类删除失败，请稍后重试。'
    } else {
      error.value = '分类删除失败，请稍后重试。'
    }
  } finally {
    deletingId.value = null
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <AdminLayout>
    <section class="grid gap-6 xl:grid-cols-[minmax(0,2fr)_minmax(360px,1fr)]">
      <div class="space-y-6">
        <div class="rounded-3xl border border-zinc-200 bg-white p-8 shadow-sm">
          <h2 class="text-2xl font-bold text-zinc-900">分类管理</h2>
          <p class="mt-2 text-sm text-zinc-500">当前共有 {{ categories.length }} 个分类。</p>
        </div>

        <p v-if="error" class="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">{{ error }}</p>
        <p v-if="success" class="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-600">{{ success }}</p>

        <StateBlock v-if="loading" title="正在加载" message="正在获取分类列表。" />
        <StateBlock v-else-if="categories.length === 0" title="还没有分类" message="当前还没有分类，可以先创建第一个分类。" />
        <div v-else class="space-y-4">
          <article v-for="category in categories" :key="category.id" class="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
            <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
              <div>
                <h3 class="text-lg font-semibold text-zinc-900">{{ category.name }}</h3>
                <p class="mt-2 text-sm text-zinc-500">Slug：{{ category.slug }}</p>
                <p class="mt-3 text-sm leading-7 text-zinc-600">{{ category.description || '暂无分类描述。' }}</p>
              </div>
              <div class="flex shrink-0 flex-wrap gap-3">
                <button type="button" class="rounded-xl border border-zinc-300 px-4 py-2 text-sm font-medium text-zinc-700 transition hover:border-zinc-500 hover:text-zinc-900" @click="startEdit(category)">编辑</button>
                <button type="button" :disabled="deletingId === category.id" class="rounded-xl bg-red-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-red-500 disabled:cursor-not-allowed disabled:bg-red-300" @click="removeCategory(category)">
                  {{ deletingId === category.id ? '删除中...' : '删除' }}
                </button>
              </div>
            </div>
          </article>
        </div>
      </div>

      <form class="space-y-5 rounded-3xl border border-zinc-200 bg-white p-8 shadow-sm" @submit.prevent="submit">
        <div>
          <h2 class="text-2xl font-bold text-zinc-900">{{ editingId === null ? '创建分类' : '编辑分类' }}</h2>
          <p class="mt-2 text-sm text-zinc-500">Slug 留空时会根据名称自动生成。</p>
        </div>

        <label class="block text-sm font-medium text-zinc-700">
          分类名称
          <input v-model="form.name" type="text" maxlength="64" class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500" />
        </label>

        <label class="block text-sm font-medium text-zinc-700">
          分类 Slug
          <input v-model="form.slug" type="text" maxlength="64" class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500" />
        </label>

        <label class="block text-sm font-medium text-zinc-700">
          分类描述
          <textarea v-model="form.description" rows="4" maxlength="255" class="mt-2 w-full rounded-2xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500" />
        </label>

        <div class="flex gap-3">
          <button type="submit" :disabled="submitting" class="flex-1 rounded-xl bg-zinc-900 px-5 py-3 text-sm font-medium text-white transition hover:bg-zinc-700 disabled:cursor-not-allowed disabled:bg-zinc-400">
            {{ submitting ? '保存中...' : editingId === null ? '创建分类' : '保存修改' }}
          </button>
          <button v-if="editingId !== null" type="button" class="rounded-xl border border-zinc-300 px-5 py-3 text-sm font-medium text-zinc-700 transition hover:border-zinc-500 hover:text-zinc-900" @click="resetForm">
            取消
          </button>
        </div>
      </form>
    </section>
  </AdminLayout>
</template>
