package com.manus.seckill.order.service;

import com.manus.seckill.order.dto.OrderDTO;
import com.manus.seckill.order.entity.Order;

public interface OrderService {

    /**
     * Create order
     */
    OrderDTO createOrder(Order order);

    /**
     * Get order by order SN
     */
    OrderDTO getOrderByOrderSn(String orderSn);

    /**
     * Get order by ID
     */
    OrderDTO getOrderById(Long id);

    /**
     * Pay order
     */
    void payOrder(String orderSn);

    /**
     * Cancel order
     */
    void cancelOrder(String orderSn);

}
