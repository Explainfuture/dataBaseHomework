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
 * 评论点赞实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("comment_likes")
public class CommentLike {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long commentId;

    private Long userId;

    private LocalDateTime createTime;
}
