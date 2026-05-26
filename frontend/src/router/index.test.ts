import { describe, expect, it, vi } from 'vitest'

const authState = {
  isAuthenticated: false,
  isAdmin: false,
}

vi.mock('../stores/auth', () => ({
  useAuthStore: () => authState,
}))

describe('router guards', () => {
  it('redirects unauthenticated users to login with redirect query', async () => {
    const { default: router } = await import('./index')

    const result = await router.push('/me/posts')
    await router.isReady()

    expect(router.currentRoute.value.name).toBe('login')
    expect(router.currentRoute.value.query.redirect).toBe('/me/posts')
    expect(result).toBeUndefined()
  })

  it('redirects authenticated admin away from guest page to admin dashboard', async () => {
    authState.isAuthenticated = true
    authState.isAdmin = true

    const { default: router } = await import('./index')

    await router.push('/login')
    await router.isReady()

    expect(router.currentRoute.value.fullPath).toBe('/admin')

    authState.isAuthenticated = false
    authState.isAdmin = false
  })

  it('redirects non-admin users away from admin routes', async () => {
    authState.isAuthenticated = true
    authState.isAdmin = false

    const { default: router } = await import('./index')

    await router.push('/admin/users')
    await router.isReady()

    expect(router.currentRoute.value.fullPath).toBe('/')

    authState.isAuthenticated = false
    authState.isAdmin = false
  })
})
