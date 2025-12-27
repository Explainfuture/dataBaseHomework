import {
  Button,
  Card,
  Divider,
  Form,
  Input,
  List,
  Space,
  Tag,
  Typography,
  message,
} from 'antd'
import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { api } from '../api/client'
import type { ApiResponse } from '../api/client'
import type { CommentItem, PostDetail } from '../api/types'
import mockPosts from '../mock/posts.json'
import mockComments from '../mock/comments.json'
import { useAuth } from '../store/auth'

export default function PostDetailPage() {
  const { id } = useParams()
  const { token } = useAuth()
  const [detail, setDetail] = useState<PostDetail | null>(null)
  const [loading, setLoading] = useState(false)
  const [replyTo, setReplyTo] = useState<CommentItem | null>(null)
  const [form] = Form.useForm()
  const [useMock, setUseMock] = useState(false)

  const buildMockDetail = (postId: number): PostDetail | null => {
    const post = (mockPosts as PostDetail[]).find((item) => item.id === postId)
    if (!post) {
      return null
    }
    const comments = (mockComments as Record<string, CommentItem[]>)[
      String(postId)
    ] || []
    return {
      ...post,
      content:
        (post as unknown as { content?: string }).content ||
        post.contentSummary ||
        '暂无内容',
      isLiked: false,
      comments,
    }
  }

  const fetchDetail = async () => {
    if (!id) return
    setLoading(true)
    try {
      const res = await api.get<ApiResponse<PostDetail>>(`/api/v1/posts/${id}`)
      const payload = res as unknown as ApiResponse<PostDetail>
      if (payload.data) {
        setUseMock(false)
        setDetail(payload.data)
      } else {
        const mockDetail = buildMockDetail(Number(id))
        if (mockDetail) {
          setUseMock(true)
          setDetail(mockDetail)
        }
      }
    } catch (error) {
      const mockDetail = buildMockDetail(Number(id))
      if (mockDetail) {
        setUseMock(true)
        setDetail(mockDetail)
      } else {
        const msg = error instanceof Error ? error.message : '加载失败'
        message.error(msg)
      }
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchDetail()
  }, [id])

  const handleToggleLike = async () => {
    if (!id) return
    if (!token) {
      message.warning('请先登录')
      return
    }
    try {
      if (useMock && detail) {
        const nextLiked = !detail.isLiked
        setDetail({
          ...detail,
          isLiked: nextLiked,
          likeCount: nextLiked ? detail.likeCount + 1 : Math.max(0, detail.likeCount - 1),
        })
      } else {
        await api.post(`/api/v1/posts/${id}/like`)
        await fetchDetail()
      }
    } catch (error) {
      const msg = error instanceof Error ? error.message : '操作失败'
      message.error(msg)
    }
  }

  const handleCommentLike = async (commentId: number) => {
    if (!token) {
      message.warning('请先登录')
      return
    }
    try {
      if (useMock && detail) {
        const updateComments = (items: CommentItem[]): CommentItem[] =>
          items.map((item) => {
            if (item.id === commentId) {
              const nextLiked = !item.isLiked
              return {
                ...item,
                isLiked: nextLiked,
                likeCount: nextLiked ? item.likeCount + 1 : Math.max(0, item.likeCount - 1),
              }
            }
            if (item.replies && item.replies.length > 0) {
              return { ...item, replies: updateComments(item.replies) }
            }
            return item
          })
        setDetail({ ...detail, comments: updateComments(detail.comments) })
      } else {
        await api.post(`/api/v1/comments/${commentId}/like`)
        await fetchDetail()
      }
    } catch (error) {
      const msg = error instanceof Error ? error.message : '操作失败'
      message.error(msg)
    }
  }

  const handleSubmitComment = async (values: { content: string }) => {
    if (!id) return
    if (!token) {
      message.warning('请先登录')
      return
    }
    try {
      if (useMock && detail) {
        const now = new Date().toISOString()
        const newComment: CommentItem = {
          id: Date.now(),
          userId: 999,
          userNickname: '测试用户',
          content: values.content,
          parentId: replyTo?.id,
          parentUserNickname: replyTo?.userNickname,
          likeCount: 0,
          isLiked: false,
          createTime: now,
          replies: [],
        }
        if (replyTo) {
          const addReply = (items: CommentItem[]): CommentItem[] =>
            items.map((item) => {
              if (item.id === replyTo.id) {
                return { ...item, replies: [...(item.replies || []), newComment] }
              }
              if (item.replies && item.replies.length > 0) {
                return { ...item, replies: addReply(item.replies) }
              }
              return item
            })
          setDetail({ ...detail, comments: addReply(detail.comments) })
        } else {
          setDetail({ ...detail, comments: [newComment, ...detail.comments] })
        }
        message.success('评论成功（模拟数据）')
        setReplyTo(null)
        form.resetFields()
      } else {
        await api.post('/api/v1/comments', {
          postId: Number(id),
          content: values.content,
          parentId: replyTo?.id,
        })
        message.success('评论成功')
        setReplyTo(null)
        form.resetFields()
        await fetchDetail()
      }
    } catch (error) {
      const msg = error instanceof Error ? error.message : '评论失败'
      message.error(msg)
    }
  }

  const renderComment = (comment: CommentItem, depth = 0) => {
    return (
      <div key={comment.id} style={{ marginLeft: depth * 24, marginBottom: 16 }}>
        <Card size="small" style={{ background: depth === 0 ? '#fff' : '#fafafa' }}>
          <Space direction="vertical" style={{ width: '100%' }}>
            <Space>
              <strong>{comment.userNickname || '匿名'}</strong>
              {comment.parentUserNickname && (
                <span style={{ color: '#888' }}>回复 {comment.parentUserNickname}</span>
              )}
              <span style={{ color: '#888' }}>
                {new Date(comment.createTime).toLocaleString()}
              </span>
            </Space>
            <div>{comment.content}</div>
            <Space>
              <Button size="small" onClick={() => handleCommentLike(comment.id)}>
                {comment.isLiked ? '已赞' : '点赞'} {comment.likeCount}
              </Button>
              <Button size="small" type="link" onClick={() => setReplyTo(comment)}>
                回复
              </Button>
            </Space>
          </Space>
        </Card>
        {comment.replies?.map((child) => renderComment(child, depth + 1))}
      </div>
    )
  }

  if (!detail) {
    return <Card loading={loading}>加载中...</Card>
  }

  return (
    <Space direction="vertical" size="large" style={{ width: '100%' }}>
      <Card loading={loading}>
        <Space direction="vertical" size="middle" style={{ width: '100%' }}>
          <Typography.Title level={3}>{detail.title}</Typography.Title>
          <Space wrap>
            <span>作者：{detail.authorNickname || '匿名'}</span>
            <span>分类：{detail.categoryName || '未分类'}</span>
            <span>发布时间：{new Date(detail.createTime).toLocaleString()}</span>
            {detail.contactInfo ? <Tag color="blue">联系方式：{detail.contactInfo}</Tag> : null}
            {useMock ? <Tag color="orange">模拟数据</Tag> : null}
          </Space>
          <Typography.Paragraph>{detail.content}</Typography.Paragraph>
          <Space>
            <Button type={detail.isLiked ? 'primary' : 'default'} onClick={handleToggleLike}>
              {detail.isLiked ? '已点赞' : '点赞'} {detail.likeCount}
            </Button>
            <Tag color="geekblue">浏览 {detail.viewCount}</Tag>
          </Space>
        </Space>
      </Card>

      <Card title="发表评论">
        {replyTo && (
          <Space style={{ marginBottom: 12 }}>
            <Tag color="blue">回复 {replyTo.userNickname || '匿名'}</Tag>
            <Button size="small" onClick={() => setReplyTo(null)}>
              取消回复
            </Button>
          </Space>
        )}
        <Form form={form} layout="vertical" onFinish={handleSubmitComment}>
          <Form.Item
            name="content"
            rules={[{ required: true, message: '请输入评论内容' }]}
          >
            <Input.TextArea rows={4} placeholder="写下你的评论..." />
          </Form.Item>
          <Button type="primary" htmlType="submit">
            提交评论
          </Button>
        </Form>
      </Card>

      <Card title="评论列表">
        <List
          dataSource={detail.comments}
          renderItem={(item) => <List.Item key={item.id}>{renderComment(item)}</List.Item>}
          locale={{ emptyText: '暂无评论' }}
        />
      </Card>
      <Divider />
    </Space>
  )
}
