package com.manus.seckill.seckill.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillResult {

    /**
     * 0: waiting in queue
     * 1: success
     * -1: failed
     */
    private Integer status;

    private String message;

    private Long orderId;

}
