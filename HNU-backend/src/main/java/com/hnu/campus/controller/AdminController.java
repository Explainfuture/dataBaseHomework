package com.hnu.campus.controller;

import com.hnu.campus.dto.admin.AuthReviewDTO;
import com.hnu.campus.dto.admin.UserMuteDTO;
import com.hnu.campus.dto.common.ApiResponse;
import com.hnu.campus.dto.user.UserInfoDTO;
import com.hnu.campus.security.CurrentUserContext;
import com.hnu.campus.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller.
 */
@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "Admin review/moderation endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/auth/review")
    @Operation(summary = "Review auth", description = "Approve or reject registration")
    public ApiResponse<Void> reviewAuth(@Valid @RequestBody AuthReviewDTO reviewDTO) {
        Long adminId = CurrentUserContext.getUserId();
        adminService.reviewAuth(adminId, reviewDTO);
        return ApiResponse.success("Review success");
    }

    @DeleteMapping("/posts/{id}")
    @Operation(summary = "Delete post", description = "Force delete a post")
    public ApiResponse<Void> forceDeletePost(
            @Parameter(description = "Post ID", example = "1", required = true)
            @PathVariable Long id) {
        Long adminId = CurrentUserContext.getUserId();
        adminService.forceDeletePost(adminId, id);
        return ApiResponse.success("Delete success");
    }

    @PostMapping("/users/mute")
    @Operation(summary = "Mute user", description = "Mute or unmute a user")
    public ApiResponse<Void> muteUser(@Valid @RequestBody UserMuteDTO muteDTO) {
        Long adminId = CurrentUserContext.getUserId();
        adminService.muteUser(adminId, muteDTO);
        return ApiResponse.success("Operation success");
    }

    @PostMapping("/users/kick")
    @Operation(summary = "Kick user", description = "Force logout and revoke tokens")
    public ApiResponse<Void> kickUser(@RequestParam Long userId) {
        Long adminId = CurrentUserContext.getUserId();
        adminService.kickUser(adminId, userId);
        return ApiResponse.success("Kick success");
    }

    @GetMapping("/users/pending")
    @Operation(summary = "Pending users", description = "List pending users")
    @Parameter(name = "page", description = "Page index", example = "1")
    @Parameter(name = "size", description = "Page size", example = "10")
    public ApiResponse<List<UserInfoDTO>> getPendingUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long adminId = CurrentUserContext.getUserId();
        return ApiResponse.success(adminService.getPendingUsers(adminId, page, size));
    }

    @GetMapping("/users")
    @Operation(summary = "All users", description = "List all users")
    @Parameter(name = "page", description = "Page index", example = "1")
    @Parameter(name = "size", description = "Page size", example = "10")
    public ApiResponse<List<UserInfoDTO>> getAllUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long adminId = CurrentUserContext.getUserId();
        return ApiResponse.success(adminService.getAllUsers(adminId, page, size));
    }

    @DeleteMapping("/comments/{id}")
    @Operation(summary = "Delete comment", description = "Delete a comment")
    public ApiResponse<Void> deleteComment(
            @Parameter(description = "Comment ID", example = "1", required = true)
            @PathVariable Long id) {
        Long adminId = CurrentUserContext.getUserId();
        adminService.deleteComment(adminId, id);
        return ApiResponse.success("Delete success");
    }
}
