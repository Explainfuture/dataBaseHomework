package com.hnu.campus.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审核用户注册DTO
 */
@Data
@Schema(description = "审核用户注册请求")
public class AuthReviewDTO {
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1", requiredMode = RequiredMode.REQUIRED)
    private Long userId;

    @NotNull(message = "审核结果不能为空")
    @Schema(description = "审核结果：approved(通过)/rejected(拒绝)", example = "approved", requiredMode = RequiredMode.REQUIRED)
    private String authStatus;
}

