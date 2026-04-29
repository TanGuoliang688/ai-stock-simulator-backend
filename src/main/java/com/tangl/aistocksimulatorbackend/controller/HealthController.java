package com.tangl.aistocksimulatorbackend.controller;

import com.rabbitmq.client.Channel;
import com.tangl.aistocksimulatorbackend.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    @GetMapping
    public Result<Map<String, Object>> health() {
        Map<String, Object> healthStatus = new HashMap<>();
        boolean allHealthy = true;

        // 检查 MySQL 连接
        try {
            jdbcTemplate.execute("SELECT 1");
            healthStatus.put("mysql", Map.of(
                    "status", "UP",
                    "message", "MySQL connection successful"
            ));
            log.info("✅ MySQL 连接正常");
        } catch (Exception e) {
            healthStatus.put("mysql", Map.of(
                    "status", "DOWN",
                    "message", "MySQL connection failed: " + e.getMessage()
            ));
            allHealthy = false;
            log.error("❌ MySQL 连接失败", e);
        }

        // 检查 Redis 连接
        try {
            redisTemplate.opsForValue().set("health_check", "ok");
            String value = (String) redisTemplate.opsForValue().get("health_check");
            redisTemplate.delete("health_check");

            if ("ok".equals(value)) {
                healthStatus.put("redis", Map.of(
                        "status", "UP",
                        "message", "Redis connection successful"
                ));
                log.info("✅ Redis 连接正常");
            } else {
                throw new RuntimeException("Redis read/write mismatch");
            }
        } catch (Exception e) {
            healthStatus.put("redis", Map.of(
                    "status", "DOWN",
                    "message", "Redis connection failed: " + e.getMessage()
            ));
            allHealthy = false;
            log.error("❌ Redis 连接失败", e);
        }

        // 检查 RabbitMQ 连接
        try {
            rabbitTemplate.execute((Channel channel) -> {
                channel.queueDeclare("health_check_queue", true, false, false, null);
                channel.queueDelete("health_check_queue");
                return null;
            });
            healthStatus.put("rabbitmq", Map.of(
                    "status", "UP",
                    "message", "RabbitMQ connection successful"
            ));
            log.info("✅ RabbitMQ 连接正常");
        } catch (Exception e) {
            healthStatus.put("rabbitmq", Map.of(
                    "status", "DOWN",
                    "message", "RabbitMQ connection failed: " + e.getMessage()
            ));
            allHealthy = false;
            log.error("❌ RabbitMQ 连接失败", e);
        }

        // 总体状态
        healthStatus.put("overall", allHealthy ? "UP" : "DOWN");

        return allHealthy
                ? Result.success("All services are healthy", healthStatus)
                : Result.error(503, "Some services are unavailable");
    }

    @GetMapping("/detail")
    public Result<Map<String, Object>> healthDetail() {
        Map<String, Object> detail = new HashMap<>();

        // MySQL 详细信息
        try {
            Map<String, Object> mysqlInfo = new HashMap<>();
            String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);
            mysqlInfo.put("version", version);
            mysqlInfo.put("status", "UP");
            detail.put("mysql", mysqlInfo);
        } catch (Exception e) {
            detail.put("mysql", Map.of("status", "DOWN", "error", e.getMessage()));
        }

        // Redis 详细信息
        try {
            Map<String, Object> redisInfo = new HashMap<>();
            Properties info = redisTemplate.execute((RedisConnection connection) ->
                    connection.info()
            );
            if (info != null) {
                redisInfo.put("version", info.getProperty("redis_version"));
            } else {
                redisInfo.put("version", "unknown");
            }
            redisInfo.put("status", "UP");
            detail.put("redis", redisInfo);
        } catch (Exception e) {
            detail.put("redis", Map.of("status", "DOWN", "error", e.getMessage()));
        }

        // RabbitMQ 详细信息
        try {
            Map<String, Object> rabbitmqInfo = new HashMap<>();
            rabbitmqInfo.put("status", "UP");
            rabbitmqInfo.put("broker", rabbitTemplate.getConnectionFactory().getHost());
            rabbitmqInfo.put("port", rabbitTemplate.getConnectionFactory().getPort());
            detail.put("rabbitmq", rabbitmqInfo);
        } catch (Exception e) {
            detail.put("rabbitmq", Map.of("status", "DOWN", "error", e.getMessage()));
        }

        return Result.success(detail);
    }
}
