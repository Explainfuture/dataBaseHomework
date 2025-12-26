package com.hnu.campus.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息DTO
 */
@Data
@Schema(description = "用户信息")
public class UserInfoDTO {
    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "学号")
    private String studentId;

    @Schema(description = "校园卡照片URL")
    private String campusCardUrl;

    @Schema(description = "认证状态")
    private String authStatus;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "是否被禁言")
    private Boolean isMuted;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

