package com.hnu.campus.service;

import com.hnu.campus.dto.comment.CommentCreateDTO;
import com.hnu.campus.dto.comment.CommentDTO;

import java.util.List;

public interface CommentService {
    Long createComment(Long userId, CommentCreateDTO createDTO);

    boolean toggleLike(Long commentId, Long userId);

    void deleteComment(Long commentId, Long userId);

    void deleteCommentAsAdmin(Long commentId, Long adminId);

    List<CommentDTO> getCommentTree(Long postId, Long currentUserId);
}
