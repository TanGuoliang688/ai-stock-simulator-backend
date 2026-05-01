
package com.tangl.aistocksimulatorbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tangl.aistocksimulatorbackend.entity.TradeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TradeRecordMapper extends BaseMapper<TradeRecord> {

    @Select("SELECT * FROM trade_record WHERE user_id = #{userId} ORDER BY trade_time DESC LIMIT #{limit}")
    List<TradeRecord> findRecentRecords(@Param("userId") Long userId, @Param("limit") int limit);
}
