package com.hnu.campus.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册DTO
 */
@Data
@Schema(description = "用户注册请求")
public class RegisterDTO {
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000", requiredMode = RequiredMode.REQUIRED)
    private String phone;

    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 6, message = "验证码长度为4-6位")
    @Schema(description = "手机验证码", example = "123456", requiredMode = RequiredMode.REQUIRED)
    private String verifyCode;

    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 50, message = "昵称长度为2-50字符")
    @Schema(description = "昵称", example = "张三", requiredMode = RequiredMode.REQUIRED)
    private String nickname;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度为6-20字符")
    @Schema(description = "密码", example = "123456", requiredMode = RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "学号", example = "2021001001")
    private String studentId;

    @Schema(description = "校园卡照片URL", example = "https://example.com/card.jpg")
    private String campusCardUrl;
}

