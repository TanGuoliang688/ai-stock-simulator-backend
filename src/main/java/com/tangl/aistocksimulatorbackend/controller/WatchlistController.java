
package com.tangl.aistocksimulatorbackend.controller;

import com.tangl.aistocksimulatorbackend.common.Result;
import com.tangl.aistocksimulatorbackend.service.WatchlistService;
import com.tangl.aistocksimulatorbackend.vo.StockVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    /**
     * 添加自选股
     */
    @PostMapping("/add/{symbol}")
    public Result<Void> addToWatchlist(@PathVariable String symbol, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        watchlistService.addToWatchlist(userId, symbol);
        return Result.success("添加成功", null);
    }

    /**
     * 删除自选股
     */
    @DeleteMapping("/remove/{symbol}")
    public Result<Void> removeFromWatchlist(@PathVariable String symbol, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        watchlistService.removeFromWatchlist(userId, symbol);
        return Result.success("删除成功", null);
    }

    /**
     * 获取自选股列表
     */
    @GetMapping("/list")
    public Result<List<StockVO>> getWatchlist(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        List<StockVO> stocks = watchlistService.getWatchlist(userId);
        return Result.success(stocks);
    }

    private Long getCurrentUserId(Authentication authentication) {
        // TODO: 从 SecurityContext 或 Token 中获取用户ID
        // 暂时返回固定值，后续需要完善
        return 1L;
    }
}
