import { createContext, useContext, useMemo, useState } from 'react'
import type { ReactNode } from 'react'

export type AuthUser = {
  userId: number
  nickname: string
  role: string
}

type AuthState = {
  token: string | null
  user: AuthUser | null
  setAuth: (token: string, user: AuthUser) => void
  clearAuth: () => void
}

const TOKEN_KEY = 'hnu_token'
const USER_KEY = 'hnu_user'

const AuthContext = createContext<AuthState | undefined>(undefined)

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
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
}

export function clearStoredUser() {
  localStorage.removeItem(USER_KEY)
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState<string | null>(getToken())
  const [user, setUserState] = useState<AuthUser | null>(getStoredUser())

  const value = useMemo<AuthState>(
    () => ({
      token,
      user,
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
    [token, user],
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
