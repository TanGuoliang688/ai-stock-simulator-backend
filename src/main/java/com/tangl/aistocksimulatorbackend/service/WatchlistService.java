package com.tangl.aistocksimulatorbackend.service;

import com.tangl.aistocksimulatorbackend.entity.Stock;
import com.tangl.aistocksimulatorbackend.entity.Watchlist;
import com.tangl.aistocksimulatorbackend.mapper.StockMapper;
import com.tangl.aistocksimulatorbackend.mapper.WatchlistMapper;
import com.tangl.aistocksimulatorbackend.vo.StockVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistMapper watchlistMapper;
    private final StockMapper stockMapper;

    /**
     * 添加自选股
     */
    @Transactional
    public void addToWatchlist(Long userId, String symbol) {
        // 检查股票是否存在
        Stock stock = stockMapper.findBySymbol(symbol);
        if (stock == null) {
            throw new RuntimeException("股票不存在: " + symbol);
        }

        // 检查是否已添加
        Watchlist existing = watchlistMapper.findByUserAndStock(userId, stock.getId());
        if (existing != null) {
            throw new RuntimeException("已在自选股中");
        }

        // 添加到自选股
        Watchlist watchlist = Watchlist.builder()
                .userId(userId)
                .stockId(stock.getId())
                .sortOrder(0)
                .build();

        watchlistMapper.insert(watchlist);
        log.info("用户 {} 添加自选股: {}", userId, symbol);
    }

    /**
     * 删除自选股
     */
    @Transactional
    public void removeFromWatchlist(Long userId, String symbol) {
        Stock stock = stockMapper.findBySymbol(symbol);
        if (stock == null) {
            throw new RuntimeException("股票不存在: " + symbol);
        }

        watchlistMapper.deleteByUserAndStock(userId, stock.getId());
        log.info("用户 {} 删除自选股: {}", userId, symbol);
    }

    /**
     * 获取用户的自选股列表
     */
    public List<StockVO> getWatchlist(Long userId) {
        List<Watchlist> watchlists = watchlistMapper.findByUserId(userId);
        
        return watchlists.stream()
                .map(w -> {
                    Stock stock = stockMapper.selectById(w.getStockId());
                    return convertToVO(stock);
                })
                .collect(Collectors.toList());
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
