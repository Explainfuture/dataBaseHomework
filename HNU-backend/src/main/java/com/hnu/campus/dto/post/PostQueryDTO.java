package com.hnu.campus.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 帖子查询DTO
 */
@Data
@Schema(description = "帖子查询请求")
public class PostQueryDTO {
    @Schema(description = "分类ID", example = "1")
    private Integer categoryId;

    @Schema(description = "搜索关键词（标题或内容）", example = "自行车")
    private String keyword;

    @Schema(description = "页码，从1开始", example = "1")
    private Integer page = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer size = 10;
}

