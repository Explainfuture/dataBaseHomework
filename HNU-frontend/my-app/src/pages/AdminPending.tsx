import { Button, Card, Select, Space, Table, Tag, message, App as AntApp } from 'antd'
import { useEffect, useState } from 'react'
import { api } from '../api/client'
import type { ApiResponse } from '../api/client'
import type { PendingUser } from '../api/types'
import { useAuth } from '../store/auth'

export default function AdminPendingPage() {
  const { user } = useAuth()
  const { notification } = AntApp.useApp()
  const [loading, setLoading] = useState(false)
  const [users, setUsers] = useState<PendingUser[]>([])
  const [mode, setMode] = useState<'pending' | 'all'>('all')

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


  const handleReview = async (userId: number, authStatus: 'approved' | 'rejected') => {
    try {
      await api.post('/api/v1/admin/auth/review', { userId, authStatus })

      notification.success({
        message: authStatus === 'approved'? '审核已通过' : '审核已拒绝',
        placement: 'topRight',
      })

      fetchPending()
    } catch (error) {
      notification.error({
        message: '操作失败',
        placement: 'topRight',
      })
    }
  }

  const handleMute = async (userId: number, isMuted: boolean) => {
    try {
      if (user?.userId === userId) {
        notification.warning({
          message: '操作无效',
          description: '不能禁言自己',
          placement: 'topRight',
        })
        return
      }
      await api.post('/api/v1/admin/users/mute', { userId, isMuted })
      notification.success({
        message: isMuted ? '禁言成功' : '解除成功',
        description: isMuted ? '该用户已被禁言' : '该用户已恢复发言权限',
        placement: 'topRight',
      })
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
                //将tag附上传入的连接，单独的一个查看很丑
                <a href={value} target="_blank" rel="noreferrer">
                  <Tag color="green">已提交</Tag>
                </a>
              ) : (
                //todo 将这个tag增加样式，红色表示未提交
                <Tag color="magenta">未提交</Tag>
                //<Tag>未提交</Tag>
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
