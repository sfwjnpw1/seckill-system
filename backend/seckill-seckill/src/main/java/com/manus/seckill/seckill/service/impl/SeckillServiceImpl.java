package com.manus.seckill.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.manus.seckill.seckill.dto.SeckillResult;
import com.manus.seckill.seckill.entity.SeckillOrder;
import com.manus.seckill.seckill.mapper.SeckillOrderMapper;
import com.manus.seckill.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String SECKILL_PATH_PREFIX = "seckill:path:";
    private static final String SECKILL_STOCK_PREFIX = "seckill:stock:";
    private static final String SECKILL_RESULT_PREFIX = "seckill:result:";
    private static final String SECKILL_STREAM_KEY = "seckill:stream";
    private static final String SECKILL_LOCK_PREFIX = "seckill:lock:";

    @Override
    public String getSeckillPath(Long activityId, Long userId) {
        // Generate a random path for this user
        String path = UUID.randomUUID().toString();
        String cacheKey = SECKILL_PATH_PREFIX + activityId + ":" + userId;
        
        // Cache the path for 5 minutes
        redisTemplate.opsForValue().set(cacheKey, path, 5, TimeUnit.MINUTES);
        
        log.info("Generated seckill path for user {} and activity {}: {}", userId, activityId, path);
        return path;
    }

    @Override
    public SeckillResult doSeckill(Long activityId, Long userId, String path) {
        try {
            // Verify path
            String cacheKey = SECKILL_PATH_PREFIX + activityId + ":" + userId;
            String validPath = (String) redisTemplate.opsForValue().get(cacheKey);
            
            if (!path.equals(validPath)) {
                log.warn("Invalid seckill path for user {} and activity {}", userId, activityId);
                return new SeckillResult(-1, "Invalid seckill path", null);
            }

            // Check if user already participated
            LambdaQueryWrapper<SeckillOrder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SeckillOrder::getUserId, userId)
                    .eq(SeckillOrder::getActivityId, activityId);
            SeckillOrder existingOrder = seckillOrderMapper.selectOne(queryWrapper);
            
            if (existingOrder != null) {
                log.warn("User {} already participated in seckill activity {}", userId, activityId);
                return new SeckillResult(-1, "You have already participated in this seckill", null);
            }

            // Use distributed lock to prevent stock deduction race condition
            String lockKey = SECKILL_LOCK_PREFIX + activityId;
            RLock lock = redissonClient.getLock(lockKey);
            
            try {
                // Try to acquire lock with timeout
                if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                    log.warn("Failed to acquire lock for seckill activity {}", activityId);
                    return new SeckillResult(0, "Waiting in queue", null);
                }

                // Check stock in Redis
                String stockKey = SECKILL_STOCK_PREFIX + activityId;
                Long stock = (Long) redisTemplate.opsForValue().get(stockKey);
                
                if (stock == null || stock <= 0) {
                    log.warn("No stock available for seckill activity {}", activityId);
                    return new SeckillResult(-1, "Seckill has ended", null);
                }

                // Deduct stock
                redisTemplate.opsForValue().decrement(stockKey);
                
                // Send message to stream for async order creation
                String message = userId + ":" + activityId;
                redisTemplate.opsForStream().add(SECKILL_STREAM_KEY, 
                    java.util.Collections.singletonMap("data", message));
                
                log.info("User {} successfully participated in seckill activity {}", userId, activityId);
                return new SeckillResult(0, "Waiting for order confirmation", null);
                
            } finally {
                lock.unlock();
            }
            
        } catch (Exception e) {
            log.error("Error during seckill", e);
            return new SeckillResult(-1, "Seckill failed: " + e.getMessage(), null);
        }
    }

    @Override
    public SeckillResult getSeckillResult(Long activityId, Long userId) {
        try {
            // Check if order was created
            LambdaQueryWrapper<SeckillOrder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SeckillOrder::getUserId, userId)
                    .eq(SeckillOrder::getActivityId, activityId);
            SeckillOrder order = seckillOrderMapper.selectOne(queryWrapper);
            
            if (order != null) {
                SeckillResult result = new SeckillResult();
                result.setStatus(1);
                result.setMessage("Seckill successful");
                result.setOrderId(order.getId());
                return result;
            }
            
            // Check if still in queue
            String stockKey = SECKILL_STOCK_PREFIX + activityId;
            Long stock = (Long) redisTemplate.opsForValue().get(stockKey);
            
            if (stock != null && stock > 0) {
                return new SeckillResult(0, "Still waiting in queue", null);
            }
            
            return new SeckillResult(-1, "Seckill has ended", null);
            
        } catch (Exception e) {
            log.error("Error getting seckill result", e);
            return new SeckillResult(-1, "Failed to get result: " + e.getMessage(), null);
        }
    }

}
