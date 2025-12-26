package com.hnu.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 帖子实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("posts")
public class Post {
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 标题，限制4-20字
     */
    private String title;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 发布者ID
     */
    private Long authorId;

    /**
     * 联系方式
     */
    private String contactInfo;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 热度值，用于热搜排序
     */
    private BigDecimal hotScore;

    /**
     * 状态：normal(正常)/deleted(删除)
     */
    private String status;

    /**
     * 全文检索向量（PostgreSQL TSVECTOR类型）
     */
    private String searchVector;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

