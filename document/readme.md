# DemoNet 文档导航

> **DemoNet** — 策展化的"试玩派对"，多品类聚合平台（游戏/电影/动漫/桌游/模型/书籍/音乐/数码/咖啡/线下体验）
> **最后更新**：2026-07-05

---

## 文档索引

| 编号 | 文档 | 内容 |
|------|------|------|
| [01](01-项目概述.md) | 项目概述 | 定位、愿景、技术栈、当前状态 |
| [02](02-项目结构.md) | 项目结构 | 前后端完整目录树与文件清单（含全部新增文件） |
| [03](03-数据模型.md) | 数据模型 | **9 张表**设计 + info_json 多态结构 |
| [04](04-API清单.md) | API 清单 | 全部端点（公开 / 需认证 / Admin，含近期新增） |
| [05](05-开发历程.md) | 开发历程 | 阶段 0-10 全部完成记录（实时更新） |
| [06](06-品类设计.md) | 品类设计 | 10 品类独立视觉语言设计文档 |
| [07](07-部署运维.md) | 部署运维 | Docker 部署 + 环境变量 + 测试策略 + 上线清单 |
| [08](08-问题与待办.md) | 问题与待办 | 已修复 / 待修复 / 后续计划 |
| [09](09-IGDB集成.md) | IGDB 集成 | Twitch IGDB API v4 数据源技术参考 |
| [10](10-修复与上线计划.md) | 修复与上线计划 | **严密执行计划**：Bug修复 → 安全加固 → 测试 → 部署 |
| [11](11-后端架构分析.md) | 后端架构分析 | 评估现有后端代码在小型服务器 + 数据量增长场景下的性能与安全风险 |
| [12](12-后端架构改造计划.md) | 后端架构改造计划 | **周密改造计划**：性能瓶颈优化 → 防崩防注入 → 缓存/并发升级 |
| [14](14-代码审计报告.md) | 代码审计报告 | 对当前代码库的独立审计快照：严重缺陷/安全风险/架构问题/代码质量（20 项问题 + 优先级排序） |

---

## 项目速览

| 指标 | 数值 |
|------|------|
| 品类完成 | 10/10 |
| 种子数据 | 130+ items |
| 前端组件 | 10 Hero + 11 Card + 11 Editor + TypeIcon |
| 后端 Service | 16 个 |
| 后端 Controller | 7 个（+ GlobalExceptionHandler） |
| 后端 DTO | 4 个 |
| 数据库表 | **9 张**（含 3 张近期新增） |
| 数据抓取源 | 6（Steam / TMDB / IGDB / AniList / Bangumi / iTunes）+ SteamGridDB |
| RabbitMQ 队列 | 7 |
| 测试覆盖 | ⚠️ 1 集成启动测试（实质为 0） |
| 安全加固 | JWT SHA-256 + IP 限流 + Turnstile + 邀请码 + 保留用户名 |

---

## 技术栈

| 层 | 技术 | 版本 |
|----|------|------|
| 前端 | Vue 3 + Vite + Pinia + Vue Router 4 + Tailwind CSS 4 | 最新稳定 |
| 图标 | @iconify/vue | 5.0.1 |
| UI 组件 | Element Plus | 2.14.0 |
| 图表 | ECharts | 6.1.0 |
| 后端 | Spring Boot | 4.0.6 |
| ORM | MyBatis-Plus | 3.5.16 |
| 认证 | Spring Security + JWT (jjwt 0.12.6) | |
| 数据库 | MySQL 8.0（生产 MariaDB 11） | |
| 缓存 | Redis 7 | |
| 消息队列 | RabbitMQ 3.12 | |
| API 文档 | SpringDoc OpenAPI（Swagger UI） | |

---

## 快速启动

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

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:5173 |
| 后端 | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui/index.html |
| 默认管理员 | admin / 见 ADMIN_DEFAULT_PASSWORD 环境变量 |

---

## 当前阶段

```
阶段 10：上线冲刺（进行中）
  ✅ 安全加固（JWT/Admin密码/输入校验/IP限流/Turnstile/邀请码）
  ✅ 功能完善（批量操作/用户管理/品类管理/API设置/热度公式）
  ❌ Bug修复（AdminDashboard TypeIcon / GameEditorFields 警告）
  ❌ Schema 补全（invite_codes/app_settings → schema.sql）
  ❌ 测试建设（单元 + 集成）
  ❌ HTTPS 配置
  ❌ 生产部署验证
阶段 11：后端架构加固与性能重构（计划中）
  ✅ 第一阶段：防崩防注入（SQL注入、超时设置、索引补全）
  ❌ 第二阶段：并发高可用（第三方API并发优化、RabbitMQ容错）
  ❌ 第三阶段：性能与内存优化（接口分页、DTO瘦身、SQL聚合）
  ❌ 第四阶段：中间件与规范化（Redis序列化、JWT缓存解析、静态资源缓存）
```

详细计划见 [10-修复与上线计划.md](10-修复与上线计划.md) 与 [12-后端架构改造计划.md](12-后端架构改造计划.md)。
