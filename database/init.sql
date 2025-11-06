-- Create database
CREATE DATABASE IF NOT EXISTS seckill_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE seckill_db;

-- User table
CREATE TABLE IF NOT EXISTS `t_user` (
  `id` BIGINT NOT NULL PRIMARY KEY,
  `username` VARCHAR(64) NOT NULL UNIQUE COMMENT 'Username',
  `password` VARCHAR(128) NOT NULL COMMENT 'Encrypted password',
  `phone` VARCHAR(16) COMMENT 'Phone number',
  `score` INT DEFAULT 0 COMMENT 'User score for seckill eligibility',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  INDEX idx_username (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User table';

-- Product table
CREATE TABLE IF NOT EXISTS `t_product` (
  `id` BIGINT NOT NULL PRIMARY KEY,
  `name` VARCHAR(128) NOT NULL COMMENT 'Product name',
  `price` DECIMAL(10, 2) NOT NULL COMMENT 'Normal price',
  `stock` INT NOT NULL DEFAULT 0 COMMENT 'Total stock',
  `description` TEXT COMMENT 'Product description',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 1=active, 0=inactive',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Product table';

-- Seckill activity table
CREATE TABLE IF NOT EXISTS `t_seckill_activity` (
  `id` BIGINT NOT NULL PRIMARY KEY,
  `product_id` BIGINT NOT NULL COMMENT 'Product ID',
  `seckill_price` DECIMAL(10, 2) NOT NULL COMMENT 'Seckill price',
  `seckill_stock` INT NOT NULL DEFAULT 0 COMMENT 'Seckill stock',
  `start_time` DATETIME NOT NULL COMMENT 'Start time',
  `end_time` DATETIME NOT NULL COMMENT 'End time',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT 'Status: 1=active, 0=not started, -1=ended',
  `version` INT NOT NULL DEFAULT 0 COMMENT 'Optimistic lock version',
  INDEX idx_product_id (`product_id`),
  INDEX idx_time_range (`start_time`, `end_time`),
  FOREIGN KEY (`product_id`) REFERENCES `t_product`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seckill activity table';

-- Seckill order table (for seckill participation record)
CREATE TABLE IF NOT EXISTS `t_seckill_order` (
  `id` BIGINT NOT NULL PRIMARY KEY,
  `user_id` BIGINT NOT NULL COMMENT 'User ID',
  `activity_id` BIGINT NOT NULL COMMENT 'Seckill activity ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  UNIQUE KEY uk_user_activity (`user_id`, `activity_id`),
  INDEX idx_user_id (`user_id`),
  INDEX idx_activity_id (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seckill order table';

-- Order table
CREATE TABLE IF NOT EXISTS `t_order` (
  `id` BIGINT NOT NULL PRIMARY KEY,
  `order_sn` VARCHAR(64) NOT NULL UNIQUE COMMENT 'Order serial number',
  `user_id` BIGINT NOT NULL COMMENT 'User ID',
  `product_id` BIGINT NOT NULL COMMENT 'Product ID',
  `seckill_activity_id` BIGINT NOT NULL COMMENT 'Seckill activity ID',
  `seckill_price` DECIMAL(10, 2) NOT NULL COMMENT 'Seckill price',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT 'Status: 0=pending payment, 1=paid, 2=cancelled',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
  `pay_time` DATETIME COMMENT 'Payment time',
  INDEX idx_order_sn (`order_sn`),
  INDEX idx_user_id (`user_id`),
  INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Order table';

-- Insert test data

-- Test users
INSERT INTO `t_user` (`id`, `username`, `password`, `phone`, `score`) VALUES
(1, 'testuser1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '13800138001', 100),
(2, 'testuser2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '13800138002', 200),
(3, 'testuser3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '13800138003', 150);

-- Test products
INSERT INTO `t_product` (`id`, `name`, `price`, `stock`, `description`, `status`) VALUES
(1, 'iPhone 15 Pro Max', 9999.00, 1000, 'Latest flagship smartphone from Apple', 1),
(2, 'MacBook Pro M3', 19999.00, 500, 'Professional laptop with M3 chip', 1),
(3, 'AirPods Pro 2', 1999.00, 2000, 'Wireless earbuds with active noise cancellation', 1),
(4, 'iPad Air', 4999.00, 800, 'Powerful and versatile tablet', 1),
(5, 'Apple Watch Ultra 2', 6299.00, 600, 'Premium smartwatch for outdoor adventures', 1);

-- Test seckill activities (adjust times to be current)
INSERT INTO `t_seckill_activity` (`id`, `product_id`, `seckill_price`, `seckill_stock`, `start_time`, `end_time`, `status`, `version`) VALUES
(1, 1, 7999.00, 100, DATE_ADD(NOW(), INTERVAL -1 HOUR), DATE_ADD(NOW(), INTERVAL 2 HOUR), 1, 0),
(2, 2, 15999.00, 50, DATE_ADD(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 4 HOUR), 0, 0),
(3, 3, 1499.00, 200, DATE_ADD(NOW(), INTERVAL -30 MINUTE), DATE_ADD(NOW(), INTERVAL 1 HOUR), 1, 0),
(4, 4, 3999.00, 80, DATE_ADD(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 5 HOUR), 0, 0),
(5, 5, 4999.00, 60, DATE_ADD(NOW(), INTERVAL -2 HOUR), DATE_ADD(NOW(), INTERVAL 1 HOUR), 1, 0);

COMMIT;
