package com.hnu.campus.enums;

import lombok.Getter;

/**
 * 帖子状态枚举
 */
@Getter
public enum PostStatus {
    NORMAL("normal", "正常"),
    DELETED("deleted", "删除");

    private final String code;
    private final String desc;

    PostStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PostStatus fromCode(String code) {
        for (PostStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown post status: " + code);
    }
}

