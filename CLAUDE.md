# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此仓库中工作提供指引。

## 项目定位

DemoNet 是一个策展化的多品类发现平台（"试玩派对"）—— 一个横跨 10 个品类（game / movie / anime / boardgame / model / book / music / digital / coffee / offline）的统一目录，每个品类都有独立的视觉语言与交互方式。内容通过异步管道从 7 个外部 API 抓取，经由时间衰减的热度公式打分，并通过公开浏览页与后台策展系统呈现。

## 基础设施

Docker Compose（MySQL 8 + Redis 7 + RabbitMQ 3.12）。配置在 `.env` 中，首次通过 `bash scripts/setup-env.sh` 生成。后端 Spring Boot 4.0.6 / JDK 17 / Maven；前端 Vue 3 / Vite / pnpm。

```bash
# 环境初始化（生成 .env）+ 一键启动全栈
bash scripts/setup-env.sh
bash scripts/start-dev.sh          # 基础设施 + 后端 :8080 + 前端 :5173

# --- 或分步手动启动 ---
docker compose up -d                # 仅基础设施
cd backend  && mvn spring-boot:run  # :8080（Swagger: /swagger-ui/index.html）
cd frontend && pnpm install && pnpm dev   # :5173

# 后端测试
cd backend && mvn -B -ntp test                       # 全套测试
cd backend && mvn -B -ntp test -Dtest=ItemServiceTest # 单个测试类
cd backend && mvn -B -ntp test -Dtest=ItemServiceTest#listHotItems # 单个测试方法
cd backend && mvn -B -ntp -DskipTests package         # 仅编译打包（跳过测试）

# 前端
cd frontend && pnpm dev          # Vite 开发服务器（HMR）
cd frontend && pnpm build         # 生产构建 → frontend/dist
cd frontend && pnpm preview       # 本地预览生产构建

# 生产环境（独立 compose 文件，一键全栈）
docker compose -f docker-compose.prod.yml up -d --build
```

测试按层拆分：`src/test/java/.../service/*Test.java` 是纯 Mockito 单元测试（无 Spring 上下文）；`controller/*Test.java` 是 `@WebMvcTest` 切片测试。CI 工作流（`.github/workflows/ci-cd.yml`）前后端并行构建，后端测试在 MySQL/Redis/RabbitMQ 服务容器中执行，仅当 `main` 分支 push 且 `ENABLE_DEPLOY=true` 时才部署 —— 任一上游失败都会阻断部署。

## 架构概览

### 整体：两个应用 + 一条异步抓取管道

```
[外部 API] ──RabbitMQ（7 队列 + DLX/DLQ）──▶ FetchConsumer ──▶ items 表
                                                   │  从 infoJson 自动打标签
[前端 Vue 3] ──/api──▶ Spring Boot ──MyBatis-Plus──▶ MySQL（9 张表）
                             │
                     Spring Security + JWT
                     Redis（缓存、限流）
```

**从单一文件最难看清的事实：内容抓取是完全异步且品类无关的。** 管理员发起抓取 → `AdminService` 向 7 个 RabbitMQ 队列之一（定义在 `RabbitMQConfig`，每个都配置了 `x-dead-letter-exchange` → `demonet.dlq`）发布 `Map<String,Object>` 消息 → `FetchConsumer` 按队列路由到对应的 `*Service`（Steam / TMDB / IGDB / AniList / Bangumi / iTunes）→ 每个服务返回归一化的 `List<Item>` → `saveItems` 按 slug 做 upsert + 从 `infoJson` 自动打标签。失败的消息会进入 DLQ，不会静默丢弃。

### 后端结构（`backend/src/main/java/com/example/demonet/`）

- `config/` — `SecurityConfig`（无状态 JWT；`/api/items/**`、`/api/scenes/**`、`/api/tags/**`、`/api/reviews/item/**`、`/api/categories/**` 公开可读；`/api/admin/**` 全部要求 `ROLE_ADMIN`）、`RabbitMQConfig`、`RedisConfig`、`MybatisPlusConfig`（分页插件 + 驼峰映射）、`WebConfig`（CORS）、`RestClientConfig`、`ResponseAdvice`。
- `security/` — `JwtTokenProvider`（HMAC-SHA256，密钥来自 `JWT_SECRET` 环境变量）+ `JwtAuthFilter`（在 `UsernamePasswordAuthenticationFilter` 之前执行）。
- `controller/` — 8 个控制器 + `GlobalExceptionHandler`。`ApiResponse<T>`（`common/ApiResponse.java`）是统一响应包络：`{code, message, data}`；`code==200` 时解出 `data`。
- `service/` — 领域服务 + 7 个外部 API 适配器 + `FetchConsumer`。`DataImportService`（`CommandLineRunner`）在首次启动时从 `resources/data/arabica_coffee.csv` 导入约 130 条种子数据，并通过 `ALTER TABLE IF NOT EXISTS` 补全 schema。
- `entity/`、`mapper/`、`dto/` — MyBatis-Plus 实体、Mapper、带 `@Valid` 校验的请求 DTO。

