package com.hnu.campus.task;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hnu.campus.entity.Post;
import com.hnu.campus.mapper.PostMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Slf4j
@Component
public class ViewCountSyncTask {
    private static final String VIEW_KEY_PREFIX = "post:view:";

    private final StringRedisTemplate redisTemplate;
    private final PostMapper postMapper;

    public ViewCountSyncTask(StringRedisTemplate redisTemplate, PostMapper postMapper) {
        this.redisTemplate = redisTemplate;
        this.postMapper = postMapper;
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void syncViewCounts() {
        Set<String> keys = redisTemplate.keys(VIEW_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return;
        }
        for (String key : keys) {
            String idText = key.substring(VIEW_KEY_PREFIX.length());
            Long postId;
            try {
                postId = Long.valueOf(idText);
            } catch (NumberFormatException ex) {
                continue;
            }
            String deltaText = redisTemplate.opsForValue().get(key);
            if (deltaText == null) {
                continue;
            }
            long delta;
            try {
                delta = Long.parseLong(deltaText);
            } catch (NumberFormatException ex) {
                continue;
            }
            if (delta <= 0) {
                redisTemplate.delete(key);
                continue;
            }
            Post post = postMapper.selectById(postId);
            if (post == null) {
                redisTemplate.delete(key);
                continue;
            }
            int newViewCount = post.getViewCount() == null ? (int) delta : post.getViewCount() + (int) delta;
            int likeCount = post.getLikeCount() == null ? 0 : post.getLikeCount();
            BigDecimal hotScore = BigDecimal.valueOf(newViewCount * 0.3 + likeCount * 0.7);
            UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", postId)
                    .set("view_count", newViewCount)
                    .set("hot_score", hotScore)
                    .set("update_time", java.time.LocalDateTime.now());
            postMapper.update(null, updateWrapper);
            redisTemplate.delete(key);
        }
        log.info("Synced post view counts from Redis.");
    }
}
