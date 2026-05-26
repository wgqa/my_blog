export interface PageResponse<T> {
  items: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

export interface PublicPostListItem {
  title: string
  slug: string
  summary: string
  author: string
  publishedAt: string | null
  category: string
}

export interface PublicPostDetail {
  title: string
  slug: string
  summary: string | null
  contentHtml: string
  author: string
  authorUsername: string
  publishedAt: string | null
  category: string
  tags: string[]
}

export interface PublicCategoryItem {
  name: string
  slug: string
  description: string | null
}

export interface PublicAuthorProfile {
  username: string
  nickname: string
  avatarUrl: string | null
  bio: string | null
  posts: PageResponse<PublicPostListItem>
}
