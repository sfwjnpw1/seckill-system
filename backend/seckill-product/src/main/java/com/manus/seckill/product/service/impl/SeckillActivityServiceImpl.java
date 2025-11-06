package com.manus.seckill.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.manus.seckill.product.dto.SeckillActivityDTO;
import com.manus.seckill.product.entity.Product;
import com.manus.seckill.product.entity.SeckillActivity;
import com.manus.seckill.product.mapper.ProductMapper;
import com.manus.seckill.product.mapper.SeckillActivityMapper;
import com.manus.seckill.product.service.SeckillActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeckillActivityServiceImpl implements SeckillActivityService {

    @Autowired
    private SeckillActivityMapper seckillActivityMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String ACTIVITY_CACHE_KEY = "seckill:activity:";
    private static final String ACTIVE_ACTIVITIES_CACHE_KEY = "seckill:activities:active";
    private static final long CACHE_EXPIRATION = 1800; // 30 minutes

    @Override
    public SeckillActivityDTO getActivityById(Long id) {
        // Try to get from cache first
        String cacheKey = ACTIVITY_CACHE_KEY + id;
        SeckillActivityDTO cached = (SeckillActivityDTO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // Get from database
        SeckillActivity activity = seckillActivityMapper.selectById(id);
        if (activity == null) {
            throw new RuntimeException("Seckill activity not found");
        }

        // Convert to DTO and cache
        SeckillActivityDTO dto = convertToDTO(activity);
        redisTemplate.opsForValue().set(cacheKey, dto, CACHE_EXPIRATION, TimeUnit.SECONDS);

        return dto;
    }

    @Override
    public List<SeckillActivityDTO> getActiveActivities() {
        // Try to get from cache first
        List<SeckillActivityDTO> cached = (List<SeckillActivityDTO>) redisTemplate.opsForValue().get(ACTIVE_ACTIVITIES_CACHE_KEY);
        if (cached != null) {
            return cached;
        }

        // Get from database - activities that are active or about to start
        LocalDateTime now = LocalDateTime.now();
        List<SeckillActivity> activities = seckillActivityMapper.selectList(
                new LambdaQueryWrapper<SeckillActivity>()
                        .le(SeckillActivity::getStartTime, now.plusHours(1))
                        .ge(SeckillActivity::getEndTime, now)
        );

        List<SeckillActivityDTO> dtos = activities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Cache the list
        redisTemplate.opsForValue().set(ACTIVE_ACTIVITIES_CACHE_KEY, dtos, CACHE_EXPIRATION, TimeUnit.SECONDS);

        return dtos;
    }

    @Override
    public void createActivity(SeckillActivity activity) {
        seckillActivityMapper.insert(activity);
        // Invalidate cache
        redisTemplate.delete(ACTIVE_ACTIVITIES_CACHE_KEY);
        log.info("Seckill activity created: {}", activity.getId());
    }

    @Override
    public void updateActivity(SeckillActivity activity) {
        seckillActivityMapper.updateById(activity);
        // Invalidate cache
        String cacheKey = ACTIVITY_CACHE_KEY + activity.getId();
        redisTemplate.delete(cacheKey);
        redisTemplate.delete(ACTIVE_ACTIVITIES_CACHE_KEY);
        log.info("Seckill activity updated: {}", activity.getId());
    }

    @Override
    public void deleteActivity(Long id) {
        seckillActivityMapper.deleteById(id);
        // Invalidate cache
        String cacheKey = ACTIVITY_CACHE_KEY + id;
        redisTemplate.delete(cacheKey);
        redisTemplate.delete(ACTIVE_ACTIVITIES_CACHE_KEY);
        log.info("Seckill activity deleted: {}", id);
    }

    @Override
    public void warmUpCache() {
        LocalDateTime now = LocalDateTime.now();
        List<SeckillActivity> activities = seckillActivityMapper.selectList(
                new LambdaQueryWrapper<SeckillActivity>()
                        .le(SeckillActivity::getStartTime, now.plusHours(1))
                        .ge(SeckillActivity::getEndTime, now)
        );

        for (SeckillActivity activity : activities) {
            String cacheKey = ACTIVITY_CACHE_KEY + activity.getId();
            SeckillActivityDTO dto = convertToDTO(activity);
            redisTemplate.opsForValue().set(cacheKey, dto, CACHE_EXPIRATION, TimeUnit.SECONDS);
        }

        List<SeckillActivityDTO> dtos = activities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        redisTemplate.opsForValue().set(ACTIVE_ACTIVITIES_CACHE_KEY, dtos, CACHE_EXPIRATION, TimeUnit.SECONDS);
        log.info("Seckill activity cache warmed up");
    }

    private SeckillActivityDTO convertToDTO(SeckillActivity activity) {
        SeckillActivityDTO dto = new SeckillActivityDTO();
        dto.setId(activity.getId());
        dto.setProductId(activity.getProductId());
        dto.setSeckillPrice(activity.getSeckillPrice());
        dto.setSeckillStock(activity.getSeckillStock());
        dto.setStartTime(activity.getStartTime());
        dto.setEndTime(activity.getEndTime());
        dto.setStatus(activity.getStatus());

        // Get product name
        Product product = productMapper.selectById(activity.getProductId());
        if (product != null) {
            dto.setProductName(product.getName());
        }

        return dto;
    }

}
