import { Button, Card, Descriptions, Form, Input, Space, Tag, message, App as AntApp } from 'antd'
import { useEffect, useState } from 'react'
import { api } from '../api/client'
import type { ApiResponse } from '../api/client'
import type { UserInfo } from '../api/types'

export default function ProfilePage() {
  const { notification } = AntApp.useApp()
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null)
  const [loading, setLoading] = useState(false)
  const [form] = Form.useForm()
  const [pwdForm] = Form.useForm()

  const fetchProfile = async () => {
    setLoading(true)
    try {
      const res = await api.get<ApiResponse<UserInfo>>('/api/v1/users/me')
      const payload = res as unknown as ApiResponse<UserInfo>
      setUserInfo(payload.data)
      form.setFieldsValue({
        nickname: payload.data.nickname,
        studentId: payload.data.studentId,
        campusCardUrl: payload.data.campusCardUrl,
      })
    } catch (error) {
      const msg = error instanceof Error ? error.message : '加载失败'
      message.error(msg)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchProfile()
  }, [])

  const onFinish = async (values: {
    nickname?: string
    studentId?: string
    campusCardUrl?: string
  }) => {
    try {
      await api.put('/api/v1/users/me', values)
      message.success('资料已更新')
      fetchProfile()
    } catch (error) {
      const msg = error instanceof Error ? error.message : '更新失败'
      message.error(msg)
    }
  }

  const onChangePassword = async (values: {
    oldPassword: string
    newPassword: string
    confirmPassword: string
  }) => {
    try {
      await api.put('/api/v1/users/me/password', values)
      notification.success({
        message: '修改成功',
        description: '密码已更新',
        placement: 'topRight',
      })
      pwdForm.resetFields()
    } catch (error) {
      const msg = error instanceof Error ? error.message : '修改失败'
      if (msg.includes('原密码')) {
        notification.error({
          message: '修改失败',
          description: '原密码错误',
          placement: 'topRight',
        })
      } else {
        message.error(msg)
      }
    }
  }

  return (
    <div style={{ maxWidth: 880, margin: '24px auto', padding: '0 16px' }}>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Card title="个人信息" loading={loading}>
          {userInfo ? (
            <Descriptions column={2} bordered size="small">
              <Descriptions.Item label="手机号">{userInfo.phone}</Descriptions.Item>
              <Descriptions.Item label="昵称">{userInfo.nickname}</Descriptions.Item>
              <Descriptions.Item label="学号">
                {userInfo.studentId || <Tag>未填写</Tag>}
              </Descriptions.Item>
              <Descriptions.Item label="认证状态">{userInfo.authStatus}</Descriptions.Item>
              <Descriptions.Item label="角色">{userInfo.role}</Descriptions.Item>
              <Descriptions.Item label="是否禁言">
                {userInfo.isMuted ? <Tag color="red">是</Tag> : <Tag color="green">否</Tag>}
              </Descriptions.Item>
            </Descriptions>
          ) : null}
        </Card>

        <Card title="编辑资料">
          <Form layout="vertical" form={form} onFinish={onFinish}>
            <Form.Item
              label="昵称"
              name="nickname"
              rules={[{ min: 2, max: 50, message: '昵称长度为2-50字' }]}
            >
              <Input placeholder="请输入昵称" />
            </Form.Item>
            <Form.Item label="学号" name="studentId">
              <Input placeholder="选填" />
            </Form.Item>
            <Form.Item label="校园卡照片 URL" name="campusCardUrl">
              <Input placeholder="选填" />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit">
                保存
              </Button>
            </Form.Item>
          </Form>
        </Card>

        <Card title="修改密码">
          <Form layout="vertical" form={pwdForm} onFinish={onChangePassword}>
            <Form.Item
              label="原密码"
              name="oldPassword"
              rules={[{ required: true, message: '请输入原密码' }]}
            >
              <Input.Password placeholder="请输入原密码" />
            </Form.Item>
            <Form.Item
              label="新密码"
              name="newPassword"
              rules={[{ required: true, message: '请输入新密码' }]}
            >
              <Input.Password placeholder="请输入新密码" />
            </Form.Item>
            <Form.Item
              label="确认新密码"
              name="confirmPassword"
              dependencies={['newPassword']}
              rules={[
                { required: true, message: '请再次输入新密码' },
                ({ getFieldValue }) => ({
                  validator(_, value) {
                    if (!value || getFieldValue('newPassword') === value) {
                      return Promise.resolve()
                    }
                    return Promise.reject(new Error('两次新密码不一致'))
                  },
                }),
              ]}
            >
              <Input.Password placeholder="请再次输入新密码" />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit">
                修改密码
              </Button>
            </Form.Item>
          </Form>
        </Card>
      </Space>
    </div>
  )
}
