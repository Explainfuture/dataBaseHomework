import { Alert, Button, Card, Select, Space, Table, Tag, message } from 'antd'
import { useEffect, useState } from 'react'
import { api } from '../api/client'
import type { ApiResponse } from '../api/client'
import type { PendingUser } from '../api/types'

export default function AdminPendingPage() {
  const [loading, setLoading] = useState(false)
  const [users, setUsers] = useState<PendingUser[]>([])
  const [mode, setMode] = useState<'pending' | 'all'>('pending')
  const [actionAlert, setActionAlert] = useState<string | null>(null)

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

  const fetchAll = async () => {
    setLoading(true)
    try {
      const res = await api.get<ApiResponse<PendingUser[]>>('/api/v1/admin/users', {
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
    if (mode === 'pending') {
      fetchPending()
    } else {
      fetchAll()
    }
  }, [mode])

  useEffect(() => {
    if (!actionAlert) return
    const timer = setTimeout(() => {
      setActionAlert(null)
    }, 1000)
    return () => clearTimeout(timer)
  }, [actionAlert])

  const handleReview = async (userId: number, authStatus: 'approved' | 'rejected') => {
    try {
      await api.post('/api/v1/admin/auth/review', { userId, authStatus })
      const msg = authStatus === 'approved' ? '审核已通过' : '审核已拒绝'
      message.success(msg)
      setActionAlert(msg)
      fetchPending()
    } catch (error) {
      const msg = error instanceof Error ? error.message : '操作失败'
      message.error(msg)
    }
  }

  const handleMute = async (userId: number, isMuted: boolean) => {
    try {
      await api.post('/api/v1/admin/users/mute', { userId, isMuted })
      const msg = isMuted ? '已禁言' : '已解禁'
      message.success(msg)
      setActionAlert(msg)
      fetchAll()
    } catch (error) {
      const msg = error instanceof Error ? error.message : '操作失败'
      message.error(msg)
    }
  }

  return (
    <Card
      title="用户管理"
      extra={
        <Select
          value={mode}
          onChange={(value) => setMode(value)}
          options={[
            { value: 'all', label: '全部用户' },
            { value: 'pending', label: '待审核用户' },
          ]}
          style={{ width: 140 }}
        />
      }
    >
      {actionAlert ? (
        <Alert
          type="success"
          showIcon
          style={{ marginBottom: 16 }}
          message={actionAlert}
        />
      ) : null}
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
                {mode === 'pending' ? (
                  <>
                    <Button type="primary" onClick={() => handleReview(record.id, 'approved')}>
                      通过
                    </Button>
                    <Button danger onClick={() => handleReview(record.id, 'rejected')}>
                      拒绝
                    </Button>
                  </>
                ) : (
                  <Button
                    danger={record.isMuted !== true}
                    onClick={() => handleMute(record.id, record.isMuted !== true)}
                  >
                    {record.isMuted ? '解禁' : '禁言'}
                  </Button>
                )}
              </Space>
            ),
          },
        ]}
      />
    </Card>
  )
}
