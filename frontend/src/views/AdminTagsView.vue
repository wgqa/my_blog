<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { getApiErrorMessage } from '../api/error'
import AdminLayout from './AdminLayout.vue'
import StateBlock from '../components/public/StateBlock.vue'
import { createAdminTag, deleteAdminTag, fetchAdminTags, updateAdminTag } from '../api/admin'
import type { AdminTag, SaveTagRequest } from '../types/admin'

const tags = ref<AdminTag[]>([])
const loading = ref(true)
const submitting = ref(false)
const deletingId = ref<number | null>(null)
const editingId = ref<number | null>(null)
const error = ref('')
const success = ref('')
const form = reactive<SaveTagRequest>({
  name: '',
  slug: null,
})

const load = async () => {
  loading.value = true
  error.value = ''

  try {
    tags.value = await fetchAdminTags()
  } catch (err) {
    error.value = getApiErrorMessage(err, '标签列表加载失败，请稍后重试。')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.name = ''
  form.slug = null
  editingId.value = null
}

const startEdit = (tag: AdminTag) => {
  editingId.value = tag.id
  form.name = tag.name
  form.slug = tag.slug
  error.value = ''
  success.value = ''
}

const submit = async () => {
  submitting.value = true
  error.value = ''
  success.value = ''

  const payload: SaveTagRequest = {
    name: form.name.trim(),
    slug: form.slug?.trim() || null,
  }

  try {
    if (editingId.value === null) {
      await createAdminTag(payload)
      success.value = '标签已创建。'
    } else {
      await updateAdminTag(editingId.value, payload)
      success.value = '标签已更新。'
    }
    resetForm()
    await load()
  } catch (err) {
    error.value = getApiErrorMessage(err, '标签保存失败，请稍后重试。')
  } finally {
    submitting.value = false
  }
}

const removeTag = async (tag: AdminTag) => {
  if (!window.confirm(`确认删除标签“${tag.name}”吗？`)) {
    return
  }

  deletingId.value = tag.id
  error.value = ''
  success.value = ''

  try {
    await deleteAdminTag(tag.id)
    success.value = '标签已删除。'
    if (editingId.value === tag.id) {
      resetForm()
    }
    await load()
  } catch (err) {
    error.value = getApiErrorMessage(err, '标签删除失败，请稍后重试。')
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
    <section class="grid gap-6 xl:grid-cols-[minmax(0,2fr)_minmax(320px,1fr)]">
      <div class="space-y-6">
        <div class="rounded-3xl border border-zinc-200 bg-white p-8 shadow-sm">
          <h2 class="text-2xl font-bold text-zinc-900">标签管理</h2>
          <p class="mt-2 text-sm text-zinc-500">当前共有 {{ tags.length }} 个标签。</p>
        </div>

        <p v-if="error" class="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">{{ error }}</p>
        <p v-if="success" class="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-600">{{ success }}</p>

        <StateBlock v-if="loading" title="正在加载" message="正在获取标签列表。" />
        <StateBlock v-else-if="tags.length === 0" title="还没有标签" message="当前还没有标签，可以先创建第一个标签。" />
        <div v-else class="grid gap-4 md:grid-cols-2">
          <article v-for="tag in tags" :key="tag.id" class="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
            <h3 class="text-lg font-semibold text-zinc-900">{{ tag.name }}</h3>
            <p class="mt-2 text-sm text-zinc-500">Slug：{{ tag.slug }}</p>
            <div class="mt-4 flex gap-3">
              <button type="button" class="rounded-xl border border-zinc-300 px-4 py-2 text-sm font-medium text-zinc-700 transition hover:border-zinc-500 hover:text-zinc-900" @click="startEdit(tag)">编辑</button>
              <button type="button" :disabled="deletingId === tag.id" class="rounded-xl bg-red-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-red-500 disabled:cursor-not-allowed disabled:bg-red-300" @click="removeTag(tag)">
                {{ deletingId === tag.id ? '删除中...' : '删除' }}
              </button>
            </div>
          </article>
        </div>
      </div>

      <form class="space-y-5 rounded-3xl border border-zinc-200 bg-white p-8 shadow-sm" @submit.prevent="submit">
        <div>
          <h2 class="text-2xl font-bold text-zinc-900">{{ editingId === null ? '创建标签' : '编辑标签' }}</h2>
          <p class="mt-2 text-sm text-zinc-500">Slug 留空时会根据名称自动生成。</p>
        </div>

        <label class="block text-sm font-medium text-zinc-700">
          标签名称
          <input v-model="form.name" type="text" maxlength="64" class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500" />
        </label>

        <label class="block text-sm font-medium text-zinc-700">
          标签 Slug
          <input v-model="form.slug" type="text" maxlength="64" class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500" />
        </label>

        <div class="flex gap-3">
          <button type="submit" :disabled="submitting" class="flex-1 rounded-xl bg-zinc-900 px-5 py-3 text-sm font-medium text-white transition hover:bg-zinc-700 disabled:cursor-not-allowed disabled:bg-zinc-400">
            {{ submitting ? '保存中...' : editingId === null ? '创建标签' : '保存修改' }}
          </button>
          <button v-if="editingId !== null" type="button" class="rounded-xl border border-zinc-300 px-5 py-3 text-sm font-medium text-zinc-700 transition hover:border-zinc-500 hover:text-zinc-900" @click="resetForm">
            取消
          </button>
        </div>
      </form>
    </section>
  </AdminLayout>
</template>
