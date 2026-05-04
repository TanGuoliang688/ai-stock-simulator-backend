-- ============================================
-- AI模拟炒股大师 - 数据库初始化脚本
-- 版本: v1.0
-- 数据库: MySQL 8.0+
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. 用户表 (user)
-- ============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                        `username` VARCHAR(50) NOT NULL COMMENT '用户名',
                        `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
                        `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希（bcrypt加密）',
                        `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
                        `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
                        `virtual_balance` DECIMAL(15,2) NOT NULL DEFAULT 1000000.00 COMMENT '虚拟资金余额',
                        `total_assets` DECIMAL(15,2) NOT NULL DEFAULT 1000000.00 COMMENT '总资产（余额+持仓市值）',
                        `trading_password` VARCHAR(255) DEFAULT NULL COMMENT '交易密码（单独加密）',
                        `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '账户状态：ACTIVE-正常, FROZEN-冻结, CLOSED-注销',
                        `last_login_at` TIMESTAMP NULL DEFAULT NULL COMMENT '最后登录时间',
                        `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_username` (`username`),
                        UNIQUE KEY `uk_email` (`email`),
                        KEY `idx_user_email` (`email`),
                        KEY `idx_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 股票基础信息表 (stock)
-- ============================================
DROP TABLE IF EXISTS `stock`;
CREATE TABLE `stock` (
                         `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                         `symbol` VARCHAR(20) NOT NULL COMMENT '股票代码（如：600519.SH）',
                         `name` VARCHAR(100) NOT NULL COMMENT '股票名称',
                         `market` VARCHAR(10) NOT NULL COMMENT '市场类型：SH-上交所, SZ-深交所, BJ-北交所',
                         `industry` VARCHAR(50) DEFAULT NULL COMMENT '所属行业',
                         `is_st` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否ST股票：0-否, 1-是',
                         `is_suspended` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否停牌：0-否, 1-是',
                         `listing_date` DATE DEFAULT NULL COMMENT '上市日期',
                         `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uk_symbol` (`symbol`),
                         KEY `idx_stock_symbol` (`symbol`),
                         KEY `idx_stock_name` (`name`),
                         KEY `idx_stock_industry` (`industry`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='股票基础信息表';

-- ============================================
-- 3. 持仓表 (position)
-- ============================================
DROP TABLE IF EXISTS `position`;
CREATE TABLE `position` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `user_id` BIGINT NOT NULL COMMENT '用户ID',
                            `stock_id` BIGINT NOT NULL COMMENT '股票ID',
                            `quantity` INT NOT NULL DEFAULT 0 COMMENT '持仓总数量',
                            `available_quantity` INT NOT NULL DEFAULT 0 COMMENT '可用数量（T+1规则，今日买入的不可用）',
                            `avg_cost` DECIMAL(10,4) NOT NULL DEFAULT 0.0000 COMMENT '平均成本价',
                            `total_cost` DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '总成本',
                            `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_user_stock` (`user_id`, `stock_id`),
                            KEY `idx_position_user_id` (`user_id`),
                            KEY `idx_position_stock_id` (`stock_id`),
                            CONSTRAINT `fk_position_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
                            CONSTRAINT `fk_position_stock` FOREIGN KEY (`stock_id`) REFERENCES `stock` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='持仓表';

-- ============================================
-- 4. 交易记录表 (transaction)
-- ============================================
DROP TABLE IF EXISTS `transaction`;
CREATE TABLE `transaction` (
                               `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                               `order_no` VARCHAR(32) NOT NULL COMMENT '订单号（唯一，幂等性校验）',
                               `user_id` BIGINT NOT NULL COMMENT '用户ID',
                               `stock_id` BIGINT NOT NULL COMMENT '股票ID',
                               `type` VARCHAR(10) NOT NULL COMMENT '交易类型：BUY-买入, SELL-卖出',
                               `quantity` INT NOT NULL COMMENT '交易数量',
                               `price` DECIMAL(10,4) NOT NULL COMMENT '成交价格',
                               `amount` DECIMAL(15,2) NOT NULL COMMENT '成交金额',
                               `fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '手续费（佣金+印花税）',
                               `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '订单状态：PENDING-待成交, FILLED-已成交, CANCELLED-已撤单, REJECTED-已拒绝',
                               `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
                               `trade_time` TIMESTAMP NULL DEFAULT NULL COMMENT '成交时间',
                               `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uk_order_no` (`order_no`),
                               KEY `idx_transaction_user_id` (`user_id`),
                               KEY `idx_transaction_stock_id` (`stock_id`),
                               KEY `idx_transaction_order_no` (`order_no`),
                               KEY `idx_transaction_created_at` (`created_at`),
                               KEY `idx_transaction_status` (`status`),
                               CONSTRAINT `fk_transaction_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
                               CONSTRAINT `fk_transaction_stock` FOREIGN KEY (`stock_id`) REFERENCES `stock` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易记录表';

-- ============================================
-- 5. 历史价格表 (price_history)
-- ============================================
DROP TABLE IF EXISTS `price_history`;
CREATE TABLE `price_history` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `stock_id` BIGINT NOT NULL COMMENT '股票ID',
                                 `trade_date` DATE NOT NULL COMMENT '交易日期',
                                 `open_price` DECIMAL(10,4) NOT NULL COMMENT '开盘价',
                                 `high_price` DECIMAL(10,4) NOT NULL COMMENT '最高价',
                                 `low_price` DECIMAL(10,4) NOT NULL COMMENT '最低价',
                                 `close_price` DECIMAL(10,4) NOT NULL COMMENT '收盘价',
                                 `volume` BIGINT NOT NULL DEFAULT 0 COMMENT '成交量（手）',
                                 `turnover` DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '成交额（元）',
                                 `adjusted_close` DECIMAL(10,4) DEFAULT NULL COMMENT '复权收盘价',
                                 `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_stock_date` (`stock_id`, `trade_date`),
                                 KEY `idx_price_history_stock_id` (`stock_id`),
                                 KEY `idx_price_history_trade_date` (`trade_date`),
                                 KEY `idx_price_history_stock_date` (`stock_id`, `trade_date`),
                                 CONSTRAINT `fk_price_history_stock` FOREIGN KEY (`stock_id`) REFERENCES `stock` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='历史价格表（K线数据）';

-- ============================================
-- 6. 自选股表 (watchlist)
-- ============================================
DROP TABLE IF EXISTS `watchlist`;
CREATE TABLE `watchlist` (
                             `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                             `user_id` BIGINT NOT NULL COMMENT '用户ID',
                             `stock_id` BIGINT NOT NULL COMMENT '股票ID',
                             `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序序号',
                             `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             PRIMARY KEY (`id`),
                             UNIQUE KEY `uk_user_stock` (`user_id`, `stock_id`),
                             KEY `idx_watchlist_user_id` (`user_id`),
                             KEY `idx_watchlist_stock_id` (`stock_id`),
                             CONSTRAINT `fk_watchlist_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
                             CONSTRAINT `fk_watchlist_stock` FOREIGN KEY (`stock_id`) REFERENCES `stock` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自选股表';

-- ============================================
-- 7. 策略表 (strategy)
-- ============================================
DROP TABLE IF EXISTS `strategy`;
CREATE TABLE `strategy` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                            `user_id` BIGINT NOT NULL COMMENT '用户ID',
                            `name` VARCHAR(100) NOT NULL COMMENT '策略名称',
                            `description` TEXT COMMENT '策略描述',
                            `strategy_type` VARCHAR(50) NOT NULL COMMENT '策略类型：MA_CROSS-均线交叉, MACD, RSI, CUSTOM-自定义',
                            `parameters` JSON NOT NULL DEFAULT (JSON_OBJECT()) COMMENT '策略参数（JSON格式）',
                            `stock_pool` JSON DEFAULT NULL COMMENT '股票池（JSON数组）',
                            `is_public` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否公开：0-否, 1-是',
                            `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-启用, INACTIVE-停用',
                            `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            KEY `idx_strategy_user_id` (`user_id`),
                            KEY `idx_strategy_type` (`strategy_type`),
                            CONSTRAINT `fk_strategy_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='策略表';

-- ============================================
-- 8. 回测结果表 (backtest_result)
-- ============================================
DROP TABLE IF EXISTS `backtest_result`;
CREATE TABLE `backtest_result` (
                                   `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                   `strategy_id` BIGINT NOT NULL COMMENT '策略ID',
                                   `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                   `start_date` DATE NOT NULL COMMENT '回测开始日期',
                                   `end_date` DATE NOT NULL COMMENT '回测结束日期',
                                   `initial_capital` DECIMAL(15,2) NOT NULL COMMENT '初始资金',
                                   `final_capital` DECIMAL(15,2) NOT NULL COMMENT '最终资金',
                                   `total_return` DECIMAL(10,4) NOT NULL COMMENT '总收益率',
                                   `annualized_return` DECIMAL(10,4) DEFAULT NULL COMMENT '年化收益率',
                                   `max_drawdown` DECIMAL(10,4) DEFAULT NULL COMMENT '最大回撤',
                                   `sharpe_ratio` DECIMAL(10,4) DEFAULT NULL COMMENT '夏普比率',
                                   `win_rate` DECIMAL(10,4) DEFAULT NULL COMMENT '胜率',
                                   `trade_count` INT DEFAULT 0 COMMENT '交易次数',
                                   `profit_factor` DECIMAL(10,4) DEFAULT NULL COMMENT '盈亏比',
                                   `execution_time` INT DEFAULT NULL COMMENT '执行耗时（毫秒）',
                                   `report_data` JSON DEFAULT NULL COMMENT '详细报告数据（JSON）',
                                   `status` VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' COMMENT '状态：RUNNING-运行中, COMPLETED-已完成, FAILED-失败',
                                   `error_message` TEXT COMMENT '错误信息',
                                   `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_backtest_result_strategy_id` (`strategy_id`),
                                   KEY `idx_backtest_result_user_id` (`user_id`),
                                   KEY `idx_backtest_result_created_at` (`created_at`),
                                   CONSTRAINT `fk_backtest_result_strategy` FOREIGN KEY (`strategy_id`) REFERENCES `strategy` (`id`) ON DELETE CASCADE,
                                   CONSTRAINT `fk_backtest_result_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回测结果表';

-- ============================================
-- 插入测试数据（可选）
-- ============================================
INSERT INTO `stock` (`symbol`, `name`, `market`, `industry`, `is_st`) VALUES
                                                                          ('600519.SH', '贵州茅台', 'SH', '白酒', 0),
                                                                          ('000858.SZ', '五粮液', 'SZ', '白酒', 0),
                                                                          ('600036.SH', '招商银行', 'SH', '银行', 0),
                                                                          ('000001.SZ', '平安银行', 'SZ', '银行', 0),
                                                                          ('601318.SH', '中国平安', 'SH', '保险', 0);

SET FOREIGN_KEY_CHECKS = 1;


-- ... existing code ...

SHOW CREATE DATABASE `ai-stock-simulator`;

-- 交易订单表
CREATE TABLE trade_order (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             user_id BIGINT NOT NULL COMMENT '用户ID',
                             stock_id BIGINT NOT NULL COMMENT '股票ID',
                             symbol VARCHAR(20) NOT NULL COMMENT '股票代码',
                             order_type VARCHAR(10) NOT NULL COMMENT '订单类型：BUY/SELL',
                             price DECIMAL(10, 2) NOT NULL COMMENT '委托价格',
                             quantity INT NOT NULL COMMENT '委托数量',
                             filled_quantity INT DEFAULT 0 COMMENT '已成交数量',
                             total_amount DECIMAL(15, 2) NOT NULL COMMENT '总金额',
                             commission DECIMAL(10, 2) DEFAULT 0.00 COMMENT '手续费',
                             status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/FILLED/CANCELLED',
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             INDEX idx_user_id (user_id),
                             INDEX idx_stock_id (stock_id),
                             INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单表';

-- 持仓表
CREATE TABLE position (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          user_id BIGINT NOT NULL COMMENT '用户ID',
                          stock_id BIGINT NOT NULL COMMENT '股票ID',
                          symbol VARCHAR(20) NOT NULL COMMENT '股票代码',
                          quantity INT NOT NULL DEFAULT 0 COMMENT '持仓数量',
                          available_quantity INT NOT NULL DEFAULT 0 COMMENT '可用数量',
                          avg_cost_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '平均成本价',
                          total_cost DECIMAL(15, 2) NOT NULL DEFAULT 0.00 COMMENT '总成本',
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          UNIQUE KEY uk_user_stock (user_id, stock_id),
                          INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='持仓表';

-- 交易记录表（历史成交）
CREATE TABLE trade_record (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              user_id BIGINT NOT NULL COMMENT '用户ID',
                              order_id BIGINT NOT NULL COMMENT '订单ID',
                              stock_id BIGINT NOT NULL COMMENT '股票ID',
                              symbol VARCHAR(20) NOT NULL COMMENT '股票代码',
                              trade_type VARCHAR(10) NOT NULL COMMENT '交易类型：BUY/SELL',
                              price DECIMAL(10, 2) NOT NULL COMMENT '成交价格',
                              quantity INT NOT NULL COMMENT '成交数量',
                              total_amount DECIMAL(15, 2) NOT NULL COMMENT '成交金额',
                              commission DECIMAL(10, 2) DEFAULT 0.00 COMMENT '手续费',
                              trade_time DATETIME NOT NULL COMMENT '成交时间',
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              INDEX idx_user_id (user_id),
                              INDEX idx_order_id (order_id),
                              INDEX idx_trade_time (trade_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易记录表';

DESCRIBE position;

-- 1. 删除旧表
DROP TABLE IF EXISTS trade_record;
DROP TABLE IF EXISTS trade_order;
DROP TABLE IF EXISTS position;

-- 2. 重新创建 position 表（正确的结构）
CREATE TABLE position (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          user_id BIGINT NOT NULL COMMENT '用户ID',
                          stock_id BIGINT NOT NULL COMMENT '股票ID',
                          symbol VARCHAR(20) NOT NULL COMMENT '股票代码',
                          quantity INT NOT NULL DEFAULT 0 COMMENT '持仓数量',
                          available_quantity INT NOT NULL DEFAULT 0 COMMENT '可用数量',
                          avg_cost_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '平均成本价',
                          total_cost DECIMAL(15, 2) NOT NULL DEFAULT 0.00 COMMENT '总成本',
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          UNIQUE KEY uk_user_stock (user_id, stock_id),
                          INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='持仓表';

-- 3. 创建 trade_order 表
CREATE TABLE trade_order (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             user_id BIGINT NOT NULL COMMENT '用户ID',
                             stock_id BIGINT NOT NULL COMMENT '股票ID',
                             symbol VARCHAR(20) NOT NULL COMMENT '股票代码',
                             order_type VARCHAR(10) NOT NULL COMMENT '订单类型：BUY/SELL',
                             price DECIMAL(10, 2) NOT NULL COMMENT '委托价格',
                             quantity INT NOT NULL COMMENT '委托数量',
                             filled_quantity INT DEFAULT 0 COMMENT '已成交数量',
                             total_amount DECIMAL(15, 2) NOT NULL COMMENT '总金额',
                             commission DECIMAL(10, 2) DEFAULT 0.00 COMMENT '手续费',
                             status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/FILLED/CANCELLED',
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             INDEX idx_user_id (user_id),
                             INDEX idx_stock_id (stock_id),
                             INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单表';

-- 4. 创建 trade_record 表
CREATE TABLE trade_record (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              user_id BIGINT NOT NULL COMMENT '用户ID',
                              order_id BIGINT NOT NULL COMMENT '订单ID',
                              stock_id BIGINT NOT NULL COMMENT '股票ID',
                              symbol VARCHAR(20) NOT NULL COMMENT '股票代码',
                              trade_type VARCHAR(10) NOT NULL COMMENT '交易类型：BUY/SELL',
                              price DECIMAL(10, 2) NOT NULL COMMENT '成交价格',
                              quantity INT NOT NULL COMMENT '成交数量',
                              total_amount DECIMAL(15, 2) NOT NULL COMMENT '成交金额',
                              commission DECIMAL(10, 2) DEFAULT 0.00 COMMENT '手续费',
                              trade_time DATETIME NOT NULL COMMENT '成交时间',
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              INDEX idx_user_id (user_id),
                              INDEX idx_order_id (order_id),
                              INDEX idx_trade_time (trade_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易记录表';

