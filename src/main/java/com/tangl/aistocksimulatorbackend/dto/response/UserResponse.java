
package com.tangl.aistocksimulatorbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String phone;
    private String avatarUrl;
    private BigDecimal virtualBalance;
    private BigDecimal totalAssets;
    private String status;
}
