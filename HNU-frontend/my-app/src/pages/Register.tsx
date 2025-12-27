import { Button, Card, Form, Input, message, Space, Typography } from 'antd'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../api/client'

export default function RegisterPage() {
  const navigate = useNavigate()
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

  const onFinish = async (values: {
    phone: string
    verifyCode: string
    nickname: string
    password: string
    studentId?: string
    campusCardUrl?: string
  }) => {
    try {
      await api.post('/api/v1/auth/register', values)
      message.success('注册成功，等待管理员审核')
      navigate('/login')
    } catch (error) {
      const msg = error instanceof Error ? error.message : '注册失败'
      message.error(msg)
    }
  }

  return (
    <div className="auth-page">
      <Card className="auth-card">
        <Typography.Title level={3} style={{ marginBottom: 24 }}>
          用户注册
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
                rules={[
                  { required: true, message: '请输入验证码' },
                  { min: 4, max: 6, message: '验证码长度为4-6位' },
                ]}
              >
                <Input placeholder="请输入验证码" />
              </Form.Item>
              <Button onClick={handleSendCode} disabled={countdown > 0}>
                {countdown > 0 ? `${countdown}s` : '获取验证码'}
              </Button>
            </Space.Compact>
          </Form.Item>
          <Form.Item
            label="昵称"
            name="nickname"
            rules={[{ required: true, message: '请输入昵称' }]}
          >
            <Input placeholder="请输入昵称" />
          </Form.Item>
          <Form.Item
            label="密码"
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password placeholder="请输入密码" />
          </Form.Item>
          <Form.Item label="学号" name="studentId">
            <Input placeholder="选填" />
          </Form.Item>
          <Form.Item label="校园卡照片 URL" name="campusCardUrl">
            <Input placeholder="选填" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block>
              提交注册
            </Button>
          </Form.Item>
          <Button type="link" onClick={() => navigate('/login')} block>
            已有账号？去登录
          </Button>
        </Form>
      </Card>
    </div>
  )
}
