import { http } from './http'
import type {
  GitHubImportRequest,
  MeAccessiblePostDetail,
  MeAccessiblePostItem,
  MePostDetail,
  MePostListItem,
  MeProfile,
  MeUpload,
  SaveMePostRequest,
  UpdateMeProfileRequest,
  UserSearchResult,
} from '../types/me'
import type { PageResponse } from '../types/public'

export interface PaginationParams {
  page?: number
  size?: number
}

export const fetchMyProfile = async () => {
  const { data } = await http.get<MeProfile>('/me/profile')
  return data
}

export const updateMyProfile = async (payload: UpdateMeProfileRequest) => {
  const { data } = await http.put<MeProfile>('/me/profile', payload)
  return data
}

export const fetchMyPosts = async (params: PaginationParams = {}) => {
  const { data } = await http.get<PageResponse<MePostListItem>>('/me/posts', { params })
  return data
}

export const fetchMyPostDetail = async (id: number) => {
  const { data } = await http.get<MePostDetail>(`/me/posts/${id}`)
  return data
}

export const createMyPost = async (payload: SaveMePostRequest) => {
  const { data } = await http.post<MePostDetail>('/me/posts', payload)
  return data
}

export const updateMyPost = async (id: number, payload: SaveMePostRequest) => {
  const { data } = await http.put<MePostDetail>(`/me/posts/${id}`, payload)
  return data
}

export const deleteMyPost = async (id: number) => {
  await http.delete(`/me/posts/${id}`)
}

export const uploadMyImage = async (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  const { data } = await http.post<MeUpload>('/me/uploads', formData)
  return data
}

export const searchUsers = async (q: string) => {
  const { data } = await http.get<UserSearchResult[]>('/me/users/search', { params: { q } })
  return data
}

export const fetchAccessiblePosts = async (params: PaginationParams = {}) => {
  const { data } = await http.get<PageResponse<MeAccessiblePostItem>>('/me/accessible-posts', { params })
  return data
}

export const fetchAccessiblePostDetail = async (id: number) => {
  const { data } = await http.get<MeAccessiblePostDetail>(`/me/accessible-posts/${id}`)
  return data
}

export const importFromGitHub = async (payload: GitHubImportRequest) => {
  const { data } = await http.post<MePostDetail>('/me/github-import', payload)
  return data
}
