package com.hnu.campus.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Login response DTO.
 */
@Data
@Schema(description = "Login response")
public class LoginResponseDTO {
    @Schema(description = "JWT Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "Nickname", example = "Alice")
    private String nickname;

    @Schema(description = "Role", example = "STUDENT")
    private String role;

    @Schema(description = "Refresh Token", example = "f1b2c3d4e5f6...")
    private String refreshToken;

    @Schema(description = "Access token TTL (seconds)", example = "1800")
    private Long expiresIn;
}
