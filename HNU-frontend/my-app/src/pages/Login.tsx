import { Button, Card, Form, Input, message, Space, Typography, App as AntApp } from 'antd'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api/client'
import type { ApiResponse } from '../api/client'
import type { LoginResponse } from '../api/types'
import { useAuth } from '../store/auth'

export default function LoginPage() {
  const navigate = useNavigate()
  const { setAuth } = useAuth()
  const { notification } = AntApp.useApp()
  const [countdown, setCountdown] = useState(0)
  const [form] = Form.useForm()

  useEffect(() => {
    if (countdown <= 0) return
    const timer = setInterval(() => {
      setCountdown((prev) => prev - 1)
    }, 1000)
    return () => clearInterval(timer)
  }, [countdown])

  const handleSendCode = async () => {
    try {
      const phone = form.getFieldValue('phone')
      if (!phone || !/^1[3-9]\d{9}$/.test(phone)) {
        message.warning('请先输入正确的手机号')
        return
      }
      await api.post('/api/v1/auth/send-verify-code', null, { params: { phone } })
      message.success('验证码已发送')
      setCountdown(60)
    } catch (error) {
      const msg = error instanceof Error ? error.message : '验证码发送失败'
      message.error(msg)
    }
  }

  const onFinish = async (values: { phone: string; password: string; verifyCode: string }) => {
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
      if (msg.includes('未通过申请')) {
        notification.warning({
          message: '无法登录',
          description: '当前帐号未通过申请，请稍等。',
          placement: 'topRight',
        })
        return
      }
      if (msg.includes('验证码') || msg.includes('密码') || msg.includes('手机号')) {
        notification.error({
          message: '登录失败',
          description: msg,
          placement: 'topRight',
        })
        return
      }
      message.error(msg)
    }
  }

  return (
    <div className="auth-page">
      <Card className="auth-card">
        <Typography.Title level={3} style={{ marginBottom: 24 }}>
          用户登录
        </Typography.Title>
        <Form form={form} layout="vertical" onFinish={onFinish}>
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
          <Form.Item label="验证码" required>
            <Space.Compact style={{ width: '100%' }}>
              <Form.Item
                name="verifyCode"
                noStyle
                rules={[{ required: true, message: '请输入验证码' }]}
              >
                <Input placeholder="请输入验证码" />
              </Form.Item>
              <Button onClick={handleSendCode} disabled={countdown > 0}>
                {countdown > 0 ? `${countdown}s` : '获取验证码'}
              </Button>
            </Space.Compact>
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
