package com.manus.seckill.order.controller;

import com.manus.seckill.order.common.Result;
import com.manus.seckill.order.dto.OrderDTO;
import com.manus.seckill.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/{orderSn}")
    public Result<OrderDTO> getOrderByOrderSn(@PathVariable String orderSn) {
        try {
            OrderDTO order = orderService.getOrderByOrderSn(orderSn);
            return Result.success(order);
        } catch (Exception e) {
            log.error("Failed to get order", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/pay/{orderSn}")
    public Result<String> payOrder(@PathVariable String orderSn) {
        try {
            orderService.payOrder(orderSn);
            return Result.success("Order paid successfully");
        } catch (Exception e) {
            log.error("Failed to pay order", e);
            return Result.error(e.getMessage());
        }
    }

}
