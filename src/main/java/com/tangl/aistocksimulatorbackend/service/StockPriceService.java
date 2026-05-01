
package com.tangl.aistocksimulatorbackend.service;

import com.tangl.aistocksimulatorbackend.entity.Stock;
import com.tangl.aistocksimulatorbackend.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockPriceService {

    private final StockMapper stockMapper;
    
    // 存储实时价格：symbol -> currentPrice
    private final Map<String, BigDecimal> priceCache = new ConcurrentHashMap<>();
    
    // 存储价格变化百分比：symbol -> changePercent
    private final Map<String, BigDecimal> changePercentCache = new ConcurrentHashMap<>();

    /**
     * 初始化所有股票的基础价格
     */
    public void initializePrices() {
        List<Stock> stocks = stockMapper.selectList(null);
        Random random = new Random();
        
        for (Stock stock : stocks) {
            // 生成随机基础价格（10-2000元之间）
            BigDecimal basePrice = new BigDecimal(random.nextInt(1990) + 10);
            priceCache.put(stock.getSymbol(), basePrice);
            changePercentCache.put(stock.getSymbol(), BigDecimal.ZERO);
        }
        
        log.info("初始化 {} 只股票的价格", stocks.size());
    }

    /**
     * 获取股票的当前价格
     */
    public BigDecimal getCurrentPrice(String symbol) {
        return priceCache.getOrDefault(symbol, BigDecimal.valueOf(100));
    }

    /**
     * 获取所有股票的实时价格
     */
    public Map<String, Object> getAllPrices() {
        Map<String, Object> result = new HashMap<>();
        priceCache.forEach((symbol, price) -> {
            Map<String, Object> stockData = new HashMap<>();
            stockData.put("price", price);
            stockData.put("changePercent", changePercentCache.getOrDefault(symbol, BigDecimal.ZERO));
            result.put(symbol, stockData);
        });
        return result;
    }

    /**
     * 更新所有股票价格（模拟市场波动）
     */
    public void updateAllPrices() {
        Random random = new Random();
        
        priceCache.forEach((symbol, currentPrice) -> {
            // 生成 -2% 到 +2% 的随机波动
            double changeRate = (random.nextDouble() - 0.5) * 0.04;
            
            // 计算新价格
            BigDecimal newPrice = currentPrice.multiply(
                BigDecimal.valueOf(1 + changeRate)
            ).setScale(2, RoundingMode.HALF_UP);
            
            // 确保价格在合理范围内（不低于1元）
            if (newPrice.compareTo(BigDecimal.ONE) < 0) {
                newPrice = BigDecimal.ONE;
            }
            
            // 更新缓存
            priceCache.put(symbol, newPrice);
            
            // 计算涨跌幅
            BigDecimal changePercent = BigDecimal.valueOf(changeRate * 100)
                .setScale(2, RoundingMode.HALF_UP);
            changePercentCache.put(symbol, changePercent);
        });
        
        log.debug("更新了 {} 只股票的价格", priceCache.size());
    }
}
