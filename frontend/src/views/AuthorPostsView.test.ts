import { flushPromises, shallowMount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

const fetchMyPosts = vi.fn()
const deleteMyPost = vi.fn()

vi.mock('../api/me', () => ({
  fetchMyPosts,
  deleteMyPost,
}))

describe('AuthorPostsView', () => {
  it('loads post list and displays count', async () => {
    fetchMyPosts.mockResolvedValue({
      items: [
        {
          id: 1,
          title: '第一篇文章',
          slug: 'first-post',
          summary: '摘要',
          categoryName: 'Java',
          status: 'PUBLISHED',
          publishedAt: '2026-05-25T10:00:00',
          updatedAt: '2026-05-25T10:00:00',
        },
      ],
      total: 1,
      page: 0,
      size: 10,
      totalPages: 1,
    })

    const { default: AuthorPostsView } = await import('./AuthorPostsView.vue')
    const wrapper = shallowMount(AuthorPostsView, {
      global: {
        stubs: {
          PublicLayout: { template: '<div><slot /></div>' },
          RouterLink: { template: '<a><slot /></a>' },
          PaginationBar: true,
          StateBlock: true,
        },
      },
    })

    await flushPromises()

    expect(fetchMyPosts).toHaveBeenCalledWith({ page: 0, size: 10 })
    expect(wrapper.text()).toContain('共 1 篇已发布文章')
    expect(wrapper.text()).toContain('第一篇文章')
    expect(wrapper.text()).toContain('first-post')
  })

  it('deletes a post after confirmation and reloads current page', async () => {
    vi.stubGlobal('confirm', vi.fn(() => true))

    fetchMyPosts
      .mockResolvedValueOnce({
        items: [
          {
            id: 2,
            title: '待删除文章',
            slug: 'delete-me',
            summary: null,
            categoryName: 'Java',
            status: 'PUBLISHED',
            publishedAt: '2026-05-25T10:00:00',
            updatedAt: '2026-05-25T10:00:00',
          },
        ],
        total: 1,
        page: 0,
        size: 10,
        totalPages: 1,
      })
      .mockResolvedValueOnce({
        items: [],
        total: 0,
        page: 0,
        size: 10,
        totalPages: 0,
      })

    deleteMyPost.mockResolvedValue(undefined)

    const { default: AuthorPostsView } = await import('./AuthorPostsView.vue')
    const wrapper = shallowMount(AuthorPostsView, {
      global: {
        stubs: {
          PublicLayout: { template: '<div><slot /></div>' },
          RouterLink: { template: '<a><slot /></a>' },
          PaginationBar: true,
          StateBlock: true,
        },
      },
    })

    await flushPromises()
    await wrapper.find('button.bg-red-600').trigger('click')
    await flushPromises()

    expect(deleteMyPost).toHaveBeenCalledWith(2)
    expect(fetchMyPosts).toHaveBeenLastCalledWith({ page: 0, size: 10 })
    expect(wrapper.text()).toContain('文章已删除。')

    vi.unstubAllGlobals()
  })
})
