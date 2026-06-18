# DemoNet 1.0

> **策展化的"试玩派对"** — 聚合游戏 Demo、电影预告、桌游教学等可"先试试"的内容

## 项目定位

一个多品类"体验前置"平台，聚合游戏试玩、电影预告片、动漫 PV 等内容，帮助用户在购买/下载前高效决策。

**当前状态**：阶段 7.1(游戏 14 款) + 7.2(电影 16 部) 已完成 — [详细状态](./document/12-当前状态.md)

---

## 快速启动

```bash
docker compose up -d                    # MySQL + Redis + RabbitMQ
cd backend && mvn clean spring-boot:run # :8080
cd frontend && npm run dev              # :5173
```

默认用户：`admin / changeme`

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite, Pinia, Vue Router 4, Tailwind CSS 4 |
| 后端 | Spring Boot 4.0.6 (JDK 17), MyBatis-Plus 3.5.16, Spring Security + JWT |
| 中间件 | MySQL 8.0, Redis 7, RabbitMQ 3.12 |
| 数据源 | Steam Store API, TMDB API v3 |

---

## 关键文件

| 文件 | 路径 |
|------|------|
| 后端配置 | `backend/src/main/resources/application.yml` |
| 数据库 DDL | `backend/src/main/resources/schema.sql` |
| 品类常量 | `frontend/src/constants/types.js` |
| 品类动画 | `frontend/src/styles/transitions.css` |
| Steam API | `backend/src/main/java/com/example/demonet/service/SteamService.java` |
| TMDB API | `backend/src/main/java/com/example/demonet/service/TMDBService.java` |
| 种子数据 | `backend/src/main/java/com/example/demonet/service/DataImportService.java` |

---

## 文档

详细文档拆分至 `document/` 目录：

| 文档 | 内容 |
|------|------|
| [目录总览](./document/00-目录.md) | 全部文档索引 |
| [项目定位](./document/01-项目定位.md) | 愿景与核心价值 |
| [技术选型](./document/02-技术选型.md) | 技术栈及版本详情 |
| [项目结构](./document/03-项目结构.md) | 目录树与文件清单 |
| [数据模型](./document/04-数据模型.md) | 6 表设计 + info_json 结构 |
| [阶段 0-5](./document/05-阶段0-5.md) | 骨架/API/用户/搜索/管道/品类 |
| [阶段 7.0 前置准备](./document/06-阶段7.0-前置准备.md) | items 表扩充、types.js、transitions.css |
| [阶段 7.1 游戏频道](./document/07-阶段7.1-游戏频道.md) | GameHero/Card, 试玩, 视频, Steam |
| [阶段 7.2 电影频道](./document/08-阶段7.2-电影频道.md) | MovieHero/Card, TMDB 管道 |
| [阶段 7.3-7.13 待实现](./document/09-阶段7.3-7.13-待实现.md) | 其余 8 品类计划 |
| [阶段 8 平台基建](./document/10-阶段8-平台基建计划.md) | 评论/缓存/Swagger/Docker |
| [问题日志](./document/11-问题日志.md) | 28 个已解决问题及根因 |
| [当前状态](./document/12-当前状态.md) | 完成度/数据量/组件清单 |
| [API 清单](./document/13-API清单.md) | 全部端点及认证要求 |
| [建议与问题汇总](./document/14-建议与问题汇总.md) | 代码审查：安全隐患、Bug、架构建议 |
| [旧版对比与迁移](./document/15-旧版对比分析与迁移建议.md) | 0.1 vs 1.0 视觉/数据对比，迁移路线图 |

---

## 业务版图

| 业务线 | Demo 形态 | 状态 |
|--------|-----------|------|
| 电子游戏 | 免费试玩版下载 / 浏览器可玩 | 已完成 14 款 |
| 电影 | 官方预告片 / 导演访谈 | 已完成 16 部 |
| 动漫 / 剧集 | 先导预告 / 前5分钟试看 / PV | 待实现 |
| 桌游 | 规则教学视频 + 在线模组 | 待实现 |
| 模型 / 手办 | 360°展示、涂装范例 | 待实现 |
| 书籍 / 漫画 | 前20页试读、有声书样音 | 待实现 |
| 音乐 / 乐器 | 30秒高潮试听、音色对比 | 待实现 |
| 咖啡 / 茶 | 风味轮盘、冲泡指南 | 待实现 |
| 数码产品 | 打字音试听、频响曲线 | 待实现 |
| 线下体验 | 密室剧情试读、展览预览 | 待实现 |

---

## 核心设计原则

- **弹性字段**：`info_json` 使用 JSON 类型，新增品类无需改表
- **异步处理**：耗时任务（抓取、AI 摘要）走 RabbitMQ，不阻塞用户请求
- **内容来源合法**：仅聚合公开 API 和官方渠道
- **前端跨域**：开发时 Vite 代理，生产用 Nginx 反代
