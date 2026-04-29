
package com.tangl.aistocksimulatorbackend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {
    ACTIVE("ACTIVE", "正常"),
    FROZEN("FROZEN", "冻结"),
    CLOSED("CLOSED", "注销");

    private final String code;
    private final String description;
}
