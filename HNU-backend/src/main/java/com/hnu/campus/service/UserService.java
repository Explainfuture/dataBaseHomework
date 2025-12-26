package com.hnu.campus.service;

import com.hnu.campus.dto.post.PostListDTO;
import com.hnu.campus.dto.user.UserInfoDTO;
import com.hnu.campus.dto.user.UserUpdateDTO;

import java.util.List;

public interface UserService {
    UserInfoDTO getUserInfo(Long userId);

    void updateUserInfo(Long userId, UserUpdateDTO updateDTO);

    List<PostListDTO> getUserPosts(Long userId, Integer page, Integer size);
}
