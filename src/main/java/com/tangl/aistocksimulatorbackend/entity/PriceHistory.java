
package com.tangl.aistocksimulatorbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("price_history")
public class PriceHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("stock_id")
    private Long stockId;

    @TableField("trade_date")
    private LocalDate tradeDate;

    @TableField("open_price")
    private BigDecimal openPrice;

    @TableField("high_price")
    private BigDecimal highPrice;

    @TableField("low_price")
    private BigDecimal lowPrice;

    @TableField("close_price")
    private BigDecimal closePrice;

    @TableField("volume")
    private Long volume;

    @TableField("turnover")
    private BigDecimal turnover;

    @TableField("adjusted_close")
    private BigDecimal adjustedClose;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
