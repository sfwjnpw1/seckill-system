package com.manus.seckill.product.service;

import com.manus.seckill.product.dto.SeckillActivityDTO;
import com.manus.seckill.product.entity.SeckillActivity;

import java.util.List;

public interface SeckillActivityService {

    /**
     * Get seckill activity by ID
     */
    SeckillActivityDTO getActivityById(Long id);

    /**
     * Get all active seckill activities
     */
    List<SeckillActivityDTO> getActiveActivities();

    /**
     * Create seckill activity
     */
    void createActivity(SeckillActivity activity);

    /**
     * Update seckill activity
     */
    void updateActivity(SeckillActivity activity);

    /**
     * Delete seckill activity
     */
    void deleteActivity(Long id);

    /**
     * Warm up cache for seckill activities
     */
    void warmUpCache();

}
