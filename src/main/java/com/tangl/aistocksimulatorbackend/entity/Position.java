
package com.tangl.aistocksimulatorbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("position")
public class Position {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("stock_id")
    private Long stockId;

    @TableField("symbol")
    private String symbol;

    @TableField("quantity")
    private Integer quantity;

    @TableField("available_quantity")
    private Integer availableQuantity;

    @TableField("avg_cost_price")
    private BigDecimal avgCostPrice;

    @TableField("total_cost")
    private BigDecimal totalCost;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
