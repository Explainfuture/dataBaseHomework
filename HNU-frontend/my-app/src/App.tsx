import { Navigate, Route, Routes } from 'react-router-dom'
import type { ReactNode } from 'react'
import { useAuth } from './store/auth'
import AppLayout from './components/AppLayout'
import LoginPage from './pages/Login'
import RegisterPage from './pages/Register'
import PostsPage from './pages/Posts'
import PostDetailPage from './pages/PostDetail'
import AdminPendingPage from './pages/AdminPending'
import CreatePostPage from './pages/CreatePost'
import MyPostsPage from './pages/MyPosts'
import ProfilePage from './pages/Profile'

function RequireAuth({ children }: { children: ReactNode }) {
  const { token, loading } = useAuth()
  if (loading) {
    return null
  }
  if (!token) {
    return <Navigate to="/login" replace />
  }
  return <>{children}</>
}

function RequireAdmin({ children }: { children: ReactNode }) {
  const { token, user, loading } = useAuth()
  if (loading) {
    return null
  }
  if (!token) {
    return <Navigate to="/login" replace />
  }
  if (user?.role !== 'ADMIN') {
    return <Navigate to="/" replace />
  }
  return <>{children}</>
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route
        path="/"
        element={
          <AppLayout>
            <PostsPage />
          </AppLayout>
        }
      />
      <Route
        path="/posts/:id"
        element={
          <AppLayout>
            <PostDetailPage />
          </AppLayout>
        }
      />
      <Route
        path="/posts/create"
        element={
          <RequireAuth>
            <AppLayout>
              <CreatePostPage />
            </AppLayout>
          </RequireAuth>
        }
      />
      <Route
        path="/users/me/posts"
        element={
          <RequireAuth>
            <AppLayout>
              <MyPostsPage />
            </AppLayout>
          </RequireAuth>
        }
      />
      <Route
        path="/users/me"
        element={
          <RequireAuth>
            <AppLayout>
              <ProfilePage />
            </AppLayout>
          </RequireAuth>
        }
      />
      <Route
        path="/admin/pending"
        element={
          <RequireAdmin>
            <AppLayout>
              <AdminPendingPage />
            </AppLayout>
          </RequireAdmin>
        }
      />
      <Route
        path="*"
        element={<Navigate to="/" replace />}
      />
    </Routes>
  )
}
