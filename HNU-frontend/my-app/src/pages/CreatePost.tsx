import { Button, Card, Col, Divider, Form, Input, Row, Select, Typography, message } from 'antd'
import { useNavigate } from 'react-router-dom'
import { api } from '../api/client'
import type { ApiResponse } from '../api/client'

const categories = [
  { value: 1, label: '二手闲置' },
  { value: 2, label: '打听求助' },
  { value: 3, label: '恋爱交友' },
  { value: 4, label: '校园趣事' },
  { value: 5, label: '考试信息' },
]

export default function CreatePostPage() {
  const navigate = useNavigate()
  const [form] = Form.useForm()

  const onFinish = async (values: {
    title: string
    content: string
    categoryId: number
    contactInfo?: string
  }) => {
    try {
      const res = await api.post<ApiResponse<number>>('/api/v1/posts', values)
      const payload = res as unknown as ApiResponse<number>
      message.success('发布成功')
      navigate(`/posts/${payload.data}`)
    } catch (error) {
      const msg = error instanceof Error ? error.message : '发布失败'
      message.error(msg)
    }
  }

  return (
    <Card
      title="发布新帖"
      style={{ maxWidth: 880, margin: '24px auto' }}
    >
      <div style={{ maxWidth: 680, margin: '0 auto' }}>
        <Form form={form} layout="vertical" onFinish={onFinish} requiredMark="optional">
          <Typography.Title level={5} style={{ margin: 0 }}>
            基本信息
          </Typography.Title>
          <Row gutter={16}>
            <Col xs={24} md={14}>
              <Form.Item
                //label="标题"
                name="title"
                style={{marginTop: 20}}
                rules={[
                  { required: true, message: '请输入标题' },
                  { min: 4, max: 20, message: '标题长度为4-20字' },
                ]}
              >
                <Input placeholder="请输入标题" />
              </Form.Item>
            </Col>
            <Col xs={24} md={10}>
              <Form.Item
                //label="分类"
                style={{marginTop: 20}}
                name="categoryId"
                rules={[{ required: true, message: '请选择分类' }]}
              >
                <Select placeholder="请选择分类" options={categories} />
              </Form.Item>
            </Col>
          </Row>

          <Divider style={{ margin: '8px 0 16px' }} />

          <Typography.Title level={5} style={{ margin: 0 }}>
          内容
          </Typography.Title>
          <Form.Item
            //label="内容"
            name="content"
            style={{marginTop:20}}
            rules={[{ required: true, message: '请输入内容' }]}
          >
            <Input.TextArea rows={8} placeholder="写下你的分享或需求..." />
          </Form.Item>

          <Divider style={{ margin: '8px 0 16px' }} />

          <Typography.Title level={5} style={{ margin: 0 }}>
            联系方式
          </Typography.Title>
          <Form.Item 
            //label="联系方式" 
            style={{marginTop: 20}}
            name="contactInfo">
            <Input placeholder="选填：微信/电话等" />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0 }}>
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12 }}>
              <Button type="primary" htmlType="submit">
                发布
              </Button>
              <Button htmlType='button' onClick={()=>form.resetFields()}>
                重置
              </Button>
            </div>
          </Form.Item>
        </Form>
      </div>
    </Card>
  )
}