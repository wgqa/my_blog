import { http } from './http'
import type { AuthResponse, LoginRequest } from '../types/auth'

export const login = async (payload: LoginRequest) => {
  const { data } = await http.post<AuthResponse>('/auth/login', payload)
  return data
}
