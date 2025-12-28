import { Card, Col, Input, List, Pagination, Row, Select, Space, Tag, Typography, message } from 'antd'
import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'
import type { ApiResponse } from '../api/client'
import type { PostListItem } from '../api/types'
import mockPosts from '../mock/posts.json'

const categories = [
  { value: 1, label: '二手闲置' },
  { value: 2, label: '打听求助' },
  { value: 3, label: '恋爱交友' },
  { value: 4, label: '校园趣事' },
  { value: 5, label: '考试信息' },
]

export default function PostsPage() {
  const [posts, setPosts] = useState<PostListItem[]>([])
  const [hotPosts, setHotPosts] = useState<PostListItem[]>([])
  const [loading, setLoading] = useState(false)
  const [keyword, setKeyword] = useState('')
  const [categoryId, setCategoryId] = useState<number | undefined>()
  const [useMock, setUseMock] = useState(false)
  const [page, setPage] = useState(1)
  const [size, setSize] = useState(10)
  const [total, setTotal] = useState(0)

  const fetchPosts = async () => {
    setLoading(true)
    try {
      if (keyword.trim()) {
        const res = await api.get<ApiResponse<PostListItem[]>>('/api/v1/posts/search', {
          params: {
            keyword: keyword.trim(),
            categoryId,
            page,
            size,
          },
        })
        const payload = res as unknown as ApiResponse<PostListItem[]>
        const list = payload.data
        if (list.length === 0) {
          setUseMock(true)
          const mockList = filterMockPosts()
          setPosts(mockList.slice((page - 1) * size, page * size))
          setTotal(mockList.length)
        } else {
          setUseMock(false)
          setPosts(list)
          setTotal(page * size + (list.length < size ? 0 : size))
        }
      } else {
        const res = await api.get<ApiResponse<PostListItem[]>>('/api/v1/posts', {
          params: { categoryId, page, size },
        })
        const payload = res as unknown as ApiResponse<PostListItem[]>
        const list = payload.data
        if (list.length === 0) {
          setUseMock(true)
          const mockList = filterMockPosts()
          setPosts(mockList.slice((page - 1) * size, page * size))
          setTotal(mockList.length)
        } else {
          setUseMock(false)
          setPosts(list)
          setTotal(page * size + (list.length < size ? 0 : size))
        }
      }
    } catch (error) {
      const msg = error instanceof Error ? error.message : '加载失败'
      message.error(msg)
      setUseMock(true)
      const mockList = filterMockPosts()
      setPosts(mockList.slice((page - 1) * size, page * size))
      setTotal(mockList.length)
    } finally {
      setLoading(false)
    }
  }

  const fetchHotPosts = async () => {
    try {
      const res = await api.get<ApiResponse<PostListItem[]>>('/api/v1/posts/hot')
      const payload = res as unknown as ApiResponse<PostListItem[]>
      if (payload.data.length === 0) {
        setHotPosts(mockPosts.slice(0, 10))
      } else {
        setHotPosts(payload.data)
      }
    } catch (error) {
      const msg = error instanceof Error ? error.message : '热搜加载失败'
      message.error(msg)
      setHotPosts(mockPosts.slice(0, 10))
    }
  }

  useEffect(() => {
    fetchPosts()
  }, [categoryId, page, size])

  useEffect(() => {
    fetchHotPosts()
  }, [])

  const categoryTag = useMemo(() => {
    if (!categoryId) return null
    const category = categories.find((item) => item.value === categoryId)
    return category ? category.label : null
  }, [categoryId])

  const filterMockPosts = () => {
    const text = keyword.trim()
    let list = mockPosts as PostListItem[]
    if (categoryId) {
      list = list.filter((item) => item.categoryId === categoryId)
    }
    if (text) {
      list = list.filter(
        (item) =>
          item.title.includes(text) || item.contentSummary.includes(text),
      )
    }
    return list
  }

  return (
    <Row gutter={24}>
      <Col xs={24} lg={16}>
        <Card>
          <Space style={{ marginBottom: 16 }} wrap>
            <Select
              allowClear
              placeholder="选择分类"
              options={categories}
              style={{ width: 180 }}
              value={categoryId}
              onChange={(value) => setCategoryId(value)}
            />
            <Input.Search
              placeholder="搜索帖子标题或内容"
              allowClear
              onSearch={() => {
                setPage(1)
                fetchPosts()
              }}
              value={keyword}
              onChange={(event) => setKeyword(event.target.value)}
              style={{ width: 260 }}
            />
            {categoryTag ? <Tag color="blue">{categoryTag}</Tag> : null}
            {useMock ? <Tag color="orange">当前展示模拟数据</Tag> : null}
          </Space>
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
                  </Space>
                }
              >
                <List.Item.Meta
                  title={<Link to={`/posts/${item.id}`}>{item.title}</Link>}
                  description={
                    <Space wrap>
                      <span>作者：{item.authorNickname || '匿名'}</span>
                      <span>分类：{item.categoryName || '未分类'}</span>
                      <span>发布时间：{new Date(item.createTime).toLocaleString()}</span>
                    </Space>
                  }
                />
                <Typography.Paragraph>{item.contentSummary}</Typography.Paragraph>
              </List.Item>
            )}
          />
          <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 16 }}>
            <Pagination
              current={page}
              pageSize={size}
              total={total}
              showSizeChanger
              pageSizeOptions={[5, 10, 20]}
              onChange={(nextPage, nextSize) => {
                setPage(nextPage)
                setSize(nextSize)
              }}
            />
          </div>
        </Card>
      </Col>
      <Col xs={24} lg={8}>
        <Card title="热搜榜">
          <List
            dataSource={hotPosts}
            renderItem={(item, index) => (
              <List.Item key={item.id}>
                <Space>
                  <Tag color={index < 3 ? 'volcano' : 'blue'}>{index + 1}</Tag>
                  <Link to={`/posts/${item.id}`}>{item.title}</Link>
                </Space>
              </List.Item>
            )}
          />
        </Card>
      </Col>
    </Row>
  )
}
