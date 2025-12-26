package com.hnu.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hnu.campus.dto.post.PostListDTO;
import com.hnu.campus.dto.user.UserInfoDTO;
import com.hnu.campus.dto.user.UserUpdateDTO;
import com.hnu.campus.entity.Post;
import com.hnu.campus.entity.PostCategory;
import com.hnu.campus.entity.User;
import com.hnu.campus.exception.BusinessException;
import com.hnu.campus.mapper.PostCategoryMapper;
import com.hnu.campus.mapper.PostMapper;
import com.hnu.campus.mapper.UserMapper;
import com.hnu.campus.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final PostCategoryMapper categoryMapper;

    public UserServiceImpl(UserMapper userMapper, PostMapper postMapper, PostCategoryMapper categoryMapper) {
        this.userMapper = userMapper;
        this.postMapper = postMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public UserInfoDTO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        UserInfoDTO dto = new UserInfoDTO();
        dto.setId(user.getId());
        dto.setPhone(user.getPhone());
        dto.setNickname(user.getNickname());
        dto.setStudentId(user.getStudentId());
        dto.setCampusCardUrl(user.getCampusCardUrl());
        dto.setAuthStatus(user.getAuthStatus());
        dto.setRole(user.getRole());
        dto.setIsMuted(user.getIsMuted());
        dto.setCreateTime(user.getCreateTime());
        return dto;
    }

    @Override
    public void updateUserInfo(Long userId, UserUpdateDTO updateDTO) {
        User existing = userMapper.selectById(userId);
        if (existing == null) {
            throw new BusinessException(404, "用户不存在");
        }
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId);
        boolean changed = false;
        if (updateDTO.getNickname() != null) {
            updateWrapper.set("nickname", updateDTO.getNickname());
            changed = true;
        }
        if (updateDTO.getStudentId() != null) {
            updateWrapper.set("student_id", updateDTO.getStudentId());
            changed = true;
        }
        if (updateDTO.getCampusCardUrl() != null) {
            updateWrapper.set("campus_card_url", updateDTO.getCampusCardUrl());
            changed = true;
        }
        if (changed) {
            updateWrapper.set("update_time", LocalDateTime.now());
            userMapper.update(null, updateWrapper);
        }
    }

    @Override
    public List<PostListDTO> getUserPosts(Long userId, Integer page, Integer size) {
        Page<Post> pageResult = postMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getAuthorId, userId)
                        .eq(Post::getStatus, "normal")
                        .orderByDesc(Post::getCreateTime)
        );
        List<Post> posts = pageResult.getRecords();
        if (posts.isEmpty()) {
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
        User author = userMapper.selectById(userId);
        String authorNickname = author == null ? "" : author.getNickname();
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
                    dto.setAuthorNickname(authorNickname);
                    dto.setViewCount(post.getViewCount());
                    dto.setLikeCount(post.getLikeCount());
                    dto.setHotScore(post.getHotScore());
                    dto.setCreateTime(post.getCreateTime());
                    return dto;
                })
                .toList();
    }
}
