package com.hnu.campus.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 帖子列表项DTO
 */
@Data
@Schema(description = "帖子列表项")
public class PostListDTO {
    @Schema(description = "帖子ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容摘要（前100字符）")
    private String contentSummary;

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "发布者ID")
    private Long authorId;

    @Schema(description = "发布者昵称")
    private String authorNickname;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "热度值")
    private BigDecimal hotScore;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}

