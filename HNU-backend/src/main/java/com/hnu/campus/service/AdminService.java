package com.hnu.campus.service;

import com.hnu.campus.dto.admin.AuthReviewDTO;
import com.hnu.campus.dto.admin.UserMuteDTO;
import com.hnu.campus.dto.user.UserInfoDTO;

import java.util.List;

public interface AdminService {
    void reviewAuth(Long adminId, AuthReviewDTO reviewDTO);

    void forceDeletePost(Long adminId, Long postId);

    void muteUser(Long adminId, UserMuteDTO muteDTO);

    List<UserInfoDTO> getPendingUsers(Long adminId, Integer page, Integer size);
}
