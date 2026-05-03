
package com.tangl.aistocksimulatorbackend.controller;

import com.tangl.aistocksimulatorbackend.entity.Position;
import com.tangl.aistocksimulatorbackend.mapper.PositionMapper;
import com.tangl.aistocksimulatorbackend.service.AIService;
import com.tangl.aistocksimulatorbackend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;
    private final PositionMapper positionMapper;

    /**
     * 获取智能选股推荐
     */
    @GetMapping("/recommendations")
    public Map<String, Object> getRecommendations(@RequestParam(defaultValue = "震荡上行") String marketTrend) {
        Map<String, Object> result = new HashMap<>();
        try {
            String recommendations = aiService.getStockRecommendations(marketTrend);
            result.put("code", 200);
            result.put("data", recommendations);
            result.put("message", "success");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取推荐失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取交易建议
     */
    @GetMapping("/trade-advice")
    public Map<String, Object> getTradeAdvice() {
        Long userId = SecurityUtils.getCurrentUserId();
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取用户持仓
            List<Position> positions = positionMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Position>()
                            .eq(Position::getUserId, userId)
            );

            // 计算持仓总值
            double totalPositionValue = positions.stream()
                    .mapToDouble(p -> p.getAvgCostPrice().doubleValue() * p.getQuantity())
                    .sum();

            // 这里需要从 User 表获取余额，简化处理
            double balance = 50000.0; // TODO: 从数据库获取真实余额

            String advice = aiService.getTradeAdvice(balance, positions.size(), totalPositionValue);
            
            result.put("code", 200);
            result.put("data", advice);
            result.put("message", "success");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取建议失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取市场分析
     */
    @GetMapping("/market-analysis")
    public Map<String, Object> getMarketAnalysis() {
        Map<String, Object> result = new HashMap<>();
        try {
            String analysis = aiService.getMarketAnalysis();
            result.put("code", 200);
            result.put("data", analysis);
            result.put("message", "success");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取分析失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 持仓分析
     */
    @GetMapping("/portfolio-analysis")
    public Map<String, Object> getPortfolioAnalysis() {
        Long userId = SecurityUtils.getCurrentUserId();
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Position> positions = positionMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Position>()
                            .eq(Position::getUserId, userId)
            );

            // 转换为 Map 列表
            List<Map<String, Object>> positionMaps = positions.stream().map(p -> {
                Map<String, Object> map = new HashMap<>();
                map.put("symbol", p.getSymbol());
                map.put("quantity", p.getQuantity());
                map.put("avgCostPrice", p.getAvgCostPrice().doubleValue());
                return map;
            }).collect(Collectors.toList());

            String analysis = aiService.analyzePortfolio(positionMaps);
            
            result.put("code", 200);
            result.put("data", analysis);
            result.put("message", "success");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "分析失败: " + e.getMessage());
        }
        return result;
    }
}
