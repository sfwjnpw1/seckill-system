package com.manus.seckill.seckill.service.impl;

import com.manus.seckill.seckill.entity.SeckillOrder;
import com.manus.seckill.seckill.mapper.SeckillOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
public class SeckillStreamConsumer implements StreamListener<String, ObjectRecord<String, Map<String, String>>> {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String SECKILL_STREAM_KEY = "seckill:stream";

    @PostConstruct
    public void init() {
        // Initialize stream consumer
        log.info("Seckill stream consumer initialized");
    }

    @Override
    public void onMessage(ObjectRecord<String, Map<String, String>> message) {
        try {
            Map<String, String> data = message.getValue();
            String messageData = data.get("data");
            
            if (messageData == null) {
                log.warn("Empty message data");
                return;
            }

            // Parse userId and activityId from message
            String[] parts = messageData.split(":");
            if (parts.length != 2) {
                log.warn("Invalid message format: {}", messageData);
                return;
            }

            Long userId = Long.parseLong(parts[0]);
            Long activityId = Long.parseLong(parts[1]);

            // Create seckill order
            SeckillOrder order = new SeckillOrder();
            order.setUserId(userId);
            order.setActivityId(activityId);
            order.setCreateTime(LocalDateTime.now());

            seckillOrderMapper.insert(order);
            log.info("Seckill order created: userId={}, activityId={}, orderId={}", userId, activityId, order.getId());

        } catch (Exception e) {
            log.error("Error processing seckill stream message", e);
        }
    }

}
