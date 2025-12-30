package com.hnu.campus.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码DTO
 */
@Data
@Schema(description = "修改密码请求")
public class PasswordUpdateDTO {
    @NotBlank(message = "原密码不能为空")
    @Schema(description = "原密码", example = "123456", requiredMode = RequiredMode.REQUIRED)
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码", example = "123456", requiredMode = RequiredMode.REQUIRED)
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", example = "123456", requiredMode = RequiredMode.REQUIRED)
    private String confirmPassword;
}
