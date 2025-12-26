package com.hnu.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 手机号，唯一
     */
    private String phone;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 密码，加密存储
     */
    private String password;

    /**
     * 学号
     */
    private String studentId;

    /**
     * 校园卡照片URL
     */
    private String campusCardUrl;

    /**
     * 认证状态：pending(待审核)/approved(通过)/rejected(拒绝)
     */
    private String authStatus;

    /**
     * 角色：STUDENT(学生)/ADMIN(管理员)
     */
    private String role;

    /**
     * 被禁言状态
     */
    private Boolean isMuted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

