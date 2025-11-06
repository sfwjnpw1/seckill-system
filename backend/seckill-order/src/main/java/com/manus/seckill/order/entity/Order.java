package com.manus.seckill.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_order")
public class Order {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String orderSn;

    private Long userId;

    private Long productId;

    private Long seckillActivityId;

    private BigDecimal seckillPrice;

    /**
     * 0: pending payment, 1: paid, 2: cancelled
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime payTime;

}
