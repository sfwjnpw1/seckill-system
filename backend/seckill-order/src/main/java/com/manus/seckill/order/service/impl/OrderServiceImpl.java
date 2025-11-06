package com.manus.seckill.order.service.impl;

import com.manus.seckill.order.dto.OrderDTO;
import com.manus.seckill.order.entity.Order;
import com.manus.seckill.order.mapper.OrderMapper;
import com.manus.seckill.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String ORDER_CANCEL_EXCHANGE = "order.cancel.exchange";
    private static final String ORDER_CANCEL_ROUTING_KEY = "order.cancel.routing.key";

    @Override
    public OrderDTO createOrder(Order order) {
        // Generate order SN
        order.setOrderSn(generateOrderSn());
        order.setStatus(0); // Pending payment
        order.setCreateTime(LocalDateTime.now());

        orderMapper.insert(order);

        // Send message to RabbitMQ for delayed cancellation (30 minutes)
        sendOrderCancellationMessage(order.getOrderSn(), 30 * 60 * 1000); // 30 minutes in milliseconds

        log.info("Order created: orderSn={}, userId={}", order.getOrderSn(), order.getUserId());

        return convertToDTO(order);
    }

    @Override
    public OrderDTO getOrderByOrderSn(String orderSn) {
        Order order = orderMapper.selectByOrderSn(orderSn);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }
        return convertToDTO(order);
    }

    @Override
    public OrderDTO getOrderById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }
        return convertToDTO(order);
    }

    @Override
    public void payOrder(String orderSn) {
        Order order = orderMapper.selectByOrderSn(orderSn);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }

        if (order.getStatus() != 0) {
            throw new RuntimeException("Order cannot be paid in current status");
        }

        order.setStatus(1); // Paid
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("Order paid: orderSn={}", orderSn);
    }

    @Override
    public void cancelOrder(String orderSn) {
        Order order = orderMapper.selectByOrderSn(orderSn);
        if (order == null) {
            log.warn("Order not found for cancellation: orderSn={}", orderSn);
            return;
        }

        if (order.getStatus() == 0) {
            order.setStatus(2); // Cancelled
            orderMapper.updateById(order);
            log.info("Order cancelled: orderSn={}", orderSn);

            // TODO: Restore stock to seckill activity
        }
    }

    private String generateOrderSn() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
    }

    private void sendOrderCancellationMessage(String orderSn, long delayMillis) {
        try {
            // Send message to RabbitMQ with TTL (Time To Live)
            rabbitTemplate.convertAndSend(ORDER_CANCEL_EXCHANGE, ORDER_CANCEL_ROUTING_KEY, orderSn, message -> {
                message.getMessageProperties().setExpiration(String.valueOf(delayMillis));
                return message;
            });
            log.info("Order cancellation message sent: orderSn={}, delay={}ms", orderSn, delayMillis);
        } catch (Exception e) {
            log.error("Failed to send order cancellation message", e);
        }
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderSn(order.getOrderSn());
        dto.setUserId(order.getUserId());
        dto.setProductId(order.getProductId());
        dto.setSeckillActivityId(order.getSeckillActivityId());
        dto.setSeckillPrice(order.getSeckillPrice());
        dto.setStatus(order.getStatus());
        dto.setCreateTime(order.getCreateTime());
        dto.setPayTime(order.getPayTime());
        return dto;
    }

}
