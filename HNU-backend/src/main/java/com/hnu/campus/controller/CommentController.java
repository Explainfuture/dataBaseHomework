package com.hnu.campus.controller;

import com.hnu.campus.dto.comment.CommentCreateDTO;
import com.hnu.campus.dto.common.ApiResponse;
import com.hnu.campus.security.CurrentUserContext;
import com.hnu.campus.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 */
@RestController
@RequestMapping("/api/v1/comments")
@Tag(name = "评论管理", description = "评论发布、点赞等接口")
@SecurityRequirement(name = "Bearer Authentication")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @Operation(summary = "发布评论", description = "对帖子发布评论或回复评论")
    public ApiResponse<Long> createComment(@Valid @RequestBody CommentCreateDTO createDTO) {
        Long userId = CurrentUserContext.getUserId();
        Long commentId = commentService.createComment(userId, createDTO);
        return ApiResponse.success("评论成功", commentId);
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "点赞/取消点赞评论", description = "点赞或取消点赞评论")
    public ApiResponse<Boolean> toggleLike(
            @Parameter(description = "评论ID", example = "1", required = true)
            @PathVariable Long id) {
        Long userId = CurrentUserContext.getUserId();
        boolean liked = commentService.toggleLike(id, userId);
        return ApiResponse.success(liked);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评论", description = "逻辑删除评论，只有评论者本人可以删除")
    public ApiResponse<Void> deleteComment(
            @Parameter(description = "评论ID", example = "1", required = true)
            @PathVariable Long id) {
        Long userId = CurrentUserContext.getUserId();
        commentService.deleteComment(id, userId);
        return ApiResponse.success("删除成功");
    }
}
