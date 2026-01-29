package com.hnu.campus.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Refresh token request")
public class RefreshTokenDTO {
    @Schema(description = "Refresh Token", example = "f1b2c3d4e5f6...")
    private String refreshToken;
}
