package com.tangl.aistocksimulatorbackend.task;

import com.tangl.aistocksimulatorbackend.service.StockPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
@Slf4j
@Component
@RequiredArgsConstructor
public class PriceUpdateTask {

    private final StockPriceService stockPriceService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 应用启动时初始化价格
     */
    @PostConstruct
    public void init() {
        stockPriceService.initializePrices();
    }

    /**
     * 每3秒更新一次价格并推送
     */
    @Scheduled(fixedRate = 3000)
    public void updatePrices() {
        try {
            stockPriceService.updateAllPrices();

            // 推送最新价格到前端
            messagingTemplate.convertAndSend("/topic/prices", stockPriceService.getAllPrices());
        } catch (Exception e) {
            log.error("更新股票价格失败", e);
        }
    }
}
