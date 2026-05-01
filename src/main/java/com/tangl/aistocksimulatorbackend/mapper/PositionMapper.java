
package com.tangl.aistocksimulatorbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tangl.aistocksimulatorbackend.entity.Position;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PositionMapper extends BaseMapper<Position> {

    @Select("SELECT * FROM position WHERE user_id = #{userId} AND quantity > 0")
    List<Position> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM position WHERE user_id = #{userId} AND stock_id = #{stockId}")
    Position findByUserAndStock(@Param("userId") Long userId, @Param("stockId") Long stockId);

    @Delete("DELETE FROM position WHERE user_id = #{userId} AND stock_id = #{stockId} AND quantity = 0")
    void deleteEmptyPosition(@Param("userId") Long userId, @Param("stockId") Long stockId);
}
