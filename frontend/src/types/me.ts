export interface MeProfile {
  id: number
  username: string
  nickname: string
  avatarUrl: string | null
  bio: string | null
  role: string
}

export interface UpdateMeProfileRequest {
  nickname: string
  avatarUrl: string | null
  bio: string | null
}

export interface MePostListItem {
  id: number
  title: string
  slug: string
  summary: string | null
  categoryName: string
  status: string
  publishedAt: string | null
  updatedAt: string | null
}

export interface MePostDetail {
  id: number
  title: string
  slug: string
  summary: string | null
  contentMarkdown: string
  contentHtml: string
  coverImageUrl: string | null
  categorySlug: string
  tagSlugs: string[]
  status: string
  publishedAt: string | null
  updatedAt: string | null
}

export interface MeUpload {
  id: number
  fileName: string
  originalName: string
  contentType: string
  fileSize: number
  url: string
}

export interface SaveMePostRequest {
  title: string
  slug: string | null
  summary: string | null
  contentMarkdown: string
  coverImageUrl: string | null
  categorySlug: string
  tagSlugs: string[]
}
