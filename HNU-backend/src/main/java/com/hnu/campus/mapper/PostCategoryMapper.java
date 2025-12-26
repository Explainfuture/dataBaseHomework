package com.hnu.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.campus.entity.PostCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子分类Mapper接口
 */
@Mapper
public interface PostCategoryMapper extends BaseMapper<PostCategory> {
}

