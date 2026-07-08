# DemoNet

策展化的"试玩派对" — 多品类聚合平台（游戏/电影/动漫/桌游/模型/书籍/音乐/数码/咖啡/线下体验）。

## 技术栈

- **前端**：Vue 3 + Vite + Pinia + Vue Router 4 + Tailwind CSS 4
- **后端**：Spring Boot 4.0.6 + JDK 17 + MyBatis-Plus 3.5.16
- **数据库**：MySQL 8.0 / MariaDB 11（生产）
- **缓存**：Redis 7
- **消息队列**：RabbitMQ 3.12
- **认证**：Spring Security + JWT（jjwt 0.12.6）

## 文档

所有文档位于 `document/` 目录，详见 [README](document/README.md)：

| # | 文档 | 内容 |
|---|------|------|
| 01 | 项目概述 | 定位、愿景、技术栈、当前状态 |
| 02 | 项目结构 | 前后端完整目录树 |
| 03 | 数据模型 | 9 张表 + info_json 多态结构 |
| 04 | API 清单 | 全部端点（公开/认证/Admin） |
| 05 | 开发历程 | 阶段 0-11 + 架构改造记录 |
| 06 | 品类设计 | 10 品类独立视觉语言 |
| 07 | 部署运维 | Docker + 测试 + 上线 |
| 08 | 问题与待办 | 已修复 / 待修复 / 后续计划 |
| 09 | 外部数据源集成 | 7 个 API + CQI 咖啡数据源 |
| 10 | 修复与上线计划 | 安全加固 + Schema + 架构清理（大部分已完成） |
| 11 | 后端架构分析 | 23 项性能与安全风险评估 |
| 12 | 后端架构改造计划 | 4 阶段改造方案 |
| 13 | 测试用例 | 前后端验收用例 |
| 14 | 规范化重构计划 | 统一响应体 + DTO 校验 + 拦截器 |
| 15 | 代码审计报告 | 两轮审计合并，25 项问题 + 修复状态 |

## 快速启动

```bash
bash scripts/setup-env.sh     # 首次：生成 .env
bash scripts/start-dev.sh     # 启动全栈
# 或手动：
docker compose up -d
cd backend && mvn clean spring-boot:run   # :8080
cd frontend && npm run dev                # :5173
```

## Git Commit 规范

```
<type>(<scope>): <中文简短描述>
```

- type: feat / fix / docs / refactor / style / chore
- scope（可选）: frontend / backend / admin / auth / docker 等
- 描述用中文，一条 commit 只做一件事
