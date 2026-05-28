# 阶段 13：测试 · Docker · 部署 · 上线

> **创建日期**：2026-05-29
> **前提**：阶段 8-12 假设完成

---

## 一、测试策略

### 当前状态

`src/test/` 为空，**零测试覆盖**。

### 分层计划

| 层级 | 工具 | 范围 | 优先级 |
|------|------|------|--------|
| 后端 Service 单元测试 | JUnit 5 + Mockito | `ItemService`、`AuthService`、`TagService` 核心方法 | P0 |
| 后端 Controller 集成测试 | `@SpringBootTest` + MockMvc | 5 个核心 API（注册/登录/列表/详情/搜索） | P1 |
| 前端组件测试 | Vitest + @vue/test-utils | 关键组件（AppCard、Detail、SearchBar） | P2 |
| E2E 测试 | Playwright | 完整用户流程：注册→浏览→收藏→评论 | P3 |

### 最低底线

```
□ ItemService: getBySlug / listPaginated / listHot / listFeatured
□ AuthService: register / login / token 验证
□ TagService: listPaginated + keyword 搜索
□ Controller: GET /api/items/xxx → 200
□ Controller: POST /api/auth/login → 200 + JWT
□ Controller: GET /api/items?type=game → 200 + 分页
```

---

## 二、Docker 化

### 目标架构

```
┌─────────────────────────────────────────┐
│  nginx (:80)                            │
│  ├── /api/*     → backend:8080          │
│  ├── /uploads/* → backend:8080          │
│  └── /*         → /usr/share/nginx/html │
├─────────────────────────────────────────┤
│  backend     (:8080)  Spring Boot jar   │
├─────────────────────────────────────────┤
│  mysql       (:3306)  MariaDB           │
│  rabbitmq    (:5672)  RabbitMQ          │
│  redis       (:6379)  Redis             │
└─────────────────────────────────────────┘
```

### 需要新建的文件

| 文件 | 说明 |
|------|------|
| `backend/Dockerfile` | 多阶段构建：`mvn package` → `eclipse-temurin:21-jre-alpine` |
| `frontend/Dockerfile` | 多阶段构建：`npm run build` → `nginx:alpine` 托管 dist |
| `docker-compose.prod.yml` | 生产编排：5 个 service + 网络 + 卷持久化 |
| `nginx/nginx.conf` | 反向代理 /api → backend + gzip + 静态资源缓存 |

### 一命令启动

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

---

## 三、部署方案

### 推荐：单机 VPS

| 步骤 | 内容 |
|------|------|
| 1 | 阿里云/腾讯云 2C4G ECS（~¥50-100/月） |
| 2 | Ubuntu 22.04 + Docker + Docker Compose |
| 3 | `git clone` → 配 `.env` → `docker compose up -d` |
| 4 | 域名 DNS 解析 + nginx 配置 |
| 5 | Let's Encrypt TLS 证书（Certbot + 自动续期） |
| 6 | GitHub Actions：`push main` → SSH → `git pull` → `docker compose up -d --build` |

### 备选：Serverless 平台

| 平台 | 月费 | 适合场景 |
|------|------|----------|
| Zeabur | ¥30-80 | 国内可用，按量，几乎零配置 |
| Railway | $5-20 | 海外节点，国内慢 |
| Fly.io | $0-10 | 轻量，全球节点 |
| 阿里云 SAE | ¥200+ | 免运维，贵 |

**建议**：先走单机 VPS。DemoNet 是内容展示型项目，月 PV 不会爆炸。

---

## 四、上线前检查清单

### 安全

```
□ SQL 查询全部参数化（无字符串拼接）
□ Admin API 前后端双角色校验（requiresAdmin）
□ 敏感配置移至 .env（MySQL/Redis/RabbitMQ 密码、JWT secret）
□ SecurityConfig X-Frame-Options 已设 SAMEORIGIN
□ passwordHash 字段 @JsonIgnore 不暴露
```

### 数据

```
□ 种子数据 dedup（生产环境删除测试数据）
□ admin 默认密码已改
□ MySQL 每日自动备份脚本（crontab + mysqldump）
```

### 运维

```
□ /api/health 健康检查端点
□ 日志不输出密码/token
□ nginx gzip 开启 + 静态资源缓存
□ HTTPS + HSTS 头
```

### 合规

```
□ 域名备案（国内服务器必需）
□ 隐私政策页面
□ robots.txt + sitemap.xml
□ 图片无版权问题（种子数据用 picsum 占位 → 替换真实图片）
```

---

## 五、CI/CD

### GitHub Actions 流水线

```yaml
name: Deploy
on:
  push:
    branches: [main]
jobs:
  test-backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21' }
      - run: cd backend && mvn test

  test-frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: '20' }
      - run: cd frontend && npm ci && npm run build

  deploy:
    needs: [test-backend, test-frontend]
    runs-on: ubuntu-latest
    steps:
      - uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.SSH_HOST }}
          username: ${{ secrets.SSH_USER }}
          key: ${{ secrets.SSH_KEY }}
          script: |
            cd /opt/demonet
            git pull
            docker compose -f docker-compose.prod.yml up -d --build
```

---

## 六、实现步骤

| 步骤 | 内容 | 预估 |
|------|------|------|
| 1 | 后端 Service 单元测试（ItemService + AuthService） | 2h |
| 2 | 后端 Controller 集成测试（5 核心端点） | 1h |
| 3 | `backend/Dockerfile` 多阶段构建 | 30min |
| 4 | `frontend/Dockerfile` nginx 托管 | 30min |
| 5 | `docker-compose.prod.yml` 5-service 编排 | 1h |
| 6 | `nginx.conf` 反向代理 + gzip | 30min |
| 7 | `.env` 模板 + 敏感配置迁移 | 30min |
| 8 | 健康检查端点 `GET /api/health` | 15min |
| 9 | GitHub Actions CI/CD 流水线 | 1h |
| 10 | VPS 部署验证 | 1h |

**合计**：~8 小时
