# 秒杀业务系统重现项目设计文档

## 1. 架构总览

本项目旨在重现用户简历中描述的基于微服务架构的秒杀系统。为实现**简单部署**的目标，所有服务及中间件将通过 **Docker Compose** 进行容器化部署。

**技术栈概览：**

| 类别 | 技术栈 | 用途 |
| :--- | :--- | :--- |
| **后端核心** | Spring Cloud, Spring Boot | 微服务开发框架 |
| **服务治理** | Nacos | 服务注册与发现、配置中心 |
| **数据库** | MySQL | 持久化存储（用户、商品、订单） |
| **缓存/分布式锁** | Redis, Redisson | 缓存、分布式锁、异步秒杀（Stream） |
| **消息队列** | RabbitMQ | 异步处理、延迟队列（订单自动取消） |
| **搜索** | ElasticSearch | 商品搜索优化 |
| **安全** | JWT, Spring Security | 身份认证与授权 |
| **前端** | Vue 3 + Vite | 用户界面 |
| **部署** | Docker, Docker Compose | 容器化部署 |

## 2. 微服务划分与职责

系统将划分为以下微服务：

| 服务名称 | 端口 | 职责描述 |
| :--- | :--- | :--- |
| `seckill-gateway` | 8080 | API网关，负责路由、鉴权、限流。 |
| `seckill-auth` | 8081 | 用户认证服务：注册、登录（JWT生成与校验）。 |
| `seckill-product` | 8082 | 商品服务：商品信息管理、库存管理、ES同步、搜索接口。 |
| `seckill-seckill` | 8083 | 秒杀核心服务：秒杀资格校验、秒杀下单、异步处理（Redis Stream）。 |
| `seckill-order` | 8084 | 订单服务：创建订单、订单查询、订单状态更新、延迟队列（订单取消）。 |

## 3. 数据库设计（MySQL）

核心实体表设计如下：

### `t_user` (用户表)

| 字段名 | 数据类型 | 描述 |
| :--- | :--- | :--- |
| `id` | BIGINT | 主键，用户ID |
| `username` | VARCHAR(64) | 用户名 |
| `password` | VARCHAR(128) | 密码（加密存储） |
| `phone` | VARCHAR(16) | 手机号 |
| `score` | INT | 积分（用于秒杀资格） |
| `create_time` | DATETIME | 创建时间 |

### `t_product` (商品表)

| 字段名 | 数据类型 | 描述 |
| :--- | :--- | :--- |
| `id` | BIGINT | 主键，商品ID |
| `name` | VARCHAR(128) | 商品名称 |
| `price` | DECIMAL(10, 2) | 正常价格 |
| `stock` | INT | 总库存 |
| `description` | TEXT | 商品描述 |
| `status` | TINYINT | 状态（1:上架, 0:下架） |
| `create_time` | DATETIME | 创建时间 |

### `t_seckill_activity` (秒杀活动表)

| 字段名 | 数据类型 | 描述 |
| :--- | :--- | :--- |
| `id` | BIGINT | 主键，活动ID |
| `product_id` | BIGINT | 关联商品ID |
| `seckill_price` | DECIMAL(10, 2) | 秒杀价格 |
| `seckill_stock` | INT | 秒杀库存 |
| `start_time` | DATETIME | 开始时间 |
| `end_time` | DATETIME | 结束时间 |
| `status` | TINYINT | 状态（1:进行中, 0:未开始, -1:已结束） |
| `version` | INT | 乐观锁版本号 |

### `t_order` (订单表)

| 字段名 | 数据类型 | 描述 |
| :--- | :--- | :--- |
| `id` | BIGINT | 主键，订单ID |
| `order_sn` | VARCHAR(64) | 订单编号 |
| `user_id` | BIGINT | 用户ID |
| `product_id` | BIGINT | 商品ID |
| `seckill_activity_id` | BIGINT | 秒杀活动ID |
| `seckill_price` | DECIMAL(10, 2) | 秒杀时价格 |
| `status` | TINYINT | 订单状态（0:待支付, 1:已支付, 2:已取消） |
| `create_time` | DATETIME | 创建时间 |
| `pay_time` | DATETIME | 支付时间 |

## 4. 核心API接口设计

### 4.1. 用户认证 (`seckill-auth`)

| 接口 | 方法 | 路径 | 描述 |
| :--- | :--- | :--- | :--- |
| **用户注册** | POST | `/auth/register` | 用户注册，包含验证码校验。 |
| **用户登录** | POST | `/auth/login` | 用户登录，成功返回JWT Token。 |
| **获取用户信息** | GET | `/auth/info` | 根据Token获取用户基本信息。 |

### 4.2. 商品服务 (`seckill-product`)

| 接口 | 方法 | 路径 | 描述 |
| :--- | :--- | :--- | :--- |
| **商品搜索** | GET | `/product/search` | 基于ES的商品搜索、分页、高亮。 |
| **商品详情** | GET | `/product/{id}` | 获取商品详情。 |
| **秒杀列表** | GET | `/product/seckill/list` | 获取当前正在进行或即将开始的秒杀活动列表。 |

### 4.3. 秒杀核心 (`seckill-seckill`)

| 接口 | 方法 | 路径 | 描述 |
| :--- | :--- | :--- | :--- |
| **获取秒杀地址** | GET | `/seckill/path/{activityId}` | 隐藏秒杀地址，防止恶意请求。 |
| **执行秒杀** | POST | `/seckill/doSeckill/{path}` | 提交秒杀请求，使用Redis Stream异步处理。 |
| **查询秒杀结果** | GET | `/seckill/result/{activityId}` | 查询秒杀结果（排队中/成功/失败）。 |

### 4.4. 订单服务 (`seckill-order`)

| 接口 | 方法 | 路径 | 描述 |
| :--- | :--- | :--- | :--- |
| **获取订单详情** | GET | `/order/{orderSn}` | 根据订单编号获取订单详情。 |
| **模拟支付** | POST | `/order/pay/{orderSn}` | 模拟支付，更新订单状态。 |

## 5. 关键技术点实现方案

| 关键点 | 实现方案 | 涉及服务 |
| :--- | :--- | :--- |
| **高并发防超卖** | 数据库乐观锁 + Redisson分布式锁 + Redis预减库存 | `seckill-seckill` |
| **异步秒杀** | Redis Stream 作为消息队列，异步创建订单 | `seckill-seckill` |
| **订单自动取消** | RabbitMQ 延迟队列（TTL或延迟插件） | `seckill-order` |
| **热点数据隔离** | Redis二级缓存（商品详情、秒杀活动信息） | `seckill-product`, `seckill-seckill` |
| **缓存穿透/击穿/雪崩** | 布隆过滤器（防穿透）、互斥锁（防击穿）、缓存过期时间分散 | `seckill-product`, `seckill-seckill` |
| **商品搜索** | ElasticSearch + Logstash/Canal（模拟）同步MySQL数据 | `seckill-product` |
| **登录认证** | JWT Token + ThreadLocal传递用户上下文 | `seckill-auth`, `seckill-gateway` |
