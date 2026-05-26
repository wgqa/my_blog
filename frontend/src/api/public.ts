import { http } from './http'
import type {
  PageResponse,
  PublicAuthorProfile,
  PublicCategoryItem,
  PublicPostDetail,
  PublicPostListItem,
} from '../types/public'

export interface PaginationParams {
  page?: number
  size?: number
}

export const fetchPosts = async (params: PaginationParams = {}) => {
  const { data } = await http.get<PageResponse<PublicPostListItem>>('/public/posts', { params })
  return data
}

export const fetchCategories = async () => {
  const { data } = await http.get<PublicCategoryItem[]>('/public/categories')
  return data
}

export const fetchPostsByCategory = async (
  slug: string,
  params: PaginationParams = {},
) => {
  const { data } = await http.get<PageResponse<PublicPostListItem>>(`/public/categories/${slug}/posts`, {
    params,
  })
  return data
}

export const searchPosts = async (
  keyword: string,
  params: PaginationParams = {},
) => {
  const { data } = await http.get<PageResponse<PublicPostListItem>>('/public/search', {
    params: {
      keyword,
      ...params,
    },
  })
  return data
}

export const fetchPostDetail = async (slug: string) => {
  const { data } = await http.get<PublicPostDetail>(`/public/posts/${slug}`)
  return data
}

export const fetchAuthorProfile = async (
  username: string,
  params: PaginationParams = {},
) => {
  const { data } = await http.get<PublicAuthorProfile>(`/public/authors/${username}`, {
    params,
  })
  return data
}
