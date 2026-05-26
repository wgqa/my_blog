import { http } from './http'
import type {
  AdminCategory,
  AdminPostDetail,
  AdminPostListItem,
  AdminStats,
  AdminTag,
  AdminUser,
  AdminUserItem,
  CreateAuthorRequest,
  SaveCategoryRequest,
  SaveTagRequest,
  UpdateUserStatusRequest,
} from '../types/admin'
import type { SaveMePostRequest } from '../types/me'
import type { PageResponse } from '../types/public'

export interface PaginationParams {
  page?: number
  size?: number
}

export const fetchAdminStats = async () => {
  const { data } = await http.get<AdminStats>('/admin/stats')
  return data
}

export const fetchAdminUsers = async (params: PaginationParams = {}) => {
  const { data } = await http.get<PageResponse<AdminUserItem>>('/admin/users', { params })
  return data
}

export const createAdminAuthor = async (payload: CreateAuthorRequest) => {
  const { data } = await http.post<AdminUser>('/admin/users', payload)
  return data
}

export const updateAdminUserStatus = async (id: number, payload: UpdateUserStatusRequest) => {
  const { data } = await http.put<AdminUser>(`/admin/users/${id}/status`, payload)
  return data
}

export const fetchAdminPosts = async (params: PaginationParams = {}) => {
  const { data } = await http.get<PageResponse<AdminPostListItem>>('/admin/posts', { params })
  return data
}

export const fetchAdminPostDetail = async (id: number) => {
  const { data } = await http.get<AdminPostDetail>(`/admin/posts/${id}`)
  return data
}

export const updateAdminPost = async (id: number, payload: SaveMePostRequest) => {
  const { data } = await http.put<AdminPostDetail>(`/admin/posts/${id}`, payload)
  return data
}

export const deleteAdminPost = async (id: number) => {
  await http.delete(`/admin/posts/${id}`)
}

export const fetchAdminCategories = async () => {
  const { data } = await http.get<AdminCategory[]>('/admin/categories')
  return data
}

export const createAdminCategory = async (payload: SaveCategoryRequest) => {
  const { data } = await http.post<AdminCategory>('/admin/categories', payload)
  return data
}

export const updateAdminCategory = async (id: number, payload: SaveCategoryRequest) => {
  const { data } = await http.put<AdminCategory>(`/admin/categories/${id}`, payload)
  return data
}

export const deleteAdminCategory = async (id: number) => {
  await http.delete(`/admin/categories/${id}`)
}

export const fetchAdminTags = async () => {
  const { data } = await http.get<AdminTag[]>('/admin/tags')
  return data
}

export const createAdminTag = async (payload: SaveTagRequest) => {
  const { data } = await http.post<AdminTag>('/admin/tags', payload)
  return data
}

export const updateAdminTag = async (id: number, payload: SaveTagRequest) => {
  const { data } = await http.put<AdminTag>(`/admin/tags/${id}`, payload)
  return data
}

export const deleteAdminTag = async (id: number) => {
  await http.delete(`/admin/tags/${id}`)
}
