import { flushPromises, shallowMount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const authState = {
  isAuthenticated: false,
  isAdmin: false,
  user: null as null | { role: string },
  logout: vi.fn(),
}

const pushMock = vi.fn()
const routeState = {
  query: {
    keyword: '',
  } as Record<string, string>,
}

vi.mock('vue-router', async () => {
  const actual = await vi.importActual<typeof import('vue-router')>('vue-router')
  return {
    ...actual,
    useRoute: () => routeState,
    useRouter: () => ({ push: pushMock }),
  }
})

vi.mock('../../stores/auth', () => ({
  useAuthStore: () => authState,
}))

describe('PublicLayout', () => {
  beforeEach(() => {
    authState.isAuthenticated = false
    authState.isAdmin = false
    authState.user = null
    authState.logout.mockReset()
    pushMock.mockReset()
    routeState.query.keyword = 'vue'
  })

  afterEach(() => {
    vi.clearAllMocks()
  })

  it('shows login entry for guests', async () => {
    const { default: PublicLayout } = await import('./PublicLayout.vue')
    const wrapper = shallowMount(PublicLayout, {
      slots: {
        default: '<div>content</div>',
      },
      global: {
        stubs: {
          RouterLink: {
            template: '<a><slot /></a>',
          },
        },
      },
    })

    expect(wrapper.text()).toContain('登录')
    expect(wrapper.text()).not.toContain('退出登录')
  })

  it('shows admin dashboard entry and performs search/logout for authenticated admin', async () => {
    authState.isAuthenticated = true
    authState.isAdmin = true
    authState.user = { role: 'ADMIN' }

    const { default: PublicLayout } = await import('./PublicLayout.vue')
    const wrapper = shallowMount(PublicLayout, {
      slots: {
        default: '<div>content</div>',
      },
      global: {
        stubs: {
          RouterLink: {
            template: '<a><slot /></a>',
          },
        },
      },
    })

    await flushPromises()

    expect(wrapper.text()).toContain('进入后台')
    expect(wrapper.text()).toContain('退出登录')

    const input = wrapper.find('input[type="search"]')
    await input.setValue('  spring boot  ')
    await wrapper.find('form').trigger('submit.prevent')

    expect(pushMock).toHaveBeenCalledWith({
      name: 'search',
      query: { keyword: 'spring boot' },
    })

    const buttons = wrapper.findAll('button')
    await buttons[1].trigger('click')

    expect(authState.logout).toHaveBeenCalled()
    expect(pushMock).toHaveBeenLastCalledWith({ name: 'home' })
  })
})
