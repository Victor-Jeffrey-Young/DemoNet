# DemoNet 文档导航

> **DemoNet** — 策展化的"试玩派对"，多品类聚合平台（游戏/电影/动漫/桌游/模型/书籍/音乐/数码/咖啡/线下体验）
> **最后更新**：2026-07-03

---

## 文档索引

| 编号 | 文档 | 内容 |
|------|------|------|
| [01](01-项目概述.md) | 项目概述 | 定位、愿景、技术栈、当前状态 |
| [02](02-项目结构.md) | 项目结构 | 前后端完整目录树与文件清单 |
| [03](03-数据模型.md) | 数据模型 | 6 张表设计 + info_json 多态结构 |
| [04](04-API清单.md) | API 清单 | 全部端点（公开 / 需认证 / Admin） |
| [05](05-开发历程.md) | 开发历程 | 阶段 0-10 全部完成记录 |
| [06](06-品类设计.md) | 品类设计 | 10 品类独立视觉语言设计文档 |
| [07](07-部署运维.md) | 部署运维 | Docker 部署 + 测试策略 + 上线清单 |
| [08](08-问题与待办.md) | 问题与待办 | 已修复 / 待修复 / 后续计划 |
| [09](09-IGDB集成.md) | IGDB 集成 | Twitch IGDB API v4 数据源技术参考 |

---

## 项目速览

| 指标 | 数值 |
|------|------|
| 品类完成 | 10/10 |
| 种子数据 | 126 items |
| 前端组件 | 10 Hero + 11 Card + 11 Editor + TypeIcon |
| 后端 Service | 15 个 |
| 后端 Controller | 7 个 |
| 数据抓取源 | 6（Steam / TMDB / IGDB / AniList / Bangumi / iTunes）+ 自动标签 |
| 测试覆盖 | 1 集成测试 |

### 技术栈

| 层 | 技术 |
|----|------|
| 前端 | Vue 3 + Vite + Pinia + Vue Router 4 + Tailwind CSS 4 |
| 后端 | Spring Boot 4.0.6 + JDK 17 + MyBatis-Plus 3.5.16 |
| 认证 | Spring Security + JWT（jjwt 0.12.6） |
| 数据库 | MySQL 8.0（生产 MariaDB 11） |
| 缓存 | Redis 7 |
| 消息队列 | RabbitMQ 3.12 |
| API 文档 | SpringDoc OpenAPI（Swagger UI） |

### 快速启动

```bash
# 首次：生成 .env
bash scripts/setup-env.sh

# 启动全栈
bash scripts/start-dev.sh

# 手动启动
docker compose up -d                          # MySQL + Redis + RabbitMQ
cd backend && mvn clean spring-boot:run       # :8080
cd frontend && npm run dev                    # :5173
```

- 前端：`http://localhost:5173`
- 后端：`http://localhost:8080`
- Swagger：`http://localhost:8080/swagger-ui/index.html`
- 默认管理员：`admin / changeme`
