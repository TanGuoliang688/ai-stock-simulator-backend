
package com.tangl.aistocksimulatorbackend.controller;

import com.tangl.aistocksimulatorbackend.common.Result;
import com.tangl.aistocksimulatorbackend.entity.Position;
import com.tangl.aistocksimulatorbackend.entity.TradeOrder;
import com.tangl.aistocksimulatorbackend.entity.TradeRecord;
import com.tangl.aistocksimulatorbackend.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/trade")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;

    /**
     * 买入股票
     */
    @PostMapping("/buy")
    public Result<TradeOrder> buy(
            @RequestParam String symbol,
            @RequestParam BigDecimal price,
            @RequestParam Integer quantity,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        TradeOrder order = tradeService.buyStock(userId, symbol, price, quantity);
        return Result.success("买入成功", order);
    }

    /**
     * 卖出股票
     */
    @PostMapping("/sell")
    public Result<TradeOrder> sell(
            @RequestParam String symbol,
            @RequestParam BigDecimal price,
            @RequestParam Integer quantity,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        TradeOrder order = tradeService.sellStock(userId, symbol, price, quantity);
        return Result.success("卖出成功", order);
    }

    /**
     * 获取持仓列表
     */
    @GetMapping("/positions")
    public Result<List<Position>> getPositions(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        List<Position> positions = tradeService.getUserPositions(userId);
        return Result.success(positions);
    }

    /**
     * 获取交易记录
     */
    @GetMapping("/records")
    public Result<List<TradeRecord>> getRecords(
            @RequestParam(defaultValue = "50") int limit,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        List<TradeRecord> records = tradeService.getUserTradeRecords(userId, limit);
        return Result.success(records);
    }

    private Long getCurrentUserId(Authentication authentication) {
        // TODO: 从 JWT Token 中解析用户ID
        return 1L;
    }
}
