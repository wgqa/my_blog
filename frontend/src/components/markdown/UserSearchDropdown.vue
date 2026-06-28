<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { searchUsers } from '../../api/me'
import type { UserSearchResult } from '../../types/me'

const props = defineProps<{ modelValue: string[] }>()
const emit = defineEmits<{ 'update:modelValue': [value: string[]] }>()

const query = ref('')
const results = ref<UserSearchResult[]>([])
const loading = ref(false)
const showDropdown = ref(false)

let debounceTimer: ReturnType<typeof setTimeout> | undefined

const selected = computed({
  get: () => props.modelValue,
  set: (val: string[]) => emit('update:modelValue', val),
})

const selectedUsers = ref<UserSearchResult[]>([])

watch(
  () => props.modelValue,
  async (usernames) => {
    if (usernames.length === 0) {
      selectedUsers.value = []
      return
    }
    const fetched: UserSearchResult[] = []
    for (const u of usernames) {
      const res = await searchUsers(u)
      const match = res.find((r) => r.username === u)
      if (match) fetched.push(match)
    }
    selectedUsers.value = fetched
  },
  { immediate: true },
)

const doSearch = async () => {
  const q = query.value.trim()
  if (!q) {
    results.value = []
    return
  }
  loading.value = true
  try {
    results.value = await searchUsers(q)
    showDropdown.value = true
  } finally {
    loading.value = false
  }
}

const onInput = () => {
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(doSearch, 300)
}

const selectUser = (user: UserSearchResult) => {
  if (!selected.value.includes(user.username)) {
    selected.value = [...selected.value, user.username]
    selectedUsers.value = [...selectedUsers.value, user]
  }
  query.value = ''
  results.value = []
  showDropdown.value = false
}

const removeUser = (username: string) => {
  selected.value = selected.value.filter((u) => u !== username)
  selectedUsers.value = selectedUsers.value.filter((u) => u.username !== username)
}

const focus = () => {
  if (results.value.length > 0) {
    showDropdown.value = true
  }
}

const blur = () => {
  setTimeout(() => {
    showDropdown.value = false
  }, 200)
}
</script>

<template>
  <div class="relative">
    <div class="flex flex-wrap gap-2">
      <span
        v-for="user in selectedUsers"
        :key="user.username"
        class="inline-flex items-center gap-1 rounded-full bg-emerald-100 px-3 py-1 text-xs font-medium text-emerald-700"
      >
        {{ user.nickname || user.username }}
        <button
          type="button"
          class="ml-1 text-emerald-500 hover:text-emerald-800"
          @click="removeUser(user.username)"
        >
          &times;
        </button>
      </span>
    </div>
    <input
      v-model="query"
      type="text"
      placeholder="搜索用户名..."
      class="mt-2 w-full rounded-xl border border-zinc-300 bg-white px-4 py-3 text-sm outline-none transition focus:border-zinc-500"
      @input="onInput"
      @focus="focus"
      @blur="blur"
    />
    <div
      v-if="showDropdown"
      class="absolute z-10 mt-1 max-h-48 w-full overflow-y-auto rounded-xl border border-zinc-200 bg-white shadow-lg"
    >
      <div v-if="loading" class="px-4 py-3 text-sm text-zinc-500">搜索中...</div>
      <div v-else-if="results.length === 0" class="px-4 py-3 text-sm text-zinc-500">无匹配用户</div>
      <button
        v-for="user in results"
        :key="user.username"
        type="button"
        class="w-full px-4 py-3 text-left text-sm transition hover:bg-zinc-50"
        :class="{ 'bg-emerald-50': selected.includes(user.username) }"
        :disabled="selected.includes(user.username)"
        @mousedown.prevent="selectUser(user)"
      >
        <span class="font-medium">{{ user.nickname }}</span>
        <span class="ml-2 text-zinc-400">@{{ user.username }}</span>
      </button>
    </div>
  </div>
</template>
