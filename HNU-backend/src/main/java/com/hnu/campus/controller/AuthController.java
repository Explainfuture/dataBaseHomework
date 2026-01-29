package com.hnu.campus.controller;

import com.hnu.campus.dto.auth.LoginDTO;
import com.hnu.campus.dto.auth.LoginResponseDTO;
import com.hnu.campus.dto.auth.RegisterDTO;
import com.hnu.campus.dto.common.ApiResponse;
import com.hnu.campus.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * Auth controller.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Register/login/refresh/verify-code endpoints")
public class AuthController {
    private final AuthService authService;

    @Value("${jwt.refresh-expire-seconds:2592000}")
    private long refreshExpireSeconds;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register", description = "Register a new user")
    public ApiResponse<Long> register(@Valid @RequestBody RegisterDTO registerDTO) {
        Long userId = authService.register(registerDTO);
        return ApiResponse.success("Register success", userId);
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Login and return access token")
    public ApiResponse<LoginResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletResponse response) {
        LoginResponseDTO loginResponse = authService.login(loginDTO);
        writeRefreshCookie(response, loginResponse.getRefreshToken());
        loginResponse.setRefreshToken(null);
        return ApiResponse.success(loginResponse);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh access token by refresh token")
    public ApiResponse<LoginResponseDTO> refresh(
            @CookieValue(value = "hnu_refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        LoginResponseDTO refreshResponse = authService.refresh(refreshToken);
        writeRefreshCookie(response, refreshResponse.getRefreshToken());
        refreshResponse.setRefreshToken(null);
        return ApiResponse.success(refreshResponse);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Clear refresh token and logout")
    public ApiResponse<Void> logout(
            @CookieValue(value = "hnu_refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        authService.logout(refreshToken);
        clearRefreshCookie(response);
        return ApiResponse.success("Logout success");
    }

    @PostMapping("/send-verify-code")
    @Operation(summary = "Send verify code", description = "Send SMS verify code and store in Redis")
    public ApiResponse<Void> sendVerifyCode(@RequestParam String phone) {
        authService.sendVerifyCode(phone);
        return ApiResponse.success("Verify code sent");
    }

    private void writeRefreshCookie(HttpServletResponse response, String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        ResponseCookie cookie = ResponseCookie.from("hnu_refresh_token", refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofSeconds(refreshExpireSeconds))
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("hnu_refresh_token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ZERO)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
