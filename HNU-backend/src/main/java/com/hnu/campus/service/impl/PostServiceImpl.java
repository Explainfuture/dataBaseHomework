package com.hnu.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hnu.campus.dto.post.PostCreateDTO;
import com.hnu.campus.dto.post.PostDetailDTO;
import com.hnu.campus.dto.post.PostListDTO;
import com.hnu.campus.entity.Post;
import com.hnu.campus.entity.PostCategory;
import com.hnu.campus.entity.PostLike;
import com.hnu.campus.entity.User;
import com.hnu.campus.exception.BusinessException;
import com.hnu.campus.mapper.PostCategoryMapper;
import com.hnu.campus.mapper.PostLikeMapper;
import com.hnu.campus.mapper.PostMapper;
import com.hnu.campus.mapper.UserMapper;
import com.hnu.campus.service.CommentService;
import com.hnu.campus.service.PostService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {
    private static final String HOT_POST_KEY = "hot:posts";
    private static final String VIEW_KEY_PREFIX = "post:view:";

    private final PostMapper postMapper;
    private final PostCategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final PostLikeMapper postLikeMapper;
    private final CommentService commentService;
    private final StringRedisTemplate redisTemplate;

    public PostServiceImpl(PostMapper postMapper,
                           PostCategoryMapper categoryMapper,
                           UserMapper userMapper,
                           PostLikeMapper postLikeMapper,
                           CommentService commentService,
                           StringRedisTemplate redisTemplate) {
        this.postMapper = postMapper;
        this.categoryMapper = categoryMapper;
        this.userMapper = userMapper;
        this.postLikeMapper = postLikeMapper;
        this.commentService = commentService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Long createPost(Long userId, PostCreateDTO createDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (Boolean.TRUE.equals(user.getIsMuted())) {
            throw new BusinessException(403, "账号已被禁言");
        }
        if (createDTO.getTitle() == null || createDTO.getTitle().length() < 4 || createDTO.getTitle().length() > 20) {
            throw new BusinessException(400, "标题长度必须为4-20字");
        }
        PostCategory category = categoryMapper.selectById(createDTO.getCategoryId());
        if (category == null || Boolean.FALSE.equals(category.getIsActive())) {
            throw new BusinessException(400, "分类不存在或不可用");
        }
        Post post = Post.builder()
                .title(createDTO.getTitle())
                .content(createDTO.getContent())
                .categoryId(createDTO.getCategoryId())
                .authorId(userId)
                .contactInfo(createDTO.getContactInfo())
                .viewCount(0)
                .likeCount(0)
                .hotScore(BigDecimal.ZERO)
                .status("normal")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        postMapper.insert(post);
        return post.getId();
    }

    @Override
    public void deletePost(Long userId, Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || !"normal".equals(post.getStatus())) {
            throw new BusinessException(404, "帖子不存在");
        }
        if (!Objects.equals(post.getAuthorId(), userId)) {
            throw new BusinessException(403, "无权限删除该帖子");
        }
        UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", postId)
                .set("status", "deleted")
                .set("update_time", LocalDateTime.now());
        postMapper.update(null, updateWrapper);
    }

    @Override
    public List<PostListDTO> getPostList(Integer categoryId, Integer page, Integer size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, "normal")
                .orderByDesc(Post::getCreateTime);
        if (categoryId != null) {
            wrapper.eq(Post::getCategoryId, categoryId);
        }
        Page<Post> pageResult = postMapper.selectPage(new Page<>(page, size), wrapper);
        return mapToPostListDTO(pageResult.getRecords());
    }

    @Override
    public List<PostListDTO> searchPosts(String keyword, Integer categoryId, Integer page, Integer size) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<Post>()
                .eq(Post::getStatus, "normal")
                .and(query -> query.like(Post::getTitle, keyword).or().like(Post::getContent, keyword))
                .orderByDesc(Post::getCreateTime);
        if (categoryId != null) {
            wrapper.eq(Post::getCategoryId, categoryId);
        }
        Page<Post> pageResult = postMapper.selectPage(new Page<>(page, size), wrapper);
        return mapToPostListDTO(pageResult.getRecords());
    }

    @Override
    public List<PostListDTO> getHotPosts() {
        Set<String> cachedIds = redisTemplate.opsForZSet().reverseRange(HOT_POST_KEY, 0, 9);
        List<Post> posts;
        if (cachedIds == null || cachedIds.isEmpty()) {
            posts = postMapper.selectList(new LambdaQueryWrapper<Post>()
                    .eq(Post::getStatus, "normal")
                    .orderByDesc(Post::getHotScore)
                    .orderByDesc(Post::getCreateTime)
                    .last("limit 10"));
            if (!posts.isEmpty()) {
                for (Post post : posts) {
                    double score = post.getHotScore() == null ? 0 : post.getHotScore().doubleValue();
                    redisTemplate.opsForZSet().add(HOT_POST_KEY, String.valueOf(post.getId()), score);
                }
                redisTemplate.expire(HOT_POST_KEY, Duration.ofHours(1));
            }
            return mapToPostListDTO(posts.stream()
                    .filter(post -> "normal".equals(post.getStatus()))
                    .toList());
        }
        List<Long> ids = cachedIds.stream()
                .map(Long::valueOf)
                .toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        List<Post> fetched = postMapper.selectBatchIds(ids);
        Map<Long, Post> postMap = fetched.stream()
                .collect(Collectors.toMap(Post::getId, item -> item));
        posts = ids.stream()
                .map(postMap::get)
                .filter(Objects::nonNull)
                .filter(post -> "normal".equals(post.getStatus()))
                .toList();
        return mapToPostListDTO(posts);
    }

    @Override
    public PostDetailDTO getPostDetail(Long postId, Long currentUserId) {
        Post post = postMapper.selectById(postId);
        if (post == null || !"normal".equals(post.getStatus())) {
            throw new BusinessException(404, "帖子不存在");
        }
        User author = userMapper.selectById(post.getAuthorId());
        PostCategory category = categoryMapper.selectById(post.getCategoryId());

        String viewKey = VIEW_KEY_PREFIX + postId;
        Long delta = redisTemplate.opsForValue().increment(viewKey);
        int baseView = post.getViewCount() == null ? 0 : post.getViewCount();
        int viewCount = baseView + (delta == null ? 0 : delta.intValue());
        int likeCount = post.getLikeCount() == null ? 0 : post.getLikeCount();
        BigDecimal hotScore = BigDecimal.valueOf(viewCount * 0.3 + likeCount * 0.7);
        redisTemplate.opsForZSet().add(HOT_POST_KEY, String.valueOf(postId), hotScore.doubleValue());
        redisTemplate.expire(HOT_POST_KEY, Duration.ofHours(1));

        PostDetailDTO detail = new PostDetailDTO();
        detail.setId(post.getId());
        detail.setTitle(post.getTitle());
        detail.setContent(post.getContent());
        detail.setCategoryId(post.getCategoryId());
        detail.setCategoryName(category == null ? null : category.getCategoryName());
        detail.setAuthorId(post.getAuthorId());
        detail.setAuthorNickname(author == null ? null : author.getNickname());
        detail.setContactInfo(post.getContactInfo());
        detail.setViewCount(viewCount);
        detail.setLikeCount(likeCount);
        detail.setHotScore(hotScore);
        if (currentUserId != null) {
            boolean liked = postLikeMapper.selectOne(new QueryWrapper<PostLike>()
                    .eq("post_id", postId)
                    .eq("user_id", currentUserId)) != null;
            detail.setIsLiked(liked);
        } else {
            detail.setIsLiked(false);
        }
        detail.setCreateTime(post.getCreateTime());
        detail.setComments(commentService.getCommentTree(postId, currentUserId));
        return detail;
    }

    @Override
    public boolean toggleLike(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null || !"normal".equals(post.getStatus())) {
            throw new BusinessException(404, "帖子不存在");
        }
        QueryWrapper<PostLike> wrapper = new QueryWrapper<PostLike>()
                .eq("post_id", postId)
                .eq("user_id", userId);
        PostLike existing = postLikeMapper.selectOne(wrapper);
        int currentLike = post.getLikeCount() == null ? 0 : post.getLikeCount();
        int viewCount = post.getViewCount() == null ? 0 : post.getViewCount();
        String deltaText = redisTemplate.opsForValue().get(VIEW_KEY_PREFIX + postId);
        long delta = 0;
        if (deltaText != null) {
            try {
                delta = Long.parseLong(deltaText);
            } catch (NumberFormatException ignored) {
                delta = 0;
            }
        }
        int totalView = viewCount + (int) delta;
        if (existing != null) {
            postLikeMapper.deleteById(existing.getId());
            int newLike = Math.max(0, currentLike - 1);
            BigDecimal hotScore = BigDecimal.valueOf(totalView * 0.3 + newLike * 0.7);
            UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", postId)
                    .set("like_count", newLike)
                    .set("hot_score", hotScore)
                    .set("update_time", LocalDateTime.now());
            postMapper.update(null, updateWrapper);
            redisTemplate.opsForZSet().add(HOT_POST_KEY, String.valueOf(postId), hotScore.doubleValue());
            redisTemplate.expire(HOT_POST_KEY, Duration.ofHours(1));
            return false;
        }
        PostLike like = PostLike.builder()
                .postId(postId)
                .userId(userId)
                .createTime(LocalDateTime.now())
                .build();
        postLikeMapper.insert(like);
        int newLike = currentLike + 1;
        BigDecimal hotScore = BigDecimal.valueOf(totalView * 0.3 + newLike * 0.7);
        UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", postId)
                .set("like_count", newLike)
                .set("hot_score", hotScore)
                .set("update_time", LocalDateTime.now());
        postMapper.update(null, updateWrapper);
        redisTemplate.opsForZSet().add(HOT_POST_KEY, String.valueOf(postId), hotScore.doubleValue());
        redisTemplate.expire(HOT_POST_KEY, Duration.ofHours(1));
        return true;
    }

    private List<PostListDTO> mapToPostListDTO(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return List.of();
        }
        List<Integer> categoryIds = posts.stream()
                .map(Post::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Integer, PostCategory> categoryMap = categoryIds.isEmpty()
                ? Collections.emptyMap()
                : categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(PostCategory::getId, item -> item));
        List<Long> authorIds = posts.stream()
                .map(Post::getAuthorId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, User> userMap = authorIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(authorIds).stream()
                .collect(Collectors.toMap(User::getId, item -> item));
        return posts.stream()
                .map(post -> {
                    PostListDTO dto = new PostListDTO();
                    dto.setId(post.getId());
                    dto.setTitle(post.getTitle());
                    String content = post.getContent() == null ? "" : post.getContent();
                    dto.setContentSummary(content.length() > 100 ? content.substring(0, 100) : content);
                    dto.setCategoryId(post.getCategoryId());
                    PostCategory category = categoryMap.get(post.getCategoryId());
                    dto.setCategoryName(category == null ? null : category.getCategoryName());
                    dto.setAuthorId(post.getAuthorId());
                    User author = userMap.get(post.getAuthorId());
                    dto.setAuthorNickname(author == null ? null : author.getNickname());
                    dto.setViewCount(post.getViewCount());
                    dto.setLikeCount(post.getLikeCount());
                    dto.setHotScore(post.getHotScore());
                    dto.setCreateTime(post.getCreateTime());
                    return dto;
                })
                .toList();
    }
}
