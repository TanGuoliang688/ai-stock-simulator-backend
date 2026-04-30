
package com.tangl.aistocksimulatorbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tangl.aistocksimulatorbackend.entity.Watchlist;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WatchlistMapper extends BaseMapper<Watchlist> {

    @Select("SELECT w.* FROM watchlist w WHERE w.user_id = #{userId} ORDER BY w.sort_order ASC")
    List<Watchlist> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM watchlist WHERE user_id = #{userId} AND stock_id = #{stockId}")
    Watchlist findByUserAndStock(@Param("userId") Long userId, @Param("stockId") Long stockId);

    @Delete("DELETE FROM watchlist WHERE user_id = #{userId} AND stock_id = #{stockId}")
    void deleteByUserAndStock(@Param("userId") Long userId, @Param("stockId") Long stockId);
}
