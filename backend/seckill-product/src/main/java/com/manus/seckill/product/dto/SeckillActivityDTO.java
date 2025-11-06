package com.manus.seckill.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillActivityDTO {

    private Long id;

    private Long productId;

    private String productName;

    private BigDecimal seckillPrice;

    private Integer seckillStock;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

}
