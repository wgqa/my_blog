export interface AdminUserItem {
  id: number
  username: string
  nickname: string
  email: string | null
  role: string
  status: string
  createdAt: string | null
  updatedAt: string | null
}

export interface AdminUser {
  id: number
  username: string
  nickname: string
  email: string | null
  role: string
  status: string
}

export interface CreateAuthorRequest {
  username: string
  password: string
  nickname: string
  email: string | null
}

export interface UpdateUserStatusRequest {
  enabled: boolean
}

export interface AdminPostListItem {
  id: number
  title: string
  slug: string
  summary: string | null
  authorUsername: string
  authorNickname: string
  categoryName: string
  status: string
  publishedAt: string | null
  updatedAt: string | null
}

export interface AdminPostDetail {
  id: number
  title: string
  slug: string
  summary: string | null
  contentMarkdown: string
  contentHtml: string
  coverImageUrl: string | null
  authorUsername: string
  authorNickname: string
  categorySlug: string
  tagSlugs: string[]
  status: string
  publishedAt: string | null
  updatedAt: string | null
}

export interface AdminCategory {
  id: number
  name: string
  slug: string
  description: string | null
}

export interface SaveCategoryRequest {
  name: string
  slug: string | null
  description: string | null
}

export interface AdminTag {
  id: number
  name: string
  slug: string
}

export interface SaveTagRequest {
  name: string
  slug: string | null
}

export interface AdminStats {
  totalUsers: number
  totalAuthors: number
  totalPublishedPosts: number
  totalCategories: number
  totalTags: number
}
