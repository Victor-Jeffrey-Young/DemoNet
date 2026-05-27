# DemoNet 1.0 开发文档

> **策展化的"试玩派对"** — 聚合游戏 Demo、电影预告、桌游教学等可"先试试"的内容
> **最后更新**：2026-05-28
> **当前状态**：阶段 7.1(游戏) ✅ + 7.2(电影) ✅ - [详细状态 →](./document/12-当前状态.md)

---

## 快速入口

文档已按阶段/主题拆分至 `document/` 目录：

| 文档 | 内容 |
|------|------|
| [📋 目录总览](./document/00-目录.md) | 全部文档索引 |
| [🎯 项目定位](./document/01-项目定位.md) | 愿景与核心价值 |
| [⚙️ 技术选型](./document/02-技术选型.md) | 技术栈及版本 |
| [📁 项目结构](./document/03-项目结构.md) | 目录树与文件清单 |
| [🗄️ 数据模型](./document/04-数据模型.md) | 6 表设计 + info_json 结构 |
| [🏗️ 阶段 0-5](./document/05-阶段0-5.md) | 骨架/API/用户/搜索/管道/品类 |
| [🔧 阶段 7.0 前置准备](./document/06-阶段7.0-前置准备.md) | 列扩充 + types.js + transitions.css |
| [🎮 阶段 7.1 游戏频道](./document/07-阶段7.1-游戏频道.md) | GameHero/Card, 试玩, 视频, Steam |
| [🎬 阶段 7.2 电影频道](./document/08-阶段7.2-电影频道.md) | MovieHero/Card, TMDB 管道, 16 部电影 |
| [⏳ 阶段 7.3-7.13 待实现](./document/09-阶段7.3-7.13-待实现.md) | 其余 8 品类计划 |
| [📋 阶段 8 平台基建](./document/10-阶段8-平台基建计划.md) | 评论/缓存/Swagger/Docker |
| [🐛 问题日志](./document/11-问题日志.md) | 28 个已解决问题及根因 |
| [📊 当前状态](./document/12-当前状态.md) | 完成度/数据量/组件清单 |
| [🔌 API 清单](./document/13-API清单.md) | 全部端点及认证要求 |
| [📝 建议与问题汇总](./document/14-建议与问题汇总.md) | 代码审查：安全隐患、Bug、架构建议 |

---

## 快速启动

```bash
docker compose up -d                    # MySQL + Redis + RabbitMQ
cd backend && mvn clean spring-boot:run # :8080
cd frontend && npm run dev              # :5173
```

默认用户：`admin / changeme`

## 关键文件

| 文件 | 路径 |
|------|------|
| 后端配置 | `backend/src/main/resources/application.yml` |
| 数据库 DDL | `backend/src/main/resources/schema.sql` |
| 品类常量 | `frontend/src/constants/types.js` |
| 品类动画 | `frontend/src/styles/transitions.css` |
| Steam API | `backend/.../service/SteamService.java` |
| TMDB API | `backend/.../service/TMDBService.java` |
| 种子数据 | `backend/.../service/DataImportService.java` |
