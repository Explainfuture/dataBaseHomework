import { Layout, Menu, Button, Space, Typography } from 'antd'
import type { ReactNode } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../store/auth'

const { Header, Content } = Layout

const menuItems = [{ key: '/', label: <Link to="/">首页</Link> }]

export default function AppLayout({ children }: { children: ReactNode }) {
  const location = useLocation()
  const navigate = useNavigate()
  const { user, clearAuth } = useAuth()

  const selectedKey = location.pathname.startsWith('/admin')
    ? '/admin'
    : location.pathname.startsWith('/posts/create')
      ? '/posts/create'
      : location.pathname.startsWith('/users/me/posts')
        ? '/users/me/posts'
        : location.pathname.startsWith('/users/me')
          ? '/users/me'
          : '/'
  const items = [...menuItems]
  if (user) {
    items.push(
      { key: '/posts/create', label: <Link to="/posts/create">发帖</Link> },
      { key: '/users/me/posts', label: <Link to="/users/me/posts">我的帖子</Link> },
      { key: '/users/me', label: <Link to="/users/me">个人中心</Link> },
    )
  }
  if (user?.role === 'ADMIN') {
    items.push({ key: '/admin', label: <Link to="/admin/pending">管理后台</Link> })
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ background: '#1677ff', padding: '0 24px' }}>
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            height: '100%',
          }}
        >
          <Space>
            <Typography.Title level={4} style={{ color: '#fff', margin: 0 }}>
              HNU 校园交流平台
            </Typography.Title>
            <Menu
              theme="dark"
              mode="horizontal"
              selectedKeys={[selectedKey]}
              items={items}
              style={{ background: 'transparent' }}
            />
          </Space>
          <Space>
            {user ? (
              <>
                <span style={{ color: '#fff' }}>你好，{user.nickname}</span>
                <Button
                  onClick={() => {
                    clearAuth()
                    navigate('/login')
                  }}
                >
                  退出登录
                </Button>
              </>
            ) : (
              <>
                <Button onClick={() => navigate('/login')}>登录</Button>
                <Button type="primary" onClick={() => navigate('/register')}>
                  注册
                </Button>
              </>
            )}
          </Space>
        </div>
      </Header>
      <Content style={{ padding: '24px 48px' }}>{children}</Content>
    </Layout>
  )
}
