
package com.tangl.aistocksimulatorbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tangl.aistocksimulatorbackend.common.Result;
import com.tangl.aistocksimulatorbackend.service.StockPriceService;
import com.tangl.aistocksimulatorbackend.service.StockService;
import com.tangl.aistocksimulatorbackend.vo.KLineDataVO;
import com.tangl.aistocksimulatorbackend.vo.StockVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final StockPriceService stockPriceService;

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

    /**
     * 获取实时价格
     */
    @GetMapping("/price/{symbol}")
    public Result<Map<String, Object>> getRealTimePrice(@PathVariable String symbol) {
        BigDecimal price = stockPriceService.getCurrentPrice(symbol);
        Map<String, Object> data = new HashMap<>();
        data.put("symbol", symbol);
        data.put("price", price);
        return Result.success(data);
    }

    /**
     * 获取所有股票实时价格
     */
    @GetMapping("/prices")
    public Result<Map<String, Object>> getAllRealTimePrices() {
        Map<String, Object> prices = stockPriceService.getAllPrices();
        return Result.success(prices);
    }
}
