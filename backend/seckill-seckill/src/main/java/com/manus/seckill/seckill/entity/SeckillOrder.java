package com.manus.seckill.seckill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_seckill_order")
public class SeckillOrder {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private Long activityId;

    private LocalDateTime createTime;

}
