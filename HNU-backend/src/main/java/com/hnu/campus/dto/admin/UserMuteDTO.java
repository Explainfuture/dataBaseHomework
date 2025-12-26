package com.hnu.campus.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户禁言DTO
 */
@Data
@Schema(description = "用户禁言请求")
public class UserMuteDTO {
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1", requiredMode = RequiredMode.REQUIRED)
    private Long userId;

    @NotNull(message = "禁言状态不能为空")
    @Schema(description = "是否禁言：true(禁言)/false(解禁)", example = "true", requiredMode = RequiredMode.REQUIRED)
    private Boolean isMuted;
}

