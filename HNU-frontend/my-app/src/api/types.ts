export type LoginResponse = {
  token: string
  expiresIn?: number
  userId: number
  nickname: string
  role: string
}

export type PostListItem = {
  id: number
  title: string
  contentSummary: string
  categoryId: number
  categoryName?: string
  authorId: number
  authorNickname?: string
  viewCount: number
  likeCount: number
  hotScore: number
  createTime: string
}

export type CommentItem = {
  id: number
  userId: number
  userNickname?: string
  content: string
  parentId?: number
  parentUserNickname?: string
  likeCount: number
  isLiked: boolean
  createTime: string
  replies?: CommentItem[]
}

export type PostDetail = {
  id: number
  title: string
  content: string
  categoryId: number
  categoryName?: string
  authorId: number
  authorNickname?: string
  contactInfo?: string
  viewCount: number
  likeCount: number
  hotScore: number
  isLiked: boolean
  createTime: string
  comments: CommentItem[]
}

export type PendingUser = {
  id: number
  phone: string
  nickname: string
  studentId?: string
  campusCardUrl?: string
  authStatus: string
  role: string
  isMuted: boolean
  createTime: string
}

export type UserInfo = {
  id: number
  phone: string
  nickname: string
  studentId?: string
  campusCardUrl?: string
  authStatus: string
  role: string
  isMuted: boolean
  createTime: string
}
