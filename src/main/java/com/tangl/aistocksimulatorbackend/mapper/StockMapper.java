
package com.tangl.aistocksimulatorbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tangl.aistocksimulatorbackend.entity.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StockMapper extends BaseMapper<Stock> {

    @Select("SELECT * FROM stock WHERE symbol LIKE CONCAT('%', #{keyword}, '%') OR name LIKE CONCAT('%', #{keyword}, '%')")
    List<Stock> searchStocks(@Param("keyword") String keyword);

    @Select("SELECT * FROM stock WHERE symbol = #{symbol}")
    Stock findBySymbol(@Param("symbol") String symbol);
}
