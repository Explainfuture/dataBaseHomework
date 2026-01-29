import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import axios from 'axios'
import type { ApiResponse } from '../api/client'
import type { UserInfo } from '../api/types'

export type AuthUser = {
  userId: number
  nickname: string
  role: string
}

type AuthState = {
  token: string | null
  user: AuthUser | null
  loading: boolean
  setAuth: (token: string, user: AuthUser) => void
  clearAuth: () => void
}

const TOKEN_KEY = 'hnu_token'
const USER_KEY = 'hnu_user'
const BASE_URL = 'http://localhost:8080'

const AuthContext = createContext<AuthState | undefined>(undefined)

function notifyAuthUpdated() {
  window.dispatchEvent(new Event('auth:updated'))
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
  notifyAuthUpdated()
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
  notifyAuthUpdated()
}

export function getStoredUser(): AuthUser | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as AuthUser
  } catch {
    return null
  }
}

export function setStoredUser(user: AuthUser) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
  notifyAuthUpdated()
}

export function clearStoredUser() {
  localStorage.removeItem(USER_KEY)
  notifyAuthUpdated()
}

function mapUserInfo(data: UserInfo): AuthUser {
  return {
    userId:
      (data as unknown as { id?: number }).id ??
      (data as unknown as { userId?: number }).userId ??
      0,
    nickname: data.nickname,
    role: data.role,
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState<string | null>(getToken())
  const [user, setUserState] = useState<AuthUser | null>(getStoredUser())
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let canceled = false

    const bootstrap = async () => {
      const storedToken = getToken()
      if (!storedToken) {
        if (!canceled) {
          setUserState(null)
          setLoading(false)
        }
        return
      }
      try {
        const res = await axios.get<ApiResponse<UserInfo>>(`${BASE_URL}/api/v1/users/me`, {
          headers: {
            Authorization: `Bearer ${storedToken}`,
          },
          withCredentials: true,
        })
        const payload = res.data as ApiResponse<UserInfo> | UserInfo
        const data = 'data' in payload ? payload.data : payload
        if (data && !canceled) {
          const nextUser = mapUserInfo(data)
          setStoredUser(nextUser)
          setUserState(nextUser)
        }
      } catch {
        clearToken()
        clearStoredUser()
        if (!canceled) {
          setTokenState(null)
          setUserState(null)
        }
      } finally {
        if (!canceled) {
          setLoading(false)
        }
      }
    }

    bootstrap()
    return () => {
      canceled = true
    }
  }, [])

  useEffect(() => {
    const syncFromStorage = () => {
      setTokenState(getToken())
      setUserState(getStoredUser())
    }
    window.addEventListener('auth:updated', syncFromStorage)
    return () => {
      window.removeEventListener('auth:updated', syncFromStorage)
    }
  }, [])

  const value = useMemo<AuthState>(
    () => ({
      token,
      user,
      loading,
      setAuth: (newToken, newUser) => {
        setToken(newToken)
        setStoredUser(newUser)
        setTokenState(newToken)
        setUserState(newUser)
      },
      clearAuth: () => {
        clearToken()
        clearStoredUser()
        setTokenState(null)
        setUserState(null)
      },
    }),
    [token, user, loading],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return ctx
}
