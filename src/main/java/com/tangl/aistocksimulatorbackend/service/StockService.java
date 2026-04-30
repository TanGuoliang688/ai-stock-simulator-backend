
package com.tangl.aistocksimulatorbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tangl.aistocksimulatorbackend.entity.Stock;
import com.tangl.aistocksimulatorbackend.mapper.StockMapper;
import com.tangl.aistocksimulatorbackend.vo.StockVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockMapper stockMapper;

    /**
     * 搜索股票
     */
    public List<StockVO> searchStocks(String keyword) {
        List<Stock> stocks = stockMapper.searchStocks(keyword);
        return stocks.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取股票列表（分页）
     */
    public Page<StockVO> getStockList(int page, int size, String market) {
        Page<Stock> stockPage = new Page<>(page, size);
        // TODO: 添加市场筛选逻辑
        Page<Stock> result = stockMapper.selectPage(stockPage, null);
        
        Page<StockVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));
        
        return voPage;
    }

    /**
     * 获取股票详情
     */
    public StockVO getStockDetail(String symbol) {
        Stock stock = stockMapper.findBySymbol(symbol);
        if (stock == null) {
            throw new RuntimeException("股票不存在: " + symbol);
        }
        return convertToVO(stock);
    }

    private StockVO convertToVO(Stock stock) {
        return StockVO.builder()
                .id(stock.getId())
                .symbol(stock.getSymbol())
                .name(stock.getName())
                .market(stock.getMarket())
                .industry(stock.getIndustry())
                .isSt(stock.getIsSt())
                .isSuspended(stock.getIsSuspended())
                .build();
    }
}
