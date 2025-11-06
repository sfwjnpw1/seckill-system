package com.manus.seckill.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.manus.seckill.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT * FROM t_order WHERE order_sn = #{orderSn}")
    Order selectByOrderSn(String orderSn);

}
