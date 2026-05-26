export interface AuthUser {
  id: number
  username: string
  nickname: string
  role: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface AuthResponse {
  token: string
  user: AuthUser
}
