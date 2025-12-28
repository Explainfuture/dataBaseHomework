package com.hnu.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hnu.campus.dto.comment.CommentCreateDTO;
import com.hnu.campus.dto.comment.CommentDTO;
import com.hnu.campus.entity.Comment;
import com.hnu.campus.entity.CommentLike;
import com.hnu.campus.entity.Post;
import com.hnu.campus.entity.User;
import com.hnu.campus.exception.BusinessException;
import com.hnu.campus.mapper.CommentLikeMapper;
import com.hnu.campus.mapper.CommentMapper;
import com.hnu.campus.mapper.PostMapper;
import com.hnu.campus.mapper.UserMapper;
import com.hnu.campus.security.CurrentUserContext;
import com.hnu.campus.enums.UserRole;
import com.hnu.campus.service.CommentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
    private static final String DELETED_PLACEHOLDER = "该评论用户已自行删除";
    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final UserMapper userMapper;
    private final PostMapper postMapper;

    public CommentServiceImpl(CommentMapper commentMapper,
                              CommentLikeMapper commentLikeMapper,
                              UserMapper userMapper,
                              PostMapper postMapper) {
        this.commentMapper = commentMapper;
        this.commentLikeMapper = commentLikeMapper;
        this.userMapper = userMapper;
        this.postMapper = postMapper;
    }

    @Override
    public Long createComment(Long userId, CommentCreateDTO createDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (Boolean.TRUE.equals(user.getIsMuted())) {
            throw new BusinessException(403, "账号已被禁言");
        }
        Post post = postMapper.selectById(createDTO.getPostId());
        if (post == null || !"normal".equals(post.getStatus())) {
            throw new BusinessException(404, "帖子不存在");
        }
        if (createDTO.getParentId() != null) {
            Comment parent = commentMapper.selectById(createDTO.getParentId());
            if (parent == null || !"normal".equals(parent.getStatus())) {
                throw new BusinessException(404, "父评论不存在");
            }
            if (!Objects.equals(parent.getPostId(), createDTO.getPostId())) {
                throw new BusinessException(400, "父评论不属于该帖子");
            }
        }
        Comment comment = Comment.builder()
                .postId(createDTO.getPostId())
                .userId(userId)
                .content(createDTO.getContent())
                .parentId(createDTO.getParentId())
                .likeCount(0)
                .status("normal")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        commentMapper.insert(comment);
        return comment.getId();
    }

    @Override
    public boolean toggleLike(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || !"normal".equals(comment.getStatus())) {
            throw new BusinessException(404, "评论不存在");
        }
        QueryWrapper<CommentLike> wrapper = new QueryWrapper<CommentLike>()
                .eq("comment_id", commentId)
                .eq("user_id", userId);
        CommentLike existing = commentLikeMapper.selectOne(wrapper);
        int currentLike = comment.getLikeCount() == null ? 0 : comment.getLikeCount();
        if (existing != null) {
            commentLikeMapper.deleteById(existing.getId());
            int newLike = Math.max(0, currentLike - 1);
            UpdateWrapper<Comment> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", commentId)
                    .set("like_count", newLike)
                    .set("update_time", LocalDateTime.now());
            commentMapper.update(null, updateWrapper);
            return false;
        }
        CommentLike like = CommentLike.builder()
                .commentId(commentId)
                .userId(userId)
                .createTime(LocalDateTime.now())
                .build();
        commentLikeMapper.insert(like);
        int newLike = currentLike + 1;
        UpdateWrapper<Comment> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", commentId)
                .set("like_count", newLike)
                .set("update_time", LocalDateTime.now());
        commentMapper.update(null, updateWrapper);
        return true;
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || !"normal".equals(comment.getStatus())) {
            throw new BusinessException(404, "评论不存在");
        }
        if (!Objects.equals(comment.getUserId(), userId)) {
            throw new BusinessException(403, "无权限删除该评论");
        }
        UpdateWrapper<Comment> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", commentId)
                .set("status", "deleted")
                .set("content", DELETED_PLACEHOLDER)
                .set("update_time", LocalDateTime.now());
        commentMapper.update(null, updateWrapper);
    }

    @Override
    public void deleteCommentAsAdmin(Long commentId, Long adminId) {
        String role = CurrentUserContext.getRole();
        if (!UserRole.ADMIN.getCode().equals(role)) {
            throw new BusinessException(403, "需要管理员权限");
        }
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(404, "评论不存在");
        }
        UpdateWrapper<Comment> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", commentId)
                .set("status", "deleted")
                .set("content", DELETED_PLACEHOLDER)
                .set("update_time", LocalDateTime.now());
        commentMapper.update(null, updateWrapper);
    }

    @Override
    public List<CommentDTO> getCommentTree(Long postId, Long currentUserId) {
        List<Comment> comments = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId, postId)
                .orderByAsc(Comment::getCreateTime));
        if (comments.isEmpty()) {
            return List.of();
        }

        Set<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, User> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, item -> item));

        Set<Long> likedCommentIds = Collections.emptySet();
        if (currentUserId != null) {
            List<Long> commentIds = comments.stream().map(Comment::getId).toList();
            if (!commentIds.isEmpty()) {
                List<CommentLike> likes = commentLikeMapper.selectList(new QueryWrapper<CommentLike>()
                        .eq("user_id", currentUserId)
                        .in("comment_id", commentIds));
                likedCommentIds = likes.stream().map(CommentLike::getCommentId).collect(Collectors.toSet());
            }
        }

        Map<Long, CommentDTO> dtoMap = new HashMap<>();
        for (Comment comment : comments) {
            CommentDTO dto = new CommentDTO();
            dto.setId(comment.getId());
            dto.setUserId(comment.getUserId());
            User user = userMap.get(comment.getUserId());
            dto.setUserNickname(user == null ? null : user.getNickname());
            if (!"normal".equals(comment.getStatus())) {
                dto.setContent(DELETED_PLACEHOLDER);
            } else {
                dto.setContent(comment.getContent());
            }
            dto.setParentId(comment.getParentId());
            dto.setLikeCount(comment.getLikeCount());
            dto.setIsLiked(likedCommentIds.contains(comment.getId()));
            dto.setCreateTime(comment.getCreateTime());
            dto.setReplies(new ArrayList<>());
            dtoMap.put(comment.getId(), dto);
        }

        List<CommentDTO> roots = new ArrayList<>();
        for (Comment comment : comments) {
            CommentDTO dto = dtoMap.get(comment.getId());
            Long parentId = comment.getParentId();
            if (parentId == null) {
                roots.add(dto);
                continue;
            }
            CommentDTO parentDto = dtoMap.get(parentId);
            if (parentDto == null) {
                roots.add(dto);
                continue;
            }
            User parentUser = userMap.get(parentDto.getUserId());
            dto.setParentUserNickname(parentUser == null ? null : parentUser.getNickname());
            parentDto.getReplies().add(dto);
        }
        return roots;
    }
}
