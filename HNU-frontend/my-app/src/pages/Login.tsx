import { Button, Card, Form, Input, message, Typography } from 'antd'
import { useNavigate } from 'react-router-dom'
import { api } from '../api/client'
import type { ApiResponse } from '../api/client'
import type { LoginResponse } from '../api/types'
import { useAuth } from '../store/auth'

export default function LoginPage() {
  const navigate = useNavigate()
  const { setAuth } = useAuth()

  const onFinish = async (values: { phone: string; password: string }) => {
    try {
      const res = await api.post<ApiResponse<LoginResponse>>('/api/v1/auth/login', values)
      const payload = res as unknown as ApiResponse<LoginResponse>
      setAuth(payload.data.token, {
        userId: payload.data.userId,
        nickname: payload.data.nickname,
        role: payload.data.role,
      })
      message.success('登录成功')
      navigate('/')
    } catch (error) {
      const msg = error instanceof Error ? error.message : '登录失败'
      message.error(msg)
    }
  }

  return (
    <div className="auth-page">
      <Card className="auth-card">
        <Typography.Title level={3} style={{ marginBottom: 24 }}>
          用户登录
        </Typography.Title>
        <Form layout="vertical" onFinish={onFinish}>
          <Form.Item
            label="手机号"
            name="phone"
            rules={[
              { required: true, message: '请输入手机号' },
              { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' },
            ]}
          >
            <Input placeholder="请输入手机号" />
          </Form.Item>
          <Form.Item
            label="密码"
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password placeholder="请输入密码" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block>
              登录
            </Button>
          </Form.Item>
          <Button type="link" onClick={() => navigate('/register')} block>
            还没有账号？去注册
          </Button>
        </Form>
      </Card>
    </div>
  )
}
