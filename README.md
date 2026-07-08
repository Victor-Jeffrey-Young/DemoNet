<div align="center">

# DemoNet

**策展化的「试玩派对」— 多品类聚合平台**

游戏 · 电影 · 动漫 · 桌游 · 模型 · 书籍 · 音乐 · 数码 · 咖啡 · 线下体验

每个品类拥有独立的视觉语言和交互体验，让用户在购买/下载/出门前，通过 Demo、预告片、试读、试听等低门槛内容先行体验。

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-6DB33F?logo=springboot&logoColor=white)
![Vue](https://img.shields.io/badge/Vue-3.5-4FC08D?logo=vuedotjs&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.12-FF6600?logo=rabbitmq&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue)

</div>

---

## ✨ 特性

### 10 品类独立视觉语言

每个品类不是简单的换色，而是完全不同的 Hero 设计、卡片布局和交互方式：

| 品类 | 视觉风格 | 交互亮点 |
|------|----------|----------|
| 🎮 游戏 | 暗绿 + 霓虹光效 | 无限循环轮播 · Steam 头图比例 21:10 |
| 🎬 电影 | 深红 + 全屏 Banner | 全屏渐变遮罩 + YouTube 预告片嵌入 |
| 📺 动漫 | 紫/粉调 | 灰度 hover 卡片 · 触控滑动手势 |
| 🎲 桌游 | 琥珀暖色 + 木纹 | 3D 盒装展示 · 原木货架拼接效果 |
| 🤖 模型 | 暗青 + 机械感 | 格纳库陈列架 |
| 📚 书籍 | 暖奶油 + 深木色 | 3D 书脊 + 手写推荐卡 · EPUB 试读 |
| 🎵 音乐 | 紫罗兰 + 圆弧几何 | CoverFlow 3D 封面墙 · Apple Music 试听 |
| ⌚ 数码 | 暗青渐变 + 毛玻璃 | 产品聚焦展台 |
| ☕ 咖啡 | 牛皮纸 + 粉笔字 | 黑板手写菜单 · SCA 雷达图 · 冲煮计算器 |
| 🏛️ 线下 | 暖橙 + 软木纹理 | 软木公告板 · 图钉 + 传单 |

### 核心功能

- **异步数据抓取管道** — 7 个外部 API（Steam / TMDB / IGDB / AniList / Bangumi / iTunes / SteamGridDB）通过 RabbitMQ 异步抓取，自动入库待审核
- **热门推荐算法** — 基于时间衰减的热度公式：`(LN(好评数+1)×100 + 热门加权 + 本地评论×500) / 天数`
- **用户系统** — JWT 无状态认证 · BCrypt 加密 · IP 限流 · Cloudflare Turnstile · 邀请码注册
- **管理后台** — Item CRUD · 批量操作 · 11 个品类专属编辑器 · 用户管理 · API 密钥配置 · 品类排序/可见性
- **评论系统** — 评分 + 文字评论 · 统计聚合
- **响应式设计** — 桌面/平板/手机全适配，移动端原生级触控交互

---

## 🛠 技术栈

| 层 | 技术 | 版本 |
|----|------|------|
| **前端** | Vue 3 + Vite + Pinia + Vue Router 4 + Tailwind CSS 4 | 3.5 / 8.0 / 3.0 / 4.6 / 4.3 |
| UI 组件 | Element Plus + @iconify/vue | 2.14 / 5.0 |
| 图表 | ECharts | 6.1 |
| **后端** | Spring Boot + JDK 17 | 4.0.6 |
| ORM | MyBatis-Plus | 3.5.16 |
| JSON | Jackson 3（`tools.jackson.*`） | 3.1.2 |
| 认证 | Spring Security + JWT (jjwt) | 0.12.6 |
| **数据库** | MySQL 8.0（生产 MariaDB 11） | Flyway V1-V3 |
| **缓存** | Redis 7（Spring Cache + @Cacheable） | Lettuce 连接池 |
| **消息队列** | RabbitMQ 3.12（7 队列 + DLX 死信队列） | |
| API 文档 | SpringDoc OpenAPI (Swagger UI) | |
| DB 迁移 | Flyway | V1 Baseline + V2 Indexes + V3 FK |

---

## 🚀 快速开始

### 前置要求

- Docker & Docker Compose
- JDK 17+
- Node.js 18+ & pnpm
- Maven 3.9+

### 一键启动

```bash
# 1. 克隆仓库
git clone https://github.com/your-username/DemoNet.git
cd DemoNet

# 2. 生成 .env 配置文件（自动生成随机 JWT 密钥）
bash scripts/setup-env.sh

# 3. 编辑 .env，填入外部 API 密钥（至少配置 TMDB 和 IGDB）
#    TMDB_API_KEY=...
#    IGDB_CLIENT_ID=...
#    IGDB_CLIENT_SECRET=...

# 4. 一键启动全栈
bash scripts/start-dev.sh
```

### 手动启动

```bash
# 启动基础设施
docker compose up -d                    # MySQL + Redis + RabbitMQ

# 启动后端
cd backend && mvn spring-boot:run       # http://localhost:8080

# 启动前端
cd frontend && pnpm install && pnpm dev # http://localhost:5173
```

### 访问地址

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:5173 |
| 后端 API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| RabbitMQ 管理 | http://localhost:15672 (admin / 见 .env) |
| 默认管理员 | admin / 见 .env 中 `ADMIN_DEFAULT_PASSWORD` |

---

## 📁 项目结构

```
DemoNet/
├── backend/                      # Spring Boot 后端
│   └── src/main/java/com/example/demonet/
│       ├── config/               # Security / Redis / RabbitMQ / RestClient
│       ├── controller/           # 7 个 Controller + GlobalExceptionHandler
│       ├── service/              # 16 个 Service（含 7 个外部 API 集成）
│       ├── entity/               # 8 个实体
│       ├── dto/                  # 6 个 DTO（@Valid 校验）
│       ├── mapper/               # MyBatis-Plus Mapper
│       └── security/             # JWT 过滤器 + Token Provider
│   └── src/main/resources/
│       ├── db/migration/         # Flyway V1-V3 迁移脚本
│       └── data/arabica_coffee.csv  # CQI 咖啡品质数据
├── frontend/                     # Vue 3 前端
│   └── src/
│       ├── views/                # 8 个页面（Home/Detail/List/Search/Admin/...）
│       ├── components/
│       │   ├── category/         # 10 Hero + 11 Card 品类组件
│       │   ├── admin/            # 11 品类编辑器 + 管理面板
│       │   └── detail/           # 详情页子组件 + widgets
│       ├── api/                  # Axios 封装 + JWT 拦截器
│       └── constants/            # 品类配置（图标/配色/默认字段）
├── docker-compose.yml            # 开发环境
├── docker-compose.prod.yml       # 生产环境
└── scripts/                      # 环境初始化 + 一键启动
```

---

## 🔌 外部数据源

| 数据源 | 品类 | 认证 | 配置项 |
|--------|------|------|--------|
| [Steam Store API](https://steamcommunity.com/dev) | 游戏 | 无需 | — |
| [TMDB API v3](https://developer.themoviedb.org/) | 电影/动漫 | API Key | `TMDB_API_KEY` |
| [IGDB API v4](https://api-docs.igdb.com/) | 游戏 | Twitch OAuth | `IGDB_CLIENT_ID` + `IGDB_CLIENT_SECRET` |
| [AniList GraphQL](https://docs.anilist.co/) | 动漫 | 无需 | — |
| [Bangumi API](https://bangumi.github.io/api/) | 动漫 | 无需 | — |
| [iTunes Search API](https://performance-partners.apple.com/) | 音乐 | 无需 | — |
| [SteamGridDB API v2](https://www.steamgriddb.com/api/v2) | 游戏（海报） | API Key | `STEAMGRIDDB_API_KEY` |
| [CQI 咖啡数据库](https://github.com/jldbc/coffee-quality-database) | 咖啡 | 离线 CSV | — |

---

## ⚙️ 配置

复制 `.env.example` 为 `.env` 并填入以下配置：

```bash
# 数据库
MYSQL_ROOT_PASSWORD=your_password
MYSQL_DATABASE=demonet

# Redis
REDIS_PASSWORD=your_password

# RabbitMQ
RABBITMQ_USER=admin
RABBITMQ_PASS=your_password

# JWT（脚本自动生成，也可手动指定）
JWT_SECRET=your_256_byte_secret

# 管理员
ADMIN_DEFAULT_PASSWORD=your_admin_password

# 外部 API（至少配置 TMDB 和 IGDB 才能抓取数据）
TMDB_API_KEY=your_tmdb_key
IGDB_CLIENT_ID=your_twitch_client_id
IGDB_CLIENT_SECRET=your_twitch_client_secret
STEAMGRIDDB_API_KEY=your_steamgriddb_key     # 可选
```

---

## 📸 截图

<!-- 在此处添加项目截图 -->
> 首页 · 10 品类圆形入口 + 热门轮播 + 精选推荐
>
> 详情页 · 游戏截图画廊 + 视频嵌入 + 评论系统
>
> 管理后台 · 11 品类专属编辑器 + 批量操作 + 用户管理
>
> 咖啡详情 · SCA 九维雷达图 + 冲煮计算器
>
> 移动端 · 原生级触控滑动 + 响应式布局

---

## 🐳 生产部署

```bash
# 1. 准备生产环境配置
cp .env.example .env
# 编辑 .env，设置强密码和生产环境参数

# 2. 启动全栈（MariaDB 11 + Redis + RabbitMQ + Backend + Frontend Nginx）
docker compose -f docker-compose.prod.yml up -d --build

# 3. 验证
curl http://localhost/api/items/hot
```

> ⚠️ 生产环境请确保使用强密码、移除中间件端口映射、为 backend 挂载 uploads 持久化卷。

---

## 🔒 安全特性

| 特性 | 实现 |
|------|------|
| 密码存储 | BCryptPasswordEncoder |
| JWT 密钥 | SHA-256 哈希 + 环境变量注入 |
| SQL 注入防护 | MyBatis-Plus LambdaQueryWrapper + 参数化 `?` 绑定 |
| 输入校验 | `@Valid` DTO + `@NotBlank` / `@Email` / `@Size` |
| 接口限流 | Redis 计数器（注册 3/min，登录 5/min） |
| 文件上传 | MIME 白名单 + 扩展名白名单 |
| 角色权限 | Spring Security `ROLE_ADMIN` 守卫管理接口 |
| 外部 API 超时 | RestClient 统一 5s 连接 / 15s 读取 |
| RabbitMQ 容错 | DLX 死信交换机 + DLQ + prefetch=5 + retry 3 次 |
| 数据库完整性 | 6 个外键约束 + ON DELETE CASCADE |

---

## 📊 项目数据

| 指标 | 数值 |
|------|------|
| 品类 | 10/10 完成 |
| 后端 Java 文件 | 59 个 |
| 前端 Vue 组件 | 67 个 |
| 数据库表 | 9 张（Flyway 管理） |
| 外部数据源 | 7 个 API + 1 个离线 CSV |
| RabbitMQ 队列 | 7 + 1 DLX + 1 DLQ |
| Java 警告 | 0 |

---

## 📝 Git Commit 规范

```
<type>(<scope>): <中文简短描述>
```

- **type**: `feat` / `fix` / `docs` / `refactor` / `style` / `chore`
- **scope**（可选）: `frontend` / `backend` / `admin` / `auth` / `docker` 等
- 一条 commit 只做一件事

---

## 📄 License

MIT License — 详见 [LICENSE](LICENSE)

---

<div align="center">

**如果这个项目对你有帮助，请给个 ⭐ Star**

</div>
