package com.hnu.campus.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论DTO
 */
@Data
@Schema(description = "评论信息")
public class CommentDTO {
    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "评论者ID")
    private Long userId;

    @Schema(description = "评论者昵称")
    private String userNickname;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "父评论ID，NULL表示直接评论帖子")
    private Long parentId;

    @Schema(description = "父评论用户昵称")
    private String parentUserNickname;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "是否已点赞")
    private Boolean isLiked;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "子评论列表")
    private List<CommentDTO> replies;
}

