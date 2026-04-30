
package com.tangl.aistocksimulatorbackend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("stock")
public class Stock {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("symbol")
    private String symbol;

    @TableField("name")
    private String name;

    @TableField("market")
    private String market;

    @TableField("industry")
    private String industry;

    @TableField("is_st")
    private Boolean isSt;

    @TableField("is_suspended")
    private Boolean isSuspended;

    @TableField("listing_date")
    private LocalDate listingDate;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
