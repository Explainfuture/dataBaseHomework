package com.hnu.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.campus.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论Mapper接口
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}

