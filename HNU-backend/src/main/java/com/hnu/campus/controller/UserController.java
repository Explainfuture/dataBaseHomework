package com.hnu.campus.controller;

import com.hnu.campus.dto.common.ApiResponse;
import com.hnu.campus.dto.post.PostListDTO;
import com.hnu.campus.dto.user.UserInfoDTO;
import com.hnu.campus.dto.user.UserUpdateDTO;
import com.hnu.campus.security.CurrentUserContext;
import com.hnu.campus.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "用户管理", description = "用户个人信息相关接口")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "获取个人信息", description = "获取当前登录用户的个人信息")
    public ApiResponse<UserInfoDTO> getMyInfo() {
        Long userId = CurrentUserContext.getUserId();
        return ApiResponse.success(userService.getUserInfo(userId));
    }

    @PutMapping("/me")
    @Operation(summary = "修改个人信息", description = "修改当前登录用户的个人信息")
    public ApiResponse<Void> updateMyInfo(@Valid @RequestBody UserUpdateDTO updateDTO) {
        Long userId = CurrentUserContext.getUserId();
        userService.updateUserInfo(userId, updateDTO);
        return ApiResponse.success("修改成功");
    }

    @GetMapping("/me/posts")
    @Operation(summary = "查看我的发帖", description = "获取当前用户发布的帖子列表，支持分页")
    @Parameter(name = "page", description = "页码，从1开始", example = "1")
    @Parameter(name = "size", description = "每页数量", example = "10")
    public ApiResponse<List<PostListDTO>> getMyPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = CurrentUserContext.getUserId();
        return ApiResponse.success(userService.getUserPosts(userId, page, size));
    }
}
