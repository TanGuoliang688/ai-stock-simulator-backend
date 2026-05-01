package com.tangl.aistocksimulatorbackend.service;

import com.tangl.aistocksimulatorbackend.entity.*;
import com.tangl.aistocksimulatorbackend.enums.OrderStatus;
import com.tangl.aistocksimulatorbackend.enums.TradeType;
import com.tangl.aistocksimulatorbackend.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeOrderMapper tradeOrderMapper;
    private final PositionMapper positionMapper;
    private final TradeRecordMapper tradeRecordMapper;
    private final StockMapper stockMapper;
    private final UserMapper userMapper;

    /**
     * 买入股票
     */
    @Transactional
    public TradeOrder buyStock(Long userId, String symbol, BigDecimal price, Integer quantity) {
        // 1. 检查股票是否存在
        Stock stock = stockMapper.findBySymbol(symbol);
        if (stock == null) {
            throw new RuntimeException("股票不存在: " + symbol);
        }

        // 2. 检查用户资金是否充足
        User user = userMapper.selectById(userId);
        BigDecimal totalAmount = price.multiply(new BigDecimal(quantity));
        BigDecimal commission = calculateCommission(totalAmount);
        BigDecimal requiredAmount = totalAmount.add(commission);

        if (user.getVirtualBalance().compareTo(requiredAmount) < 0) {
            throw new RuntimeException("资金不足，需要: " + requiredAmount + ", 余额: " + user.getVirtualBalance());
        }

        // 3. 扣减资金
        user.setVirtualBalance(user.getVirtualBalance().subtract(requiredAmount));
        userMapper.updateById(user);

        // 4. 创建订单
        TradeOrder order = TradeOrder.builder()
                .userId(userId)
                .stockId(stock.getId())
                .symbol(symbol)
                .orderType(TradeType.BUY.getCode())
                .price(price)
                .quantity(quantity)
                .filledQuantity(quantity)
                .totalAmount(totalAmount)
                .commission(commission)
                .status(OrderStatus.FILLED.getCode())
                .build();
        tradeOrderMapper.insert(order);

        // 5. 更新持仓
        updatePositionAfterBuy(userId, stock.getId(), symbol, price, quantity);

        // 6. 创建交易记录
        createTradeRecord(userId, order.getId(), stock.getId(), symbol, TradeType.BUY, price, quantity, totalAmount, commission);

        log.info("用户 {} 买入 {} {} 股，价格 {}", userId, symbol, quantity, price);
        return order;
    }

    /**
     * 卖出股票
     */
    @Transactional
    public TradeOrder sellStock(Long userId, String symbol, BigDecimal price, Integer quantity) {
        // 1. 检查股票是否存在
        Stock stock = stockMapper.findBySymbol(symbol);
        if (stock == null) {
            throw new RuntimeException("股票不存在: " + symbol);
        }

        // 2. 检查持仓是否充足
        Position position = positionMapper.findByUserAndStock(userId, stock.getId());
        if (position == null || position.getAvailableQuantity() < quantity) {
            throw new RuntimeException("持仓不足");
        }

        // 3. 计算总金额和手续费
        BigDecimal totalAmount = price.multiply(new BigDecimal(quantity));
        BigDecimal commission = calculateCommission(totalAmount);
        BigDecimal stampDuty = totalAmount.multiply(new BigDecimal("0.001")); // 印花税 0.1%
        BigDecimal netAmount = totalAmount.subtract(commission).subtract(stampDuty);

        // 4. 增加资金
        User user = userMapper.selectById(userId);
        user.setVirtualBalance(user.getVirtualBalance().add(netAmount));
        userMapper.updateById(user);

        // 5. 创建订单
        TradeOrder order = TradeOrder.builder()
                .userId(userId)
                .stockId(stock.getId())
                .symbol(symbol)
                .orderType(TradeType.SELL.getCode())
                .price(price)
                .quantity(quantity)
                .filledQuantity(quantity)
                .totalAmount(totalAmount)
                .commission(commission)
                .status(OrderStatus.FILLED.getCode())
                .build();
        tradeOrderMapper.insert(order);

        // 6. 更新持仓
        updatePositionAfterSell(userId, stock.getId(), position, quantity);

        // 7. 创建交易记录
        createTradeRecord(userId, order.getId(), stock.getId(), symbol, TradeType.SELL, price, quantity, totalAmount, commission);

        log.info("用户 {} 卖出 {} {} 股，价格 {}", userId, symbol, quantity, price);
        return order;
    }

    /**
     * 获取用户持仓列表
     */
    public java.util.List<Position> getUserPositions(Long userId) {
        return positionMapper.findByUserId(userId);
    }

    /**
     * 获取用户交易记录
     */
    public java.util.List<TradeRecord> getUserTradeRecords(Long userId, int limit) {
        return tradeRecordMapper.findRecentRecords(userId, limit);
    }

    // ==================== 私有方法 ====================

    /**
     * 计算手续费（万分之三，最低5元）
     */
    private BigDecimal calculateCommission(BigDecimal amount) {
        BigDecimal commission = amount.multiply(new BigDecimal("0.0003"));
        return commission.max(new BigDecimal("5.00"));
    }

    /**
     * 买入后更新持仓
     */
    private void updatePositionAfterBuy(Long userId, Long stockId, String symbol, 
                                        BigDecimal price, Integer quantity) {
        Position position = positionMapper.findByUserAndStock(userId, stockId);
        
        if (position == null) {
            // 新建持仓
            position = Position.builder()
                    .userId(userId)
                    .stockId(stockId)
                    .symbol(symbol)
                    .quantity(quantity)
                    .availableQuantity(quantity)
                    .avgCostPrice(price)
                    .totalCost(price.multiply(new BigDecimal(quantity)))
                    .build();
            positionMapper.insert(position);
        } else {
            // 更新现有持仓
            BigDecimal totalCost = position.getTotalCost().add(price.multiply(new BigDecimal(quantity)));
            int totalQuantity = position.getQuantity() + quantity;
            BigDecimal avgCostPrice = totalCost.divide(new BigDecimal(totalQuantity), 2, RoundingMode.HALF_UP);
            
            position.setQuantity(totalQuantity);
            position.setAvailableQuantity(position.getAvailableQuantity() + quantity);
            position.setAvgCostPrice(avgCostPrice);
            position.setTotalCost(totalCost);
            positionMapper.updateById(position);
        }
    }

    /**
     * 卖出后更新持仓
     */
    private void updatePositionAfterSell(Long userId, Long stockId, Position position, Integer quantity) {
        position.setQuantity(position.getQuantity() - quantity);
        position.setAvailableQuantity(position.getAvailableQuantity() - quantity);
        
        if (position.getQuantity() == 0) {
            positionMapper.deleteEmptyPosition(userId, stockId);
        } else {
            positionMapper.updateById(position);
        }
    }

    /**
     * 创建交易记录
     */
    private void createTradeRecord(Long userId, Long orderId, Long stockId, String symbol,
                                   TradeType tradeType, BigDecimal price, Integer quantity,
                                   BigDecimal totalAmount, BigDecimal commission) {
        TradeRecord record = TradeRecord.builder()
                .userId(userId)
                .orderId(orderId)
                .stockId(stockId)
                .symbol(symbol)
                .tradeType(tradeType.getCode())
                .price(price)
                .quantity(quantity)
                .totalAmount(totalAmount)
                .commission(commission)
                .tradeTime(LocalDateTime.now())
                .build();
        tradeRecordMapper.insert(record);
    }
}
