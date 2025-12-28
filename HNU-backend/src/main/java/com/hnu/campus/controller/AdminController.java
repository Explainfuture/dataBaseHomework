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
 * 管理员控制器
 */
@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "管理员管理", description = "管理员审核、管理帖子、禁言用户等接口")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/auth/review")
    @Operation(summary = "审核注册信息", description = "审核用户注册信息，通过或拒绝")
    public ApiResponse<Void> reviewAuth(@Valid @RequestBody AuthReviewDTO reviewDTO) {
        Long adminId = CurrentUserContext.getUserId();
        adminService.reviewAuth(adminId, reviewDTO);
        return ApiResponse.success("审核成功");
    }

    @DeleteMapping("/posts/{id}")
    @Operation(summary = "强制删除帖子", description = "管理员强制删除帖子（逻辑删除）")
    public ApiResponse<Void> forceDeletePost(
            @Parameter(description = "帖子ID", example = "1", required = true)
            @PathVariable Long id) {
        Long adminId = CurrentUserContext.getUserId();
        adminService.forceDeletePost(adminId, id);
        return ApiResponse.success("删除成功");
    }

    @PostMapping("/users/mute")
    @Operation(summary = "禁言用户", description = "管理员禁言或解除禁言用户")
    public ApiResponse<Void> muteUser(@Valid @RequestBody UserMuteDTO muteDTO) {
        Long adminId = CurrentUserContext.getUserId();
        adminService.muteUser(adminId, muteDTO);
        return ApiResponse.success("操作成功");
    }

    @GetMapping("/users/pending")
    @Operation(summary = "获取待审核用户列表", description = "获取所有待审核的用户列表，支持分页")
    @Parameter(name = "page", description = "页码，从1开始", example = "1")
    @Parameter(name = "size", description = "每页数量", example = "10")
    public ApiResponse<List<UserInfoDTO>> getPendingUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long adminId = CurrentUserContext.getUserId();
        return ApiResponse.success(adminService.getPendingUsers(adminId, page, size));
    }

    @GetMapping("/users")
    @Operation(summary = "获取全部用户列表", description = "获取所有用户列表，支持分页")
    @Parameter(name = "page", description = "页码，从1开始", example = "1")
    @Parameter(name = "size", description = "每页数量", example = "10")
    public ApiResponse<List<UserInfoDTO>> getAllUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long adminId = CurrentUserContext.getUserId();
        return ApiResponse.success(adminService.getAllUsers(adminId, page, size));
    }

    @DeleteMapping("/comments/{id}")
    @Operation(summary = "删除评论", description = "管理员删除用户评论（逻辑删除）")
    public ApiResponse<Void> deleteComment(
            @Parameter(description = "评论ID", example = "1", required = true)
            @PathVariable Long id) {
        Long adminId = CurrentUserContext.getUserId();
        adminService.deleteComment(adminId, id);
        return ApiResponse.success("删除成功");
    }
}
