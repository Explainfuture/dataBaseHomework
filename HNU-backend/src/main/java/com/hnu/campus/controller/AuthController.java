package com.hnu.campus.controller;

import com.hnu.campus.dto.auth.LoginDTO;
import com.hnu.campus.dto.auth.LoginResponseDTO;
import com.hnu.campus.dto.auth.RegisterDTO;
import com.hnu.campus.dto.common.ApiResponse;
import com.hnu.campus.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "认证管理", description = "用户注册、登录等认证相关接口")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册接口，包含手机验证码校验逻辑")
    public ApiResponse<Long> register(@Valid @RequestBody RegisterDTO registerDTO) {
        Long userId = authService.register(registerDTO);
        return ApiResponse.success("注册成功，等待审核", userId);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口，返回JWT Token")
    public ApiResponse<LoginResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginResponseDTO response = authService.login(loginDTO);
        return ApiResponse.success(response);
    }

    @PostMapping("/send-verify-code")
    @Operation(summary = "发送验证码", description = "发送手机验证码到Redis，有效期5分钟")
    public ApiResponse<Void> sendVerifyCode(@RequestParam String phone) {
        authService.sendVerifyCode(phone);
        return ApiResponse.success("验证码已发送");
    }
}
