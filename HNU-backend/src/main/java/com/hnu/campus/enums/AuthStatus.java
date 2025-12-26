package com.hnu.campus.enums;

import lombok.Getter;

/**
 * 用户认证状态枚举
 */
@Getter
public enum AuthStatus {
    PENDING("pending", "待审核"),
    APPROVED("approved", "通过"),
    REJECTED("rejected", "拒绝");

    private final String code;
    private final String desc;

    AuthStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AuthStatus fromCode(String code) {
        for (AuthStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown auth status: " + code);
    }
}

