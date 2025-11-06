package com.manus.seckill.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    private Long id;

    private String orderSn;

    private Long userId;

    private Long productId;

    private Long seckillActivityId;

    private BigDecimal seckillPrice;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime payTime;

}
