
package com.tangl.aistocksimulatorbackend.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockVO {

    private Long id;
    private String symbol;
    private String name;
    private String market;
    private String industry;
    private Boolean isSt;
    private Boolean isSuspended;
    
    // 实时价格信息（可选）
    private BigDecimal currentPrice;
    private BigDecimal changePercent;
    private Long volume;
}
