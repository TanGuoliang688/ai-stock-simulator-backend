
package com.tangl.aistocksimulatorbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tangl.aistocksimulatorbackend.entity.PriceHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PriceHistoryMapper extends BaseMapper<PriceHistory> {

    @Select("SELECT * FROM price_history WHERE stock_id = #{stockId} ORDER BY trade_date DESC LIMIT #{limit}")
    List<PriceHistory> findRecentKLine(@Param("stockId") Long stockId, @Param("limit") int limit);
}
