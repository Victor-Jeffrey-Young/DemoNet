# DemoNet 文档导航

> **DemoNet** — 策展化的"试玩派对"，多品类聚合平台（游戏/电影/动漫/桌游/模型/书籍/音乐/数码/咖啡/线下体验）
> **最后更新**：2026-07-15

---

## 文档索引

### 核心文档

| 编号 | 文档 | 内容 |
|------|------|------|
| [01](01-项目概述.md) | 项目概述 | 定位、愿景、技术栈、当前状态 |
| [02](02-项目结构.md) | 项目结构 | 前后端完整目录树与文件清单 |
| [03](03-数据模型.md) | 数据模型 | 9 张表设计 + info_json 多态结构 |
| [04](04-API清单.md) | API 清单 | 全部端点（公开 / 需认证 / Admin） |
| [05](05-开发历程.md) | 开发历程 | 阶段 0-11 全部完成记录（实时更新） |
| [06](06-品类设计.md) | 品类设计 | 10 品类独立视觉语言设计文档 |
| [07](07-部署运维.md) | 部署运维 | Docker 部署、环境变量、测试策略、验收用例、上线清单与回滚 |
| [08](08-问题与待办.md) | 问题与待办 | 当前状态、后续待办，以及修复/架构/规范化历史方案附录 |
| [09](09-外部数据源集成.md) | 外部数据源集成 | 7 个外部 API（Steam/TMDB/IGDB/AniList/Bangumi/iTunes/SteamGridDB）+ CQI 咖啡数据源技术参考 |
| [10](10-代码质量与后端架构审查报告.md) | 代码质量审查 | 合并历史审计、代码审查与后端风险详表，按五个维度评估当前状态 |
| [11](11-CI-CD-GitHub-Actions.md) | CI/CD 工作流 | 唯一流水线架构、部署配置、历史故障、排查方法、回滚与改进计划 |
| [19](19-测试最佳实践.md) | 测试最佳实践 | 认证 Mock 标准模式、RestClient Mock 链优化、测试基类设计原则 |

### 架构决策记录（ADR）

| 编号 | 文档 | 内容 |
|------|------|------|
| [ADR-001](decisions/ADR-001-JWT认证策略.md) | JWT 认证策略 | 为什么选择 JWT 而非 Session/OAuth2/API Key |
| [ADR-002](decisions/ADR-002-异步抓取管道.md) | 异步抓取管道 | 为什么选择 RabbitMQ 而非同步/Kafka/Redis Queue |
| [ADR-003](decisions/ADR-003-多态内容模型.md) | 多态内容模型 | 为什么选择 items + info_json 而非 TPT/MongoDB |
| [ADR-004](decisions/ADR-004-数据库选型.md) | 数据库选型 | 为什么选择 MySQL 8.0 而非 PostgreSQL/MongoDB/SQLite |
| [ADR-005](decisions/ADR-005-前端技术栈.md) | 前端技术栈 | 为什么选择 Vue 3 + Tailwind CSS 4 + Element Plus |
| [ADR-006](decisions/ADR-006-ORM选型.md) | ORM 选型 | 为什么选择 MyBatis-Plus 而非 JPA/MyBatis 原生/jOOQ |
| [ADR-007](decisions/ADR-007-缓存策略.md) | 缓存策略 | 为什么选择 Redis + Spring Cache 而非 Caffeine/Ehcache |
| [ADR-008](decisions/ADR-008-安全架构.md) | 安全架构 | 多层防护体系（认证 + 授权 + 限流 + 输入验证） |
| [ADR-009](decisions/ADR-009-Docker容器化策略.md) | Docker 容器化策略 | 为什么选择 Docker Compose 而非 Kubernetes/无容器化 |

### 项目报告

