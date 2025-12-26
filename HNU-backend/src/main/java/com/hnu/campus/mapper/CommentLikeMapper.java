package com.hnu.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.campus.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论点赞Mapper接口
 */
@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {
}
