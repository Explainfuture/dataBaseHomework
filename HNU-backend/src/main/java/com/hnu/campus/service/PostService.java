package com.hnu.campus.service;

import com.hnu.campus.dto.post.PostCreateDTO;
import com.hnu.campus.dto.post.PostDetailDTO;
import com.hnu.campus.dto.post.PostListDTO;

import java.util.List;

public interface PostService {
    Long createPost(Long userId, PostCreateDTO createDTO);

    void deletePost(Long userId, Long postId);

    List<PostListDTO> getPostList(Integer categoryId, Integer page, Integer size);

    List<PostListDTO> searchPosts(String keyword, Integer categoryId, Integer page, Integer size);

    List<PostListDTO> getHotPosts();

    PostDetailDTO getPostDetail(Long postId, Long currentUserId);

    boolean toggleLike(Long postId, Long userId);
}
