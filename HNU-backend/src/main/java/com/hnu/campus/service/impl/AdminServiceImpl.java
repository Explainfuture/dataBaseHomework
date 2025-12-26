package com.hnu.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hnu.campus.dto.admin.AuthReviewDTO;
import com.hnu.campus.dto.admin.UserMuteDTO;
import com.hnu.campus.dto.user.UserInfoDTO;
import com.hnu.campus.entity.Post;
import com.hnu.campus.entity.User;
import com.hnu.campus.enums.AuthStatus;
import com.hnu.campus.enums.UserRole;
import com.hnu.campus.exception.BusinessException;
import com.hnu.campus.mapper.PostMapper;
import com.hnu.campus.mapper.UserMapper;
import com.hnu.campus.security.CurrentUserContext;
import com.hnu.campus.service.AdminService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    private final UserMapper userMapper;
    private final PostMapper postMapper;

    public AdminServiceImpl(UserMapper userMapper, PostMapper postMapper) {
        this.userMapper = userMapper;
        this.postMapper = postMapper;
    }

    @Override
    public void reviewAuth(Long adminId, AuthReviewDTO reviewDTO) {
        ensureAdmin();
        AuthStatus status;
        try {
            status = AuthStatus.fromCode(reviewDTO.getAuthStatus());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(400, "审核状态不合法");
        }
        if (status == AuthStatus.PENDING) {
            throw new BusinessException(400, "审核状态不能为pending");
        }
        User user = userMapper.selectById(reviewDTO.getUserId());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setAuthStatus(status.getCode());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void forceDeletePost(Long adminId, Long postId) {
        ensureAdmin();
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }
        post.setStatus("deleted");
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);
    }

    @Override
    public void muteUser(Long adminId, UserMuteDTO muteDTO) {
        ensureAdmin();
        User user = userMapper.selectById(muteDTO.getUserId());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setIsMuted(muteDTO.getIsMuted());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public List<UserInfoDTO> getPendingUsers(Long adminId, Integer page, Integer size) {
        ensureAdmin();
        Page<User> pageResult = userMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<User>().eq(User::getAuthStatus, AuthStatus.PENDING.getCode())
                        .orderByAsc(User::getCreateTime)
        );
        return pageResult.getRecords().stream().map(user -> {
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
        }).toList();
    }

    private void ensureAdmin() {
        String role = CurrentUserContext.getRole();
        if (!UserRole.ADMIN.getCode().equals(role)) {
            throw new BusinessException(403, "需要管理员权限");
        }
    }
}
