
package com.tangl.aistocksimulatorbackend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MarketType {
    SH("SH", "上交所"),
    SZ("SZ", "深交所"),
    BJ("BJ", "北交所");

    private final String code;
    private final String description;
}
