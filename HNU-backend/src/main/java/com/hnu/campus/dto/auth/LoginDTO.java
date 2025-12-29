package com.hnu.campus.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录DTO
 */
@Data
@Schema(description = "用户登录请求")
public class LoginDTO {
    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号", example = "13800138000", requiredMode = RequiredMode.REQUIRED)
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456", requiredMode = RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "手机验证码", example = "123456", requiredMode = RequiredMode.REQUIRED)
    private String verifyCode;
}
