import { flushPromises, shallowMount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

const fetchAdminStats = vi.fn()

vi.mock('../api/admin', () => ({
  fetchAdminStats,
}))

describe('AdminDashboardView', () => {
  it('renders stats after loading', async () => {
    fetchAdminStats.mockResolvedValue({
      totalUsers: 10,
      totalAuthors: 4,
      totalPublishedPosts: 12,
      totalCategories: 3,
      totalTags: 8,
    })

    const { default: AdminDashboardView } = await import('./AdminDashboardView.vue')
    const wrapper = shallowMount(AdminDashboardView, {
      global: {
        stubs: {
          AdminLayout: { template: '<div><slot /></div>' },
          StateBlock: true,
        },
      },
    })

    await flushPromises()

    expect(fetchAdminStats).toHaveBeenCalledTimes(1)
    expect(wrapper.text()).toContain('10')
    expect(wrapper.text()).toContain('12')
    expect(wrapper.text()).toContain('标签总数')
  })

  it('shows fallback error message when loading fails', async () => {
    fetchAdminStats.mockRejectedValueOnce(new Error('network'))

    const { default: AdminDashboardView } = await import('./AdminDashboardView.vue')
    const wrapper = shallowMount(AdminDashboardView, {
      global: {
        stubs: {
          AdminLayout: { template: '<div><slot /></div>' },
          StateBlock: true,
        },
      },
    })

    await flushPromises()

    expect(wrapper.text()).toContain('统计数据加载失败，请稍后重试。')
  })
})
