package com.hnu.campus.dto.post;

import com.hnu.campus.dto.comment.CommentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子详情DTO
 */
@Data
@Schema(description = "帖子详情")
public class PostDetailDTO {
    @Schema(description = "帖子ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "发布者ID")
    private Long authorId;

    @Schema(description = "发布者昵称")
    private String authorNickname;

    @Schema(description = "联系方式")
    private String contactInfo;

    @Schema(description = "浏览量")
    private Integer viewCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "热度值")
    private BigDecimal hotScore;

    @Schema(description = "是否已点赞")
    private Boolean isLiked;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "评论列表")
    private List<CommentDTO> comments;
}

