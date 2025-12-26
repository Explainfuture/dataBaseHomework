package com.hnu.campus.controller;

import com.hnu.campus.dto.common.ApiResponse;
import com.hnu.campus.dto.post.PostCreateDTO;
import com.hnu.campus.dto.post.PostDetailDTO;
import com.hnu.campus.dto.post.PostListDTO;
import com.hnu.campus.security.CurrentUserContext;
import com.hnu.campus.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 帖子控制器
 */
@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "帖子管理", description = "帖子发布、查询、搜索等接口")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @Operation(summary = "发布帖子", description = "发布新帖子，标题限制4-20字，分类必须合法")
    @SecurityRequirement(name = "Bearer Authentication")
    public ApiResponse<Long> createPost(@Valid @RequestBody PostCreateDTO createDTO) {
        Long userId = CurrentUserContext.getUserId();
        Long postId = postService.createPost(userId, createDTO);
        return ApiResponse.success("发布成功", postId);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除帖子", description = "逻辑删除帖子，只有作者本人可以删除")
    @SecurityRequirement(name = "Bearer Authentication")
    public ApiResponse<Void> deletePost(
            @Parameter(description = "帖子ID", example = "1", required = true)
            @PathVariable Long id) {
        Long userId = CurrentUserContext.getUserId();
        postService.deletePost(userId, id);
        return ApiResponse.success("删除成功");
    }

    @GetMapping
    @Operation(summary = "获取帖子列表", description = "获取帖子列表，支持分页和按分类筛选")
    public ApiResponse<List<PostListDTO>> getPostList(
            @Parameter(description = "分类ID", example = "1")
            @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10")
            @RequestParam(defaultValue = "10") Integer size) {
        return ApiResponse.success(postService.getPostList(categoryId, page, size));
    }

    @GetMapping("/search")
    @Operation(summary = "搜索帖子", description = "支持模糊搜索（SQL LIKE实现），可预留ES接口")
    public ApiResponse<List<PostListDTO>> searchPosts(
            @Parameter(description = "搜索关键词", example = "自行车", required = true)
            @RequestParam String keyword,
            @Parameter(description = "分类ID", example = "1")
            @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "10")
            @RequestParam(defaultValue = "10") Integer size) {
        return ApiResponse.success(postService.searchPosts(keyword, categoryId, page, size));
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热搜帖子", description = "返回热度前10的帖子，使用Redis缓存")
    public ApiResponse<List<PostListDTO>> getHotPosts() {
        return ApiResponse.success(postService.getHotPosts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取帖子详情", description = "获取帖子详情，包含评论列表")
    public ApiResponse<PostDetailDTO> getPostDetail(
            @Parameter(description = "帖子ID", example = "1", required = true)
            @PathVariable Long id) {
        Long currentUserId = CurrentUserContext.getUserId();
        return ApiResponse.success(postService.getPostDetail(id, currentUserId));
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "点赞/取消点赞帖子", description = "点赞或取消点赞帖子")
    @SecurityRequirement(name = "Bearer Authentication")
    public ApiResponse<Boolean> toggleLike(
            @Parameter(description = "帖子ID", example = "1", required = true)
            @PathVariable Long id) {
        Long userId = CurrentUserContext.getUserId();
        boolean liked = postService.toggleLike(id, userId);
        return ApiResponse.success(liked);
    }
}
