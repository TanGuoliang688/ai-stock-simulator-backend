
package com.tangl.aistocksimulatorbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${ai.qwen.api-key}")
    private String apiKey;

    @Value("${ai.qwen.api-url}")
    private String apiUrl;

    @Value("${ai.qwen.model}")
    private String model;

    /**
     * 智能选股推荐
     */
    public String getStockRecommendations(String marketTrend) {
        String prompt = "作为专业的股票分析师，请根据当前市场情况推荐3-5只值得关注的A股股票。\n" +
                "市场趋势：" + marketTrend + "\n\n" +
                "要求：\n" +
                "1. 用自然语言描述，不要使用JSON格式\n" +
                "2. 每只股票包含：股票代码、名称、推荐理由、建议操作（买入/卖出/持有）、置信度（0-100）\n" +
                "3. 使用Markdown格式，让内容更清晰\n" +
                "4. 用简洁的中文回答";

        return callQwenAPI(prompt);
    }

    /**
     * 交易建议
     */
    public String getTradeAdvice(double balance, int positionCount, double totalPositionValue) {
        String prompt = "作为专业的投资顾问，请根据以下用户持仓情况给出交易建议：\n" +
                "- 可用资金：¥" + balance + "\n" +
                "- 持仓数量：" + positionCount + " 只\n" +
                "- 持仓总市值：¥" + totalPositionValue + "\n\n" +
                "要求：\n" +
                "1. 用自然语言描述，不要使用JSON格式\n" +
                "2. 请给出：整体建议、风险等级（低/中/高）、具体操作建议（3-5条）\n" +
                "3. 使用Markdown格式\n" +
                "4. 用简洁的中文回答";

        return callQwenAPI(prompt);
    }

    /**
     * 市场分析
     */
    public String getMarketAnalysis() {
        String prompt = "作为专业的市场分析师，请分析当前A股市场情况。\n\n" +
                "要求：\n" +
                "1. 用自然语言描述，不要使用JSON格式\n" +
                "2. 包含：市场整体趋势总结、主要板块表现（3-5个趋势）、投资机会（3-5个）、风险提示（3-5个）\n" +
                "3. 使用Markdown格式\n" +
                "4. 用简洁的中文回答";

        return callQwenAPI(prompt);
    }

    /**
     * 持仓分析
     */
    public String analyzePortfolio(List<Map<String, Object>> positions) {
        StringBuilder prompt = new StringBuilder("作为专业的投资组合分析师，请分析以下持仓：\n\n");

        double totalValue = 0;
        for (Map<String, Object> pos : positions) {
            String symbol = (String) pos.get("symbol");
            Integer quantity = (Integer) pos.get("quantity");
            Double avgCost = (Double) pos.get("avgCostPrice");
            double value = quantity * avgCost;
            totalValue += value;

            prompt.append(String.format("- %s: %d股 @ ¥%.2f (市值 ¥%.2f)\n",
                    symbol, quantity, avgCost, value));
        }

        prompt.append("\n总持仓市值：¥").append(String.format("%.2f", totalValue)).append("\n");
        prompt.append("持仓数量：").append(positions.size()).append(" 只\n\n");
        prompt.append("要求：\n");
        prompt.append("1. 用自然语言描述，不要使用JSON格式\n");
        prompt.append("2. 请给出：持仓概况、风险评估、优化建议\n");
        prompt.append("3. 使用Markdown格式\n");
        prompt.append("4. 用简洁的中文回答");

        return callQwenAPI(prompt.toString());
    }

    /**
     * 调用通义千问 API
     */
    private String callQwenAPI(String prompt) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);

            Map<String, Object> input = new HashMap<>();
            List<Map<String, String>> messages = List.of(
                    Map.of("role", "system", "content", "你是一个专业的股票分析师，提供客观、理性的投资建议。"),
                    Map.of("role", "user", "content", prompt)
            );
            input.put("messages", messages);
            requestBody.put("input", input);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("result_format", "message");
            requestBody.put("parameters", parameters);

            // 调用 API
            String response = webClientBuilder.build()
                    .post()
                    .uri(apiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // 解析响应
            if (response != null) {
                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode outputNode = rootNode.path("output");
                JsonNode choicesNode = outputNode.path("choices");

                if (choicesNode.isArray() && choicesNode.size() > 0) {
                    JsonNode messageNode = choicesNode.get(0).path("message");
                    return messageNode.path("content").asText();
                }
            }

            return "AI 分析暂时不可用，请稍后重试。";

        } catch (Exception e) {
            log.error("调用 AI API 失败", e);
            return "AI 分析暂时不可用，请稍后重试。";
        }
    }
}
