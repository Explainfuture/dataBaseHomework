package com.hnu.campus.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建帖子DTO
 */
@Data
@Schema(description = "创建帖子请求")
public class PostCreateDTO {
    @NotBlank(message = "标题不能为空")
    @Size(min = 4, max = 20, message = "标题长度为4-20字符")
    @Schema(description = "标题", example = "求购二手自行车", requiredMode = RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "内容不能为空")
    @Schema(description = "帖子内容", example = "想买一辆二手自行车，价格面议", requiredMode = RequiredMode.REQUIRED)
    private String content;

    @NotNull(message = "分类ID不能为空")
    @Schema(description = "分类ID", example = "1", requiredMode = RequiredMode.REQUIRED)
    private Integer categoryId;

    @Schema(description = "联系方式", example = "微信：xxx")
    private String contactInfo;
}

