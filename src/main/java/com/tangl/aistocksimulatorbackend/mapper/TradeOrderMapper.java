
package com.tangl.aistocksimulatorbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tangl.aistocksimulatorbackend.entity.TradeOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TradeOrderMapper extends BaseMapper<TradeOrder> {

    @Select("SELECT * FROM trade_order WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit}")
    List<TradeOrder> findRecentOrders(@Param("userId") Long userId, @Param("limit") int limit);
}
