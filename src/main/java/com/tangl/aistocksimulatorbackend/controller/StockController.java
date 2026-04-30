
package com.tangl.aistocksimulatorbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tangl.aistocksimulatorbackend.common.Result;
import com.tangl.aistocksimulatorbackend.service.StockService;
import com.tangl.aistocksimulatorbackend.vo.KLineDataVO;
import com.tangl.aistocksimulatorbackend.vo.StockVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * 搜索股票
     */
    @GetMapping("/search")
    public Result<List<StockVO>> searchStocks(@RequestParam String keyword) {
        List<StockVO> stocks = stockService.searchStocks(keyword);
        return Result.success(stocks);
    }

    /**
     * 获取股票列表（分页）
     */
    @GetMapping("/list")
    public Result<Page<StockVO>> getStockList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String market) {
        Page<StockVO> result = stockService.getStockList(page, size, market);
        return Result.success(result);
    }

    /**
     * 获取股票详情
     */
    @GetMapping("/{symbol}")
    public Result<StockVO> getStockDetail(@PathVariable String symbol) {
        StockVO stock = stockService.getStockDetail(symbol);
        return Result.success(stock);
    }

    // 在 StockController 中添加
    @GetMapping("/{symbol}/kline")
    public Result<List<KLineDataVO>> getKLineData(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "30") int days) {
        // TODO: 实现K线数据查询
        return Result.success(null);
    }
}
