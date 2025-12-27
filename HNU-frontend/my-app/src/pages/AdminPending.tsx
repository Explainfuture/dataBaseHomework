import { Button, Card, Space, Table, Tag, message } from 'antd'
import { useEffect, useState } from 'react'
import { api } from '../api/client'
import type { ApiResponse } from '../api/client'
import type { PendingUser } from '../api/types'

export default function AdminPendingPage() {
  const [loading, setLoading] = useState(false)
  const [users, setUsers] = useState<PendingUser[]>([])

  const fetchPending = async () => {
    setLoading(true)
    try {
      const res = await api.get<ApiResponse<PendingUser[]>>('/api/v1/admin/users/pending', {
        params: { page: 1, size: 20 },
      })
      const payload = res as unknown as ApiResponse<PendingUser[]>
      setUsers(payload.data)
    } catch (error) {
      const msg = error instanceof Error ? error.message : '加载失败'
      message.error(msg)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchPending()
  }, [])

  const handleReview = async (userId: number, authStatus: 'approved' | 'rejected') => {
    try {
      await api.post('/api/v1/admin/auth/review', { userId, authStatus })
      message.success('操作成功')
      fetchPending()
    } catch (error) {
      const msg = error instanceof Error ? error.message : '操作失败'
      message.error(msg)
    }
  }

  return (
    <Card title="待审核用户">
      <Table
        rowKey="id"
        loading={loading}
        dataSource={users}
        columns={[
          { title: 'ID', dataIndex: 'id', width: 80 },
          { title: '手机号', dataIndex: 'phone' },
          { title: '昵称', dataIndex: 'nickname' },
          { title: '学号', dataIndex: 'studentId' },
          {
            title: '校园卡',
            dataIndex: 'campusCardUrl',
            render: (value: string | undefined) =>
              value ? (
                <a href={value} target="_blank" rel="noreferrer">
                  查看
                </a>
              ) : (
                <Tag>未提交</Tag>
              ),
          },
          {
            title: '注册时间',
            dataIndex: 'createTime',
            render: (value: string) => new Date(value).toLocaleString(),
          },
          {
            title: '操作',
            render: (_, record) => (
              <Space>
                <Button type="primary" onClick={() => handleReview(record.id, 'approved')}>
                  通过
                </Button>
                <Button danger onClick={() => handleReview(record.id, 'rejected')}>
                  拒绝
                </Button>
              </Space>
            ),
          },
        ]}
      />
    </Card>
  )
}
