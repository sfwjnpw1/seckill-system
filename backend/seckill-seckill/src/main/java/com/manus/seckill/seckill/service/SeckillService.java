package com.manus.seckill.seckill.service;

import com.manus.seckill.seckill.dto.SeckillResult;

public interface SeckillService {

    /**
     * Get seckill path (hidden path for anti-bot)
     */
    String getSeckillPath(Long activityId, Long userId);

    /**
     * Execute seckill
     */
    SeckillResult doSeckill(Long activityId, Long userId, String path);

    /**
     * Get seckill result
     */
    SeckillResult getSeckillResult(Long activityId, Long userId);

}
