import { Card, List, Popconfirm, Space, Tag, Typography, message } from 'antd'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'
import type { ApiResponse } from '../api/client'
import type { PostListItem } from '../api/types'

export default function MyPostsPage() {
  const [posts, setPosts] = useState<PostListItem[]>([])
  const [loading, setLoading] = useState(false)

  const fetchMyPosts = async () => {
    setLoading(true)
    try {
      const res = await api.get<ApiResponse<PostListItem[]>>('/api/v1/users/me/posts', {
        params: { page: 1, size: 20 },
      })
      const payload = res as unknown as ApiResponse<PostListItem[]>
      setPosts(payload.data)
    } catch (error) {
      const msg = error instanceof Error ? error.message : '加载失败'
      message.error(msg)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchMyPosts()
  }, [])

  const handleDelete = async (postId: number) => {
    try {
      await api.delete(`/api/v1/posts/${postId}`)
      message.success('删除成功')
      fetchMyPosts()
    } catch (error) {
      const msg = error instanceof Error ? error.message : '删除失败'
      message.error(msg)
    }
  }

  return (
    <Card title="我的帖子">
      <List
        itemLayout="vertical"
        dataSource={posts}
        loading={loading}
        renderItem={(item) => (
          <List.Item
            key={item.id}
            extra={
              <Space>
                <span>浏览 {item.viewCount}</span>
                <span>点赞 {item.likeCount}</span>
                <Popconfirm
                  title="确认删除该帖子？"
                  okText="删除"
                  cancelText="取消"
                  onConfirm={() => handleDelete(item.id)}
                >
                  <a>删除</a>
                </Popconfirm>
              </Space>
            }
          >
            <List.Item.Meta
              title={<Link to={`/posts/${item.id}`}>{item.title}</Link>}
              description={
                <Space wrap>
                  <span>分类：{item.categoryName || '未分类'}</span>
                  <span>发布时间：{new Date(item.createTime).toLocaleString()}</span>
                </Space>
              }
            />
            <Typography.Paragraph>{item.contentSummary}</Typography.Paragraph>
            {item.categoryName ? <Tag color="blue">{item.categoryName}</Tag> : null}
          </List.Item>
        )}
      />
    </Card>
  )
}
