package com.hnu.campus.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建评论DTO
 */
@Data
@Schema(description = "创建评论请求")
public class CommentCreateDTO {
    @NotNull(message = "帖子ID不能为空")
    @Schema(description = "帖子ID", example = "1", requiredMode = RequiredMode.REQUIRED)
    private Long postId;

    @NotBlank(message = "评论内容不能为空")
    @Schema(description = "评论内容", example = "这个不错！", requiredMode = RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "父评论ID，回复评论时使用", example = "1")
    private Long parentId;
}

