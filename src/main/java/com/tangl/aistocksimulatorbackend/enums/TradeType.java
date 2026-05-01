
package com.tangl.aistocksimulatorbackend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TradeType {
    BUY("BUY", "买入"),
    SELL("SELL", "卖出");

    private final String code;
    private final String description;
}
