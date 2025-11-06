package com.manus.seckill.order.listener;

import com.manus.seckill.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderCancellationListener {

    @Autowired
    private OrderService orderService;

    @RabbitListener(queues = "order.cancel.queue")
    public void handleOrderCancellation(String orderSn) {
        try {
            log.info("Processing order cancellation: orderSn={}", orderSn);
            orderService.cancelOrder(orderSn);
        } catch (Exception e) {
            log.error("Error processing order cancellation", e);
        }
    }

}