| 编号 | 文档 | 内容 |
|------|------|------|
| [19](19-测试最佳实践.md) | 测试最佳实践 | 认证 Mock 标准模式、RestClient Mock 链优化、测试基类设计原则 |
| [20](20-404错误处理优化报告.md) | 404 错误处理优化 | 后端异常处理器 + 前端 LazyImage 组件 + 可观测性实现 |
| [21](21-代码审查报告-75cbf5d.md) | 代码审查报告 | Commit 75cbf5d 全面审查（5+5 角度 × 8 个候选 → 10 个发现） |
| [22](22-测试简化总结报告.md) | 测试简化总结 | /simplify 任务完成报告：测试基类、Mock 优化、CI 收敛 |
| [23](23-Maven参考文档.md) | Maven 参考文档 | Spring Boot Maven 插件官方参考（自动生成） |
| [24](24-代码审查报告英文版.md) | 代码审查报告（英文） | Commit 75cbf5d 英文审查报告（原始版本） |
| [98](98-Claude-Code中文化配置修复记录.md) | Claude Code 中文化配置修复 | macOS 技能翻译不一致诊断与修复过程 |
| [99](99-文档整理完成报告.md) | 文档整理完成报告 | 文档重组过程、文档结构总览、下一步建议 |

---

## 项目速览

| 指标 | 数值 |
|------|------|
| 品类完成 | 10/10 |
| 种子数据 | 150+ items |
| 前端组件 | 10 Hero + 11 Card + 11 Editor + TypeIcon |
| 后端 Service | 16 个 |
| 后端 Controller | 7 个（+ GlobalExceptionHandler） |
| 后端 DTO | 6 个 |
| 后端 Entity | 8 个 |
| 数据库表 | 9 张（Flyway V1-V3 迁移管理） |
| 数据抓取源 | 7（Steam / TMDB / IGDB / AniList / Bangumi / iTunes / SteamGridDB） |
| RabbitMQ 队列 | 7 + 1 DLX + 1 DLQ |
| 测试覆盖 | 后端单元/Controller/集成测试；前端自动化测试仍待建设 |
| 安全加固 | JWT SHA-256 + IP 限流 + Turnstile + 邀请码 + 保留用户名 |
| Java 警告 | ✅ 0（2026-07-09 消除全部 127 个 WARNING） |

---

## 技术栈

| 层 | 技术 | 版本 |
|----|------|------|
| 前端 | Vue 3 + Vite + Pinia + Vue Router 4 + Tailwind CSS 4 | 最新稳定 |
| 图标 | @iconify/vue | 5.0.1 |
| UI 组件 | Element Plus | 2.14.0 |
| 图表 | ECharts | 6.1.0 |
| 后端 | Spring Boot | 4.0.6 |
| JDK | Java | 17 |
| JSON | Jackson 3（`tools.jackson.*`） | 3.1.2 |
| ORM | MyBatis-Plus | 3.5.16 |
| 认证 | Spring Security + JWT (jjwt 0.12.6) | |
| 数据库 | MySQL 8.0（生产 MariaDB 11） | |
| 缓存 | Redis 7（Spring Cache + @Cacheable） | |
| 消息队列 | RabbitMQ 3.12 | |
| API 文档 | SpringDoc OpenAPI（Swagger UI） | |
| DB 迁移 | Flyway（V1 Baseline + V2 Indexes + V3 FK） | |

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
| RabbitMQ 管理 | http://localhost:15672 |
| 默认管理员 | admin / 见 .env 中 ADMIN_DEFAULT_PASSWORD |

---

## 当前阶段

```
✅ 阶段 0-9：骨架 + CRUD + JWT + 搜索 + 抓取管道 + 10品类 + 平台基建 + 质量加固
✅ 阶段 10：上线冲刺（安全加固 + Schema补全 + Flyway + 架构清理 + 功能完善）
✅ 阶段 11：管理端重构 + 移动端体验优化 + Spring Boot 4 Jackson 3 迁移
✅ 架构改造第一阶段：SQL注入修复 + RestClient超时 + 数据库索引 + 外键级联 + Redis连接池 + RabbitMQ DLQ + 文件上传限制
🚧 架构改造第二阶段：外部API并行化 + IGDB Token缓存 + API Key缓存
🚧 架构改造第三阶段：列表分页 + DTO瘦身 + Review聚合SQL
📋 架构改造第四阶段：JWT单次解析 + 精细缓存TTL + 静态资源缓存
📋 测试建设：单元/集成测试
📋 功能增强：Elasticsearch + 定时抓取 + Light Mode
```

详细计划见 [08-问题与待办.md](08-问题与待办.md#历史方案附录)。
