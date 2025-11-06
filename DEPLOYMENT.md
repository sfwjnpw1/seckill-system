# 秒杀系统部署文档

本文档详细说明了秒杀系统的部署流程和注意事项。

## 部署环境要求

### 硬件要求

| 资源 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 4核 | 8核+ |
| 内存 | 8GB | 16GB+ |
| 磁盘 | 20GB | 50GB+ |
| 网络 | 10Mbps | 100Mbps+ |

### 软件要求

| 软件 | 版本要求 | 安装说明 |
|------|----------|----------|
| Docker | 20.10+ | [安装Docker](https://docs.docker.com/get-docker/) |
| Docker Compose | 2.0+ | [安装Docker Compose](https://docs.docker.com/compose/install/) |
| Git | 2.0+ | 用于克隆项目代码 |

### 操作系统支持

- Linux (Ubuntu 20.04+, CentOS 7+)
- macOS 10.15+
- Windows 10+ (需要WSL2)

## 部署步骤

### 1. 准备工作

#### 1.1 克隆项目

```bash
git clone <repository-url>
cd seckill_project
```

#### 1.2 检查Docker环境

```bash
# 检查Docker版本
docker --version

# 检查Docker Compose版本
docker-compose --version

# 检查Docker服务状态
docker ps
```

#### 1.3 配置环境变量（可选）

如需修改默认配置，可以创建 `.env` 文件：

```bash
# MySQL配置
MYSQL_ROOT_PASSWORD=root123456
MYSQL_DATABASE=seckill_db

# Redis配置
REDIS_PASSWORD=redis123456

# RabbitMQ配置
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest
```

### 2. 构建和启动

#### 2.1 使用一键启动脚本（推荐）

```bash
chmod +x start.sh
./start.sh
```

启动脚本会自动完成以下操作：
1. 检查Docker环境
2. 构建所有微服务镜像
3. 启动所有容器
4. 等待服务就绪

#### 2.2 手动启动（高级用户）

```bash
# 构建镜像
docker-compose build

# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 3. 验证部署

#### 3.1 检查服务状态

```bash
docker-compose ps
```

所有服务的状态应该显示为 `Up`。

#### 3.2 检查服务健康状态

```bash
# 检查MySQL
docker-compose exec mysql mysqladmin ping -h localhost -uroot -proot123456

# 检查Redis
docker-compose exec redis redis-cli -a redis123456 ping

# 检查RabbitMQ
curl http://localhost:15672

# 检查Nacos
curl http://localhost:8848/nacos/

# 检查ElasticSearch
curl http://localhost:9200/_cluster/health
```

#### 3.3 访问系统

| 服务 | URL | 说明 |
|------|-----|------|
| 前端 | http://localhost | 用户界面 |
| API网关 | http://localhost:8080 | 后端API |
| Nacos | http://localhost:8848/nacos | 用户名/密码: nacos/nacos |
| RabbitMQ | http://localhost:15672 | 用户名/密码: guest/guest |

### 4. 初始化数据

数据库初始化脚本会在MySQL容器启动时自动执行。如需手动执行：

```bash
docker-compose exec mysql mysql -uroot -proot123456 seckill_db < /docker-entrypoint-initdb.d/init.sql
```

## 服务端口说明

| 服务 | 内部端口 | 外部端口 | 说明 |
|------|----------|----------|------|
| MySQL | 3306 | 3306 | 数据库 |
| Redis | 6379 | 6379 | 缓存 |
| RabbitMQ | 5672 | 5672 | 消息队列 |
| RabbitMQ管理 | 15672 | 15672 | 管理控制台 |
| Nacos | 8848 | 8848 | 服务注册中心 |
| ElasticSearch | 9200 | 9200 | 搜索引擎 |
| Gateway | 8080 | 8080 | API网关 |
| Auth Service | 8081 | 8081 | 认证服务 |
| Product Service | 8082 | 8082 | 商品服务 |
| Seckill Service | 8083 | 8083 | 秒杀服务 |
| Order Service | 8084 | 8084 | 订单服务 |
| Frontend | 80 | 80 | 前端服务 |

## 常见问题排查

### 问题1：服务启动失败

**症状：**容器启动后立即退出

**排查步骤：**

```bash
# 查看容器日志
docker-compose logs <service-name>

# 查看容器状态
docker-compose ps

# 重启服务
docker-compose restart <service-name>
```

**常见原因：**
- 端口被占用
- 依赖服务未就绪
- 配置错误

### 问题2：无法连接数据库

**症状：**微服务启动时报数据库连接错误

**排查步骤：**

```bash
# 检查MySQL是否启动
docker-compose ps mysql

# 检查MySQL日志
docker-compose logs mysql

# 测试数据库连接
docker-compose exec mysql mysql -uroot -proot123456 -e "SELECT 1"
```

**解决方案：**
- 等待MySQL完全启动（通常需要30秒）
- 检查数据库密码是否正确
- 检查网络连接

### 问题3：Nacos注册失败

**症状：**微服务无法注册到Nacos

**排查步骤：**

```bash
# 检查Nacos是否启动
docker-compose ps nacos

# 访问Nacos控制台
curl http://localhost:8848/nacos/

# 查看微服务日志
docker-compose logs <service-name>
```

**解决方案：**
- 确保Nacos完全启动（通常需要1-2分钟）
- 检查网络配置
- 重启微服务

### 问题4：前端无法访问后端

**症状：**前端页面加载失败或API请求失败

**排查步骤：**

```bash
# 检查网关是否启动
docker-compose ps seckill-gateway

# 测试网关接口
curl http://localhost:8080/actuator/health

# 检查前端配置
docker-compose logs seckill-frontend
```

**解决方案：**
- 检查API网关是否正常运行
- 确认前端配置的API地址正确
- 检查跨域配置

### 问题5：内存不足

**症状：**容器频繁重启或系统变慢

**排查步骤：**

```bash
# 查看Docker资源使用情况
docker stats

# 查看系统内存
free -h
```

**解决方案：**
- 增加系统内存
- 减少ElasticSearch的内存分配（修改docker-compose.yml中的ES_JAVA_OPTS）
- 关闭不必要的服务

## 性能优化

### 生产环境配置建议

#### 1. JVM参数优化

在docker-compose.yml中为每个Java服务添加JVM参数：

```yaml
environment:
  JAVA_OPTS: "-Xms512m -Xmx1024m -XX:+UseG1GC"
```

#### 2. MySQL优化

```sql
-- 增加连接池大小
SET GLOBAL max_connections = 1000;

-- 优化查询缓存
SET GLOBAL query_cache_size = 268435456;

-- 优化InnoDB缓冲池
SET GLOBAL innodb_buffer_pool_size = 2147483648;
```

#### 3. Redis优化

```bash
# 修改Redis配置
docker-compose exec redis redis-cli -a redis123456 CONFIG SET maxmemory 2gb
docker-compose exec redis redis-cli -a redis123456 CONFIG SET maxmemory-policy allkeys-lru
```

#### 4. Nginx优化

创建自定义nginx.conf：

```nginx
worker_processes auto;
worker_connections 4096;

gzip on;
gzip_types text/plain text/css application/json application/javascript;
```

## 监控和日志

### 日志管理

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f seckill-gateway

# 导出日志到文件
docker-compose logs > logs.txt

# 查看最近100行日志
docker-compose logs --tail=100
```

### 监控指标

建议监控以下指标：

| 指标 | 说明 | 告警阈值 |
|------|------|----------|
| CPU使用率 | 容器CPU使用率 | >80% |
| 内存使用率 | 容器内存使用率 | >85% |
| 磁盘使用率 | 磁盘空间使用率 | >90% |
| API响应时间 | 接口平均响应时间 | >1000ms |
| 错误率 | API错误率 | >5% |
| 数据库连接数 | MySQL活跃连接数 | >800 |
| Redis内存 | Redis内存使用率 | >80% |

## 备份和恢复

### 数据备份

```bash
# 备份MySQL数据
docker-compose exec mysql mysqldump -uroot -proot123456 seckill_db > backup.sql

# 备份Redis数据
docker-compose exec redis redis-cli -a redis123456 SAVE
docker cp seckill-redis:/data/dump.rdb ./backup/redis-dump.rdb
```

### 数据恢复

```bash
# 恢复MySQL数据
docker-compose exec -T mysql mysql -uroot -proot123456 seckill_db < backup.sql

# 恢复Redis数据
docker cp ./backup/redis-dump.rdb seckill-redis:/data/dump.rdb
docker-compose restart redis
```

## 升级和维护

### 滚动升级

```bash
# 重新构建镜像
docker-compose build <service-name>

# 滚动更新服务
docker-compose up -d --no-deps --build <service-name>
```

### 清理资源

```bash
# 停止并删除容器
docker-compose down

# 删除所有数据卷
docker-compose down -v

# 清理未使用的镜像
docker image prune -a

# 清理未使用的卷
docker volume prune
```

## 安全建议

### 生产环境安全配置

1. **修改默认密码**
   - 修改MySQL root密码
   - 修改Redis密码
   - 修改RabbitMQ密码
   - 修改Nacos密码

2. **网络隔离**
   - 使用防火墙限制端口访问
   - 只暴露必要的端口（80, 443）
   - 内部服务不对外暴露

3. **HTTPS配置**
   - 配置SSL证书
   - 强制使用HTTPS
   - 配置HSTS

4. **访问控制**
   - 配置API限流
   - 实现IP白名单
   - 添加请求签名验证

## 故障恢复

### 服务异常重启

```bash
# 重启单个服务
docker-compose restart <service-name>

# 重启所有服务
docker-compose restart

# 强制重新创建容器
docker-compose up -d --force-recreate
```

### 数据恢复

如果数据损坏，可以从备份恢复：

```bash
# 停止服务
docker-compose down

# 删除数据卷
docker volume rm seckill_project_mysql-data

# 重新启动
docker-compose up -d

# 恢复数据
docker-compose exec -T mysql mysql -uroot -proot123456 seckill_db < backup.sql
```

## 联系支持

如遇到部署问题，请通过以下方式获取帮助：

- 查看项目README.md
- 查看Docker日志
- 提交Issue到GitHub
- 发送邮件至：ycy221100@163.com

---

**作者：** Manus AI  
**最后更新：** 2024年11月
