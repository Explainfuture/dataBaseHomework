package com.hnu.campus.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
@Schema(description = "登录响应")
public class LoginResponseDTO {
    @Schema(description = "JWT Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "角色", example = "STUDENT")
    private String role;
}

