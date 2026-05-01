
package com.tangl.aistocksimulatorbackend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    PENDING("PENDING", "待成交"),
    FILLED("FILLED", "已成交"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String description;
}
