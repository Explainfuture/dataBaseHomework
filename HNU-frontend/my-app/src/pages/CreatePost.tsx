import { Button, Card, Form, Input, Select, message, App as AntApp } from 'antd'
import { useNavigate } from 'react-router-dom'
import { api } from '../api/client'
import type { ApiResponse } from '../api/client'
import { useAuth } from '../store/auth'

const categories = [
  { value: 1, label: '二手闲置' },
  { value: 2, label: '打听求助' },
  { value: 3, label: '恋爱交友' },
  { value: 4, label: '校园趣事' },
  { value: 5, label: '考试信息' },
]

export default function CreatePostPage() {
  const { notification } = AntApp.useApp()
  const navigate = useNavigate()
  const [form] = Form.useForm()
  const { token, user } = useAuth()

  const onFinish = async (values: {
    title: string
    content: string
    categoryId: number
    contactInfo?: string
  }) => {
    if (!token) {
      message.warning('请先登录')
      return
    }
    try {
      const res = await api.post<ApiResponse<number>>('/api/v1/posts', values)
      const payload = res as unknown as ApiResponse<number>
      message.success('发布成功')
      navigate(`/posts/${payload.data}`)
    } catch (error) {
      const errorMsg = error instanceof Error ? error.message : '发布失败'
      
      if (errorMsg.includes('禁言')) {
        notification.error({
          message: '无法发布帖子',
          description: '你已被禁言，请联系管理员处理',
          placement: 'topRight',
        })
      } else {
        message.error(errorMsg)
      }
    }
  }

  return (
    <Card title="发布新帖">
      <Form form={form} layout="vertical" onFinish={onFinish}>
        <Form.Item
          label="标题"
          name="title"
          rules={[
            { required: true, message: '请输入标题' },
            { min: 4, max: 20, message: '标题长度为4-20字' },
          ]}
        >
          <Input placeholder="请输入标题" />
        </Form.Item>
        <Form.Item
          label="分类"
          name="categoryId"
          rules={[{ required: true, message: '请选择分类' }]}
        >
          <Select placeholder="请选择分类" options={categories} />
        </Form.Item>
        <Form.Item
          label="内容"
          name="content"
          rules={[{ required: true, message: '请输入内容' }]}
        >
          <Input.TextArea rows={6} placeholder="写下你的分享或需求..." />
        </Form.Item>
        <Form.Item label="联系方式" name="contactInfo">
          <Input placeholder="选填：微信/电话等" />
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit">
            发布
          </Button>
        </Form.Item>
      </Form>
    </Card>
  )
}