### 多态内容模型（修改 items 前必读）

`items` 表是所有 10 个品类的统一表。三个字段承担核心职责：
- `type`（VARCHAR）— 取值 `game / movie / anime / boardgame / model / book / music / digital / coffee / offline`，同时驱动后端路由与前端渲染。
- `info_json`（JSON）— **品类专属结构**（冲煮参数、EPUB 链接、玩家人数、演员表等）。使用 `tools.jackson`（`ObjectMapper`）解析，而非默认 Jackson。`FetchConsumer.autoTag` 正是读取此字段来生成标签。
- `status` TINYINT — `1`=已上线，`0`=待审核。新条目从 `0` 开始；`approve` 翻转为 `1`。

热度排序（`ItemService`）：
```
热度分 = (LN(recommendations + 1) * 100 + hot_boost + recent_reviews * 500)
        / GREATEST(DATEDIFF(NOW(), created_at), 1)
```
`recommendations` 承载好评数（如 Steam）；`hot_boost` 是管理员手动加权。Schema 由 Flyway 管理（`resources/db/migration/V1–V4`）；`V4` 为 Scene 功能新增了 `curation_collections` + `curation_collection_items`，独立于热度/轮播。Schema 的权威文档见 `document/03-数据模型.md`。

### 前端结构（`frontend/src/`）

- `api/` — 按域划分模块（`admin`、`auth`、`item`、`review`、`scene`、`tag`），都是 `api/request.js` 中 axios 实例的薄封装。请求拦截器注入 JWT；响应拦截器解包 `{code, message, data}`（`code != 200` 时 reject）并在 401/403 时跳转到 `/login`。Base URL `/api` 由 Vite 代理。
- `stores/auth.js` — Pinia 状态；`token` 持久化在 `localStorage`，`user` 持有 `role`。路由守卫（`router/index.js`）将未登录用户拦在 `requiresAuth` 路由外，将非管理员拦在 `/admin` 外。
- `constants/types.js` — 品类元数据的唯一来源（emoji / icon / accent / heroColor 等）。10 个 Hero/Card 组件（`components/category/*`）和 11 个后台编辑器字段集（`components/admin/*EditorFields.vue`）都按此文件的 `type` 键接。新增品类请改这里。
- 视图在路由中懒加载；`Detail.vue` 按 `type` 分派到 `components/detail/types/*Detail.vue`；`Admin.vue` 分派到对应的 `*EditorFields`。

### 外部数据源

Steam（游戏，无认证）· TMDB（电影/动漫，`TMDB_API_KEY`）· IGDB（游戏，Twitch OAuth `IGDB_CLIENT_ID/SECRET`）· AniList（GraphQL）· Bangumi（动漫，中文）· iTunes（音乐）· SteamGridDB（海报，`STEAMGRIDDB_API_KEY`）。通过 `.env` 配置；至少配置 TMDB + IGDB 管道才能产出数据。适配器清单见 `document/09-外部数据源集成.md`。

## 需要知道的约定

- **统一响应包络：** 响应包装在 `ApiResponse {code, message, data}` 中。公开 GET 路由因 `ResponseAdvice` 直接返回数据；新增端点应沿用同一包络，以保持前端拦截器正确。
- **后端 JSON 库是 `tools.jackson`**，不是 `com.fasterxml.jackson`。新代码保持一致（`tools.jackson.databind.*`）。
- **国际化：** 用户可见字符串与文档为中文；提交信息遵循 `type(scope): 中文描述`（`feat|fix|docs|refactor|style|chore`；scope 如 `frontend/backend/admin/auth/docker`）。
- **文档所有权：** `document/` 中是按编号排列的唯一权威文档（01 项目概述 … 03 数据模型、04 API 清单、07 运维、09 外部集成、11 CI/CD）。详见 `AGENTS.md` 的"文档维护原则"—— 一个主题一份权威文档；增删文件时同步更新 `document/README.md` 的索引；切勿提交密钥。
- **安全面：** BCrypt 密码、SHA-256 JWT、MyBatis-Plus `LambdaQueryWrapper`（参数化）、`@Valid` DTO、Redis 限流（注册 3/分钟，登录 5/分钟）、`ROLE_ADMIN` 守卫、RestClient 5s/15s 超时、RabbitMQ 的 DLX/DLQ。详见 README"安全特性"。

## 索引说明

codebase-memory-mcp 的先前索引排除了 `backend/src/main/java/com/example` 与 `backend/src/test` —— 若图谱查询遗漏 Java 符号，请重新索引并包含这两个目录（或直接读取文件；后端约 60 个 Java 文件，可直接浏览）。
