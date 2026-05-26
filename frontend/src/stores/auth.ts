import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { login as loginRequest } from '../api/auth'
import type { AuthUser, LoginRequest } from '../types/auth'

const TOKEN_KEY = 'blog.auth.token'
const USER_KEY = 'blog.auth.user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref('')
  const user = ref<AuthUser | null>(null)

  const isAuthenticated = computed(() => Boolean(token.value))
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  const setSession = (nextToken: string, nextUser: AuthUser) => {
    token.value = nextToken
    user.value = nextUser
    localStorage.setItem(TOKEN_KEY, nextToken)
    localStorage.setItem(USER_KEY, JSON.stringify(nextUser))
  }

  const updateUserProfile = (payload: { nickname: string; avatarUrl?: string | null }) => {
    if (!user.value) {
      return
    }

    user.value = {
      ...user.value,
      nickname: payload.nickname,
    }
    localStorage.setItem(USER_KEY, JSON.stringify(user.value))
  }

  const clearSession = () => {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  const restore = () => {
    const storedToken = localStorage.getItem(TOKEN_KEY)
    const storedUser = localStorage.getItem(USER_KEY)

    if (!storedToken || !storedUser) {
      clearSession()
      return
    }

    try {
      setSession(storedToken, JSON.parse(storedUser) as AuthUser)
    } catch {
      clearSession()
    }
  }

  const login = async (payload: LoginRequest) => {
    const response = await loginRequest(payload)
    setSession(response.token, response.user)
    return response
  }

  const logout = () => {
    clearSession()
  }

  return {
    token,
    user,
    isAuthenticated,
    isAdmin,
    login,
    logout,
    restore,
    setSession,
    updateUserProfile,
    clearSession,
  }
})
