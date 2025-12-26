package com.hnu.campus.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户信息更新DTO
 */
@Data
@Schema(description = "用户信息更新请求")
public class UserUpdateDTO {
    @Size(min = 2, max = 50, message = "昵称长度为2-50字符")
    @Schema(description = "昵称", example = "李四")
    private String nickname;

    @Schema(description = "学号", example = "2021001001")
    private String studentId;

    @Schema(description = "校园卡照片URL", example = "https://example.com/card.jpg")
    private String campusCardUrl;
}

