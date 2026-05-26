import { createPinia, setActivePinia } from 'pinia'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const loginMock = vi.fn()

vi.mock('../api/auth', () => ({
  login: loginMock,
}))

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    loginMock.mockReset()
  })

  afterEach(() => {
    vi.resetModules()
  })

  it('restores stored session', async () => {
    localStorage.setItem('blog.auth.token', 'stored-token')
    localStorage.setItem('blog.auth.user', JSON.stringify({
      id: 1,
      username: 'admin',
      nickname: '管理员',
      role: 'ADMIN',
    }))

    const { useAuthStore } = await import('./auth')
    const store = useAuthStore()

    store.restore()

    expect(store.isAuthenticated).toBe(true)
    expect(store.isAdmin).toBe(true)
    expect(store.user?.nickname).toBe('管理员')
  })

  it('clears broken stored session', async () => {
    localStorage.setItem('blog.auth.token', 'stored-token')
    localStorage.setItem('blog.auth.user', '{broken')

    const { useAuthStore } = await import('./auth')
    const store = useAuthStore()

    store.restore()

    expect(store.isAuthenticated).toBe(false)
    expect(store.user).toBeNull()
    expect(localStorage.getItem('blog.auth.token')).toBeNull()
  })

  it('logs in and persists session', async () => {
    loginMock.mockResolvedValue({
      token: 'new-token',
      user: {
        id: 2,
        username: 'alice',
        nickname: 'Alice',
        role: 'AUTHOR',
      },
    })

    const { useAuthStore } = await import('./auth')
    const store = useAuthStore()

    await store.login({ username: 'alice', password: 'secret' })

    expect(store.token).toBe('new-token')
    expect(store.isAuthenticated).toBe(true)
    expect(store.isAdmin).toBe(false)
    expect(localStorage.getItem('blog.auth.token')).toBe('new-token')
  })
})
