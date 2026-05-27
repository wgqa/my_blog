import { flushPromises, shallowMount } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'

const createMyPost = vi.fn()
const fetchMyPostDetail = vi.fn()
const updateMyPost = vi.fn()
const uploadMyImage = vi.fn()
const fetchCategories = vi.fn()

vi.mock('../api/me', () => ({
  createMyPost,
  fetchMyPostDetail,
  updateMyPost,
  uploadMyImage,
}))

vi.mock('../api/public', () => ({
  fetchCategories,
}))

vi.mock('md-editor-v3', () => ({
  MdEditor: {
    template: '<textarea />',
    props: ['modelValue'],
    emits: ['update:modelValue', 'onUploadImg'],
  },
}))

vi.mock('vue-router', async () => {
  const actual = await vi.importActual<typeof import('vue-router')>('vue-router')
  return {
    ...actual,
    useRoute: () => ({ params: {} }),
    useRouter: () => ({
      replace: vi.fn(),
      push: vi.fn(),
    }),
  }
})

describe('AuthorPostFormView', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('shows field error message from backend when save fails validation', async () => {
    fetchCategories.mockResolvedValue([{ slug: 'java', name: 'Java' }])
    createMyPost.mockRejectedValueOnce({
      isAxiosError: true,
      response: {
        data: {
          status: 400,
          message: '请求参数校验失败',
          fieldErrors: [{ field: 'categorySlug', message: '分类不能为空' }],
        },
      },
    })

    const { default: AuthorPostFormView } = await import('./AuthorPostFormView.vue')
    const wrapper = shallowMount(AuthorPostFormView, {
      global: {
        stubs: {
          PublicLayout: { template: '<div><slot /></div>' },
          RouterLink: { template: '<a><slot /></a>' },
        },
      },
    })

    await flushPromises()
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    expect(wrapper.text()).toContain('分类不能为空')
    expect(wrapper.text()).not.toContain('请求参数校验失败')
  })
})
