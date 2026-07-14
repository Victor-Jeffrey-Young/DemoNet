# ADR-009: Docker 容器化策略

## 状态
已接受（Accepted）

## 日期
2025-03-25

## 上下文

DemoNet 是一个全栈应用，包含以下服务：

- **前端**：Vue 3 + Vite（Node.js）
- **后端**：Spring Boot 4.0.6（JDK 17）
- **数据库**：MySQL 8.0 / MariaDB 11
- **缓存**：Redis 7
- **消息队列**：RabbitMQ 3.12
- **可选**：Elasticsearch、Nginx

### 关键需求
- **环境一致性**：开发、测试、生产环境保持一致
- **一键启动**：新成员可以快速启动完整开发环境
- **可移植性**：可以在任何支持 Docker 的平台上运行
- **隔离性**：服务之间相互隔离，避免依赖冲突
- **可扩展性**：支持水平扩展（多个后端实例）

### 可选方案

| 方案 | 优点 | 缺点 |
|------|------|------|
| **Docker Compose（当前方案）** | 简单、适合单机部署、开发友好 | 不支持多机编排 |
| **Kubernetes** | 强大的编排能力、自动扩缩容 | 复杂度高、运维成本高 |
| **无容器化（直接安装）** | 简单、无 Docker 开销 | 环境不一致、部署复杂 |

## 决策

**使用 Docker Compose 进行容器化部署，分开发/生产两套配置**：

### 开发环境（docker-compose.yml）

```yaml
services:
  mysql:
    image: mysql:8.0
    container_name: demonet-mysql
    restart: always
    ports:
      - "3306:3306"  # 暴露端口，便于本地调试
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    container_name: demonet-redis
    restart: always
    ports:
      - "6379:6379"  # 暴露端口，便于本地调试

  rabbitmq:
    image: rabbitmq:3.12-management
    container_name: demonet-rabbitmq
    restart: always
    ports:
      - "5672:5672"    # AMQP 协议
      - "15672:15672"  # 管理界面
```

**特点**：
- **暴露端口**：MySQL/Redis/RabbitMQ 端口暴露到宿主机，便于本地调试
- **无密码保护**：开发环境简化配置（密码在 `.env` 中设置）
- **热重载**：后端和前端在本地运行，支持热重载

### 生产环境（docker-compose.prod.yml）

```yaml
services:
  mysql:
    image: mariadb:11  # 生产使用 MariaDB（性能更好）
    # 不暴露端口，仅内部网络通信
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:?错误提示}
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    command: redis-server --requirepass ${REDIS_PASSWORD:?错误提示}
    # 不暴露端口

  rabbitmq:
    image: rabbitmq:3-management-alpine
    # 不暴露端口

  backend:
    build: ./backend
    depends_on:
      - mysql
      - redis
      - rabbitmq
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/demonet
      SPRING_REDIS_HOST: redis
      SPRING_RABBITMQ_HOST: rabbitmq
    volumes:
      - uploads:/app/uploads  # 持久化上传文件
      - logs:/app/logs        # 持久化日志

  frontend:
    build: ./frontend
    ports:
      - "80:80"  # 仅暴露前端端口
```

**特点**：
- **不暴露内部服务端口**：MySQL/Redis/RabbitMQ 仅通过 Docker 内部网络通信
- **强制环境变量**：使用 `${VAR:?错误提示}` 语法，缺少环境变量时直接失败
- **持久化卷**：`uploads`、`logs` 持久化到 Docker Volume，避免容器重启丢失数据
- **生产镜像优化**：后端使用 `spring-boot:run` 直接运行 Jar 包，减少镜像体积

### Dockerfile 示例

**后端 Dockerfile**：
```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**前端 Dockerfile**：
```dockerfile
FROM node:22-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

### 一键启动脚本

```bash
# scripts/start-dev.sh
#!/bin/bash
docker compose up -d
cd backend && mvn spring-boot:run &
cd frontend && pnpm dev &
wait
```

## 替代方案被拒绝的原因

### Kubernetes
- ❌ **复杂度高**：DemoNet 的流量量级（每天几百到几千次请求）不需要 K8s 的编排能力
- ❌ **运维成本高**：需要专职 DevOps 维护 K8s 集群
- ❌ **过度设计**：Docker Compose 完全满足当前需求

### 无容器化
- ❌ **环境不一致**：新成员需要手动安装 JDK、MySQL、Redis、RabbitMQ，容易出现版本不一致
- ❌ **部署复杂**：生产环境需要手动配置每个服务，容易出错
- ❌ **隔离性差**：不同项目的依赖可能冲突（如 Java 版本、MySQL 版本）

## 后果

### ✅ 正面影响
- **环境一致性**：开发、测试、生产环境完全一致，避免"在我机器上能跑"的问题
- **一键启动**：新成员只需 `bash scripts/start-dev.sh` 即可启动完整环境
- **隔离性**：服务之间相互隔离，避免依赖冲突
- **可移植性**：可以在任何支持 Docker 的平台上运行（本地、服务器、云平台）

### ⚠️ 负面影响
- **Docker 学习成本**：团队成员需要学习 Docker 基础命令和 Docker Compose
- **磁盘占用**：Docker 镜像、容器、卷占用大量磁盘空间（约 5-10 GB）
- **性能开销**：容器网络、文件系统有轻微性能损耗（约 5-10%）
- **调试困难**：容器内部的日志、配置不如本地直观

### 📋 优化方向
1. **多阶段构建**：优化 Dockerfile，减小镜像体积（如后端 Jar 包大小从 500MB 降到 100MB）
2. **健康检查**：添加 `healthcheck` 配置，确保服务启动完成后再启动依赖服务
3. **日志聚合**：使用 ELK Stack（Elasticsearch + Logstash + Kibana）或 Loki + Grafana 聚合日志
4. **镜像优化**：使用 Distroless 或 Alpine 基础镜像，减小镜像体积和攻击面

## 参考资料
- [Docker 官方文档](https://docs.docker.com/)
- [Docker Compose 官方文档](https://docs.docker.com/compose/)
- [Spring Boot Docker 最佳实践](https://spring.io/guides/gs/spring-boot-docker/)
- 部署脚本：`scripts/start-dev.sh`、`scripts/setup-env.sh`
- 运维文档：`document/07-部署运维.md`
