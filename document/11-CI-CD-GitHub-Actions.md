# 16. CI/CD（GitHub Actions）

> DemoNet CI/CD 的唯一维护文档。涵盖当前架构、配置、操作手册、故障记录、排查方法、回滚与后续改进。最后更新：2026-07-14。

---

## 一、当前架构

仓库只保留一个 GitHub Actions 工作流：`.github/workflows/ci-cd.yml`。此前独立存在的 `ci.yml`、`docker-build.yml` 和 `deploy.yml` 已于提交 `75cbf5d` 删除，避免重复构建以及绕过测试门禁。

| 阶段 | Job | 作用 | 依赖 |
|------|-----|------|------|
| 构建 | `frontend-build` | 安装锁定依赖、构建 Vue/Vite，上传 `frontend-dist` | 无 |
| 构建 | `backend-build` | Maven 打包，上传 `backend-jar` | 无 |
| 测试 | `backend-test` | 在 MySQL、Redis、RabbitMQ 服务容器下运行后端测试 | `backend-build` |
| 部署 | `deploy` | 下载产物、构建并推送 GHCR 镜像、SSH 部署 | `frontend-build`、`backend-test` |

```text
        ┌─ frontend-build ─────────────────────────┐
触发 ───┤                                          ├── deploy ── 生产服务器
        └─ backend-build ── backend-test ──────────┘
```

质量门禁的核心约束：

- 前后端构建并行执行。
- 后端测试必须在后端成功编译后执行。
- 镜像构建、SHA/`latest` 标签发布和 SSH 部署都位于 `deploy` Job 内；生产 Compose 只使用 SHA 标签。
- 任一上游构建或测试失败，`deploy` 不会运行。
- `deploy` 还要求事件为 `main` 分支 push，且仓库变量 `ENABLE_DEPLOY` 等于 `true`。

### 触发规则

| 事件 | 构建 | 测试 | 部署 |
|------|:----:|:----:|:----:|
| push 到 `main` | 是 | 是 | 条件执行 |
| PR 指向 `main` | 是 | 是 | 否 |
| `workflow_dispatch` | 是 | 是 | 否（当前部署条件仅允许 push） |
| 仅修改 `document/**`、Markdown、`.gitignore` 或 `LICENSE` | 否 | 否 | 否 |

同一分支的新运行通过 `concurrency` 取消旧运行，但 `main` 不取消，避免部署执行到一半被中断。

---

## 二、运行环境与产物

| 项目 | CI 配置 |
|------|---------|
| Java | Temurin JDK 17，`actions/setup-java@v4` |
| 后端 | Spring Boot 4.0.6，Maven，MyBatis-Plus 3.5.16 |
| Node.js | 22 |
| 前端 | Vue 3、Vite、pnpm 11.10.0 |
| MySQL | `mysql:8.0` 服务容器，端口 3306 |
| Redis | `redis:7-alpine` 服务容器，端口 6379 |
| RabbitMQ | `rabbitmq:3.12-management-alpine` 服务容器，端口 5672 |
| 镜像仓库 | GitHub Container Registry（GHCR） |

### 缓存

| 缓存项 | 实现 | 依据 |
|--------|------|------|
| Maven 本地仓库 | `setup-java` 的 `cache: maven` | `pom.xml` |
| pnpm store | `setup-node` 的 `cache: pnpm` | `frontend/pnpm-lock.yaml` |
| Docker 构建层 | `docker/build-push-action` 的 `type=gha` | 前后端独立 scope |

### 构建产物

| 产物 | 来源 | 保留时间 | 消费方 |
|------|------|----------|--------|
| `frontend-dist` | `frontend/dist/` | 7 天 | 前端镜像构建 |
| `backend-jar` | `backend/target/*.jar` | 7 天 | 后端镜像构建 |
| `backend-test-reports` | Surefire 报告 | 14 天 | 测试失败排查 |

后端构建 Job 使用 `-DskipTests` 只验证编译和打包；真正的测试门禁由 `backend-test` 执行 `mvn -B -ntp test`。不能为了让流水线变绿而删除、禁用或跳过失败测试。

---

## 三、部署配置

部署默认关闭。启用前应确保 CI 全绿，并在 GitHub 仓库中完成以下设置。

### GitHub Actions Secrets

| Secret | 说明 |
|--------|------|
| `DEPLOY_HOST` | 生产服务器 IP 或域名 |
| `DEPLOY_USER` | SSH 用户名 |
| `DEPLOY_SSH_KEY` | 完整 SSH 私钥 |
| `DEPLOY_PATH` | 服务器上的仓库路径，如 `/opt/demonet` |

`GITHUB_TOKEN` 由 GitHub 自动提供，工作流通过 `packages: write` 权限将镜像推送到 GHCR，不需要手工保存该 Token。

### GitHub Actions Variables

| Variable | 值 | 说明 |
|----------|----|------|
| `ENABLE_DEPLOY` | `true` | 生产部署总开关；缺失或非 `true` 时跳过 |
| `DEPLOY_PORT` | 可选，默认 `22` | SSH 端口 |

### 生产服务器

1. 安装 Docker 和 Docker Compose v2。
2. 将仓库克隆到 `DEPLOY_PATH`（会自动跟踪 `scripts/deploy/*.sh`，无需额外下载）。
3. 根据 `.env.example` 创建生产 `.env`。**以下变量为必填**，缺失会让
   Compose 在启动时报 `<VAR>` 未设置：
   - `REGISTRY_OWNER`：小写 GitHub 用户名（镜像仓库所有者）。
   - `REDIS_PASSWORD`：Redis 认证密码。`docker-compose.ci.yml` 第 340 行强制
     `redis-server --requirepass ${REDIS_PASSWORD:?...}`，不再接受空密码。
   - `MYSQL_ROOT_PASSWORD`、`MYSQL_PASSWORD`、`RABBITMQ_PASS`：各中间件凭据，
     生产环境不得复用 `changeme`。
4. 设置 `IMAGE_TAG` 和发布状态：**首次部署时不用手动创建 `.env.deploy`**，
   `scripts/deploy/deploy.sh` 检测到该文件不存在时会自动写占位回滚锚点
   （无需手工准备）。
5. 私有镜像需要 GHCR 登录：

```bash
echo <PAT> | docker login ghcr.io -u <GitHub用户名> --password-stdin
```

首次部署：

```bash
cd "$DEPLOY_PATH"
chmod +x scripts/deploy/*.sh
NEW_IMAGE_TAG=<提交的短 SHA，7 位十六进制>
NEW_DEPLOY_SHA=<完整提交 SHA，40 位十六进制>
scripts/deploy/deploy.sh
```

注意：`docker-compose.ci.yml` 对 `IMAGE_TAG` 使用 `${IMAGE_TAG:?...}` 校验，
**不再接受 `latest`**；必须使用 7 位 SHA。部署脚本内置健康检查与冒烟测试；
失败会自动回滚到 `.env.deploy` 中记录的上一个成功 SHA。

自动部署时，工作流检出本次 run 的精确 `${{ github.sha }}`，调用
`scripts/deploy/deploy.sh` 拉取同一 SHA 的镜像，等待健康检查并执行冒烟测试；
失败时尝试恢复 `.env.deploy` 记录的上一成功 SHA。

---

## 四、镜像、版本与回滚

| 镜像 | 标签 |
| --- | --- |
| `ghcr.io/<owner>/demonet-frontend` | 提交 SHA 前 7 位 |
| `ghcr.io/<owner>/demonet-backend` | 提交 SHA 前 7 位 |

> 生产镜像只使用 SHA 标签；`docker-compose.ci.yml` 对 `IMAGE_TAG`
> 强制校验，不再接受 `latest`。部署与回滚必须使用 SHA。

正常情况下由 `scripts/deploy/rollback.sh` 自动恢复 `.env.deploy` 中的上一成功版本；人工恢复示例：

```bash
cd /opt/demonet
# 查看 .env.deploy 中的 PREVIOUS_IMAGE_TAG / PREVIOUS_DEPLOY_SHA，确认后写入 IMAGE_TAG / DEPLOY_SHA
scripts/deploy/rollback.sh <上一成功 SHA，7 位> <上一完整提交 SHA，40 位>
```

或在 `.env.deploy` 中手动写入经过确认的 SHA 标签与完整提交 SHA 后执行：

```bash
docker compose --env-file .env --env-file .env.deploy \
  -f docker-compose.ci.yml pull
docker compose --env-file .env --env-file .env.deploy \
  -f docker-compose.ci.yml up -d
```

发生流水线或生产故障时，可先将 `ENABLE_DEPLOY` 设为 `false`，阻止后续提交继续部署。

---

## 五、本地验证

### 完整验证

后端完整测试需要 MySQL、Redis 和 RabbitMQ。未启动 MySQL 时，`DemoNetApplicationTests` 会以 `Communications link failure` 失败，这不是可以忽略的测试失败，而是缺少测试基础设施。

```bash
docker compose up -d

cd backend
mvn -B -ntp test
mvn -B -ntp package -DskipTests

cd ../frontend
pnpm install --frozen-lockfile
pnpm build
```

### 工作流静态检查

提交工作流变更前至少执行 YAML 解析检查；建议在项目中固定引入 `actionlint`。临时检查示例：

```bash
pnpm dlx yaml-lint .github/workflows/ci-cd.yml
```

### 远程状态检查

```bash
gh run list --limit 10
gh run view <run-id>
gh run view <run-id> --log-failed
gh run watch <run-id> --exit-status
gh run download <run-id> -n backend-test-reports -D tmp-reports
```

如果 `gh run list --commit <sha>` 异常返回 404，可先使用不带 `--commit` 的列表命令确认运行，再从列表中的完整 SHA 或 run ID 查询；不要把状态查询失败误判为 push 失败。

---

## 六、故障记录

### 6.1 2026-07-10：pnpm 11 与 Node 20 不兼容

**现象**：前端 Job 在安装 Node.js 阶段出现：

```text
This version of pnpm requires at least Node.js v22.13
Error [ERR_UNKNOWN_BUILTIN_MODULE]: No such built-in module: node:sqlite
```

**根因**：pnpm 11.10.0 使用 Node 22 提供的 `node:sqlite`，工作流仍使用 Node 20。

**修复**：提交 `23d75cd` 将 `NODE_VERSION` 从 20 升到 22。

**经验**：升级包管理器时必须同步核对其 Node.js 最低版本，不能沿用 Runner 的旧默认值。

### 6.2 2026-07-10：Spring Boot 4 未自动运行 Flyway

**现象**：全新 CI 数据库没有任何表，`DataImportService` 启动时查询 `users` 表并触发 `BadSqlGrammarException`；Surefire 显示 3 个 ApplicationContext 错误。

**诊断**：

1. 确认 V1、V2、V3 SQL 迁移文件已提交且进入 `target/classes/db/migration`。
2. `SHOW TABLES` 为空，排除迁移文件缺失以外的问题。
3. Flyway DEBUG 和自动配置报告中没有 `FlywayAutoConfiguration`，确认自动配置未触发。

**根因**：Spring Boot 4 中单独依赖 `flyway-core` 不再提供所需的 Boot 自动配置集成；本地旧数据库已有表，掩盖了问题。

**修复**：提交 `be486ba` 用 `spring-boot-starter-flyway` 替换直接的 `flyway-core`，继续保留 `flyway-mysql`。提交 `05c155b` 在验证成功后移除了临时诊断步骤。

**经验**：全新数据库是迁移链路的真实验收环境；“本地已有表时能启动”不能证明 Flyway 配置正确。

### 6.3 2026-07-11 至 2026-07-12：Mockito 与 Controller 测试连续失败

**最终现象**：提交 `1191e6b` 的 CI 共运行 96 个测试，出现 7 个 failure 和 1 个 error：

- `ReviewControllerTest`：创建、删除预期 200，实际 500。
- `UserItemControllerTest`：保存、列表、查询、删除共 5 个用例预期 200，实际 500。
- `IGDBServiceTest.fetchGameById_emptyResponse`：`UnnecessaryStubbingException`。

**Controller 根因**：Controller 参数类型为 `Authentication`。Standalone MockMvc 的标准 `PrincipalMethodArgumentResolver` 从请求 principal 解析该参数；`AuthenticationPrincipalArgumentResolver` 面向 `@AuthenticationPrincipal`，追加自定义解析器也不会取代先匹配的标准解析器，因此 Controller 收到 `null`。

**Controller 修复**：成功路径使用 MockMvc 请求的 `.principal(authentication)`；参数校验失败路径不注入认证对象，保持测试职责单一。空返回体用 `content().string("")` 断言，避免 JsonPath 对空文本求值。

**IGDB 根因**：实际 RestClient 链为 `.header(...).body(bodyStr).retrieve()`，测试没有正确模拟 `body(Object)` 的返回值，链条在 `retrieve()` 前中断，后续 stub 又被严格模式认定为未使用。

**IGDB 修复**：补齐 `body(any(Object.class))` 链式返回，并只对双重 token 查询带来的非必经 stub 使用宽松模式。

**验证**：目标 17 个回归测试全部通过；前端生产构建、后端 JAR 打包和工作流 YAML 校验通过。相关修复提交为 `75cbf5d`。

### 6.4 2026-07-12：四套工作流互相竞争

**现象**：同一提交同时触发 `ci.yml`、`ci-cd.yml`、`docker-build.yml`、`deploy.yml`：

- 主 CI 后端测试失败。
- `ci.yml` 在创建 Job 前失败，没有 Job 日志。
- Docker 工作流仍成功并可更新镜像。
- 独立部署只依赖 Docker 工作流，理论上能绕过主 CI 测试门禁。

**根因**：工作流经过多轮演进后，新旧方案并存，构建、测试、镜像和部署之间没有单一依赖图。

**决策**：以 `ci-cd.yml` 为唯一事实来源，将镜像构建和部署放在同时依赖前端构建与后端测试的 `deploy` Job 中；删除另外三套工作流。

**后果**：每个提交只产生一套 Check，失败提交不会覆盖生产 `latest` 镜像，分支保护只需绑定唯一 CI/CD Check。

### 6.5 2026-07-12：HTTPS push 无法读取用户名

**现象**：非交互推送失败：

```text
fatal: could not read Username for 'https://github.com': terminal prompts disabled
```

**诊断结果**：`gh auth status` 登录有效，`gh auth git-credential get` 能返回凭据，但 `git credential fill` 不能；全局 Git 同时存在 `manager`、空 helper 和 GitHub CLI helper，当前 Git 进程没有正确取得令牌。

**修复**：执行 `gh auth setup-git` 重写凭据助手配置，随后成功将 `1191e6b..75cbf5d` 推送到 `origin/main`。

**安全要求**：排查凭据时只检查是否返回 `username`/`password` 字段，禁止把 Token 输出到日志、文档或聊天中。

### 6.6 2026-07-13：本地 Maven Wrapper 缺失配置

**现象**：执行 `backend\\mvnw.cmd -Dtest=... test` 在编译前失败，提示找不到 `.mvn/wrapper/maven-wrapper.properties`。

**影响范围**：GitHub Actions 当前直接调用 Maven，不受此问题影响；本地开发者和自动化工具无法按约定使用仓库 Wrapper，容易因系统 Maven 版本不同产生不可复现的结果。

**本轮处置**：为避免中断运行中的开发服务，已使用本机 Maven 3.9.16 完成目标测试。没有伪造或绕过测试，也没有修改正在运行的应用。

**待办**：在独立、可验证的工具链修复提交中恢复并提交 Wrapper 配置（含 Maven 版本与校验信息），再以 `mvnw.cmd -v` 和后端目标测试验收；修复前，本地验证命令应明确使用已安装的 Maven。

---

## 七、标准排查流程

1. 用 `gh run list` 确认失败的是哪个工作流和提交。
2. 用 `gh run view <run-id>` 定位失败 Job 和步骤。
3. 用 `--log-failed` 获取最小错误片段，不要只看最终 exit code。
4. 测试失败时下载 `backend-test-reports`，查看 Surefire 的失败汇总和首个根异常。
5. 区分编译失败、测试断言失败、ApplicationContext 失败、服务容器失败和 YAML 加载失败。
6. 日志不足时临时增加诊断步骤，输出文件是否存在、服务健康状态和自动配置匹配结果；定位后删除诊断噪音。
7. 在本地复现具体失败测试，修复后先跑目标测试，再跑完整套件。
8. 推送后用 `gh run watch --exit-status` 验证远程结果。

常见判断：

| 现象 | 优先检查 |
|------|----------|
| 运行立即失败且没有 Job | YAML、表达式、工作流文件路径 |
| MySQL `Connection refused` | 服务容器、端口、健康检查、环境变量 |
| 表不存在 | Flyway starter、迁移文件、profile、空数据库验证 |
| Mockito `UnnecessaryStubbing` | 实际调用链、重载匹配、stub 是否属于当前场景 |
| Controller 返回 500 | 全局异常日志、参数解析、认证对象是否真实注入 |
| Docker 成功但测试失败 | 工作流依赖图是否允许绕过质量门禁 |
| HTTPS push 要求用户名 | `gh auth status`、`git credential fill`、credential helper |

---

## 八、企业级差距与个人项目演进路线

### 8.1 评估结论

当前工作流已经达到“个人项目可持续使用”的水平：有锁定依赖安装、并行构建、真实中间件集成测试、构建产物复用、部署开关和失败阻断。与企业级生产流水线相比，主要不足集中在部署一致性、质量门禁、供应链安全、环境治理和故障恢复。

本项目由个人维护，不需要照搬大型企业的多团队审批、复杂发布委员会或昂贵平台。改进原则是：

1. 优先解决可能导致错误版本上线、服务不可用或凭据泄漏的问题。
2. 优先采用 GitHub Actions、GitHub Environment 和开源工具能直接提供的能力。
3. 每个新增门禁都应自动化、可复现，并且维护成本低于它预防的问题成本。
4. 暂不引入 Kubernetes、专用制品平台或复杂多云发布系统。

综合成熟度约为 **2.5/5**：基础 CI/CD 已成立，但生产发布保护仍不完整。

### 8.2 P0：代码已实现，待服务器条件验收

| P0 能力 | 仓库实现状态 | 真实环境状态 |
|---------|--------------|--------------|
| Actuator health/probes 与安全限制 | 已实现并通过后端集成测试 | 待生产容器验证 |
| 基础设施、后端、前端 healthcheck | 已写入 Compose | 待完整生产镜像启动验证 |
| SHA 不可变部署与 `.env.deploy` | 已实现，Compose 拒绝缺失 `IMAGE_TAG` | 待首次服务器部署 |
| 部署后 readiness/首页/公开 API 冒烟测试 | 已实现 | 待真实网络与容器验证 |
| 上一成功 SHA 自动回滚 | 已实现脚本与状态单元测试 | 待可控失败演练 |
| SSH 主机指纹 | 工作流已要求 `DEPLOY_FINGERPRINT` | 因尚无服务器，无法配置和验证 |

在租用服务器并完成生产演练前，必须保持 `ENABLE_DEPLOY=false`。当前实现属于“部署代码就绪”，不能宣称生产闭环已经验收完成。

#### 不可变版本部署

旧流程在服务器执行 `git reset --hard origin/main`，并依赖可变 `latest`，可能造成配置和镜像错配。现已改为传递完整 `${{ github.sha }}`，服务器检出该精确提交，并通过必填的 SHA `IMAGE_TAG` 部署对应前后端镜像。

改进要求：

- 生产部署只使用提交 SHA 或镜像 digest，不使用 `latest` 作为最终部署依据。
- 将 `${{ github.sha }}` 对应的 `IMAGE_TAG` 显式传入服务器。
- 服务器检出本次运行的精确 SHA，或彻底取消部署对服务器 Git 工作区的依赖。
- 在 Job Summary 记录提交 SHA、镜像 digest、部署时间和结果。

验收标准：生产容器标签、服务器配置和 GitHub Actions run 能一一对应。

#### 部署后健康检查与自动回滚

旧流程只执行 `docker compose up -d`。现已增加 Actuator readiness、容器 healthcheck、前端首页和公开 API 冒烟检查，并在失败时输出受限诊断日志和触发回滚。

改进要求：

- 后端提供稳定的健康检查端点，部署后轮询至 ready。
- 检查前端首页和至少一个无需认证的核心 API。
- 设置明确的启动超时和观察窗口。
- 检查失败时自动恢复上一成功 SHA，并再次验证健康状态。
- 保存失败容器状态和最近日志作为 artifact 或 Job Summary。

验收标准：健康检查失败会让部署 Job 失败，并自动恢复上一已知可用版本。

#### SSH 主机身份校验

工作流已把 `DEPLOY_FINGERPRINT` 传给 `appleboy/ssh-action`。由于目前没有服务器，尚无法取得可信指纹或验证错误指纹会拒绝连接；缺少该 Secret 时不得启用部署。

验收标准：主机指纹不匹配时连接立即失败。

### 8.3 P1：高收益企业级能力

#### GitHub Production Environment

为部署 Job 增加：

```yaml
environment:
  name: production
```

将 `DEPLOY_HOST`、`DEPLOY_USER`、`DEPLOY_SSH_KEY`、`DEPLOY_PATH` 迁移为 production Environment Secrets。个人项目可以不设置多人审批，但应保留：

- 环境级 Secrets 隔离。
- 部署历史。
- 可选的人工确认。
- 生产环境 URL。

涉及数据库迁移、认证、安全配置或基础设施变更时，可临时启用 required reviewer，由维护者本人确认。

#### 前端质量门禁

当前前端只有 `pnpm build`，能够打包不等于行为正确。建议依次补充：

1. ESLint。
2. `vue-tsc --noEmit` 类型检查。
3. Vitest 单元测试。
4. 覆盖率基线，初期只要求不下降，再逐步提高。
5. 登录、列表、详情、收藏和 Admin 核心路径的少量 Playwright E2E。
6. Bundle 大小告警，重点关注 ECharts 和主入口大包。

验收标准：lint、类型、单元测试或生产构建任一失败都会阻断发布。

#### 后端质量门禁

现有 96 个测试是良好基础，建议增加：

- JaCoCo 覆盖率报告和最低阈值。
- Checkstyle 或 Spotless 格式检查。
- SpotBugs 静态缺陷检查。
- Flyway migration 校验。
- Maven 依赖漏洞检查。
- 编译警告和弃用 API 的定期清理。

个人项目不必一次启用所有严格规则。建议先“生成报告但不阻断”，清理存量问题后再将新增问题设为阻断。

#### Action 与镜像供应链安全

当前第三方 Action 使用 `@v4`、`@v6`、`@v1` 等可变标签，服务容器也使用可变镜像标签。

改进要求：

- 第三方 Action 固定到完整 commit SHA，旁边注释对应版本号。
- MySQL、Redis、RabbitMQ 和基础镜像逐步固定 digest。
- 使用 Dependabot 或 Renovate 自动提交更新。
- 构建镜像后运行 Trivy 或 Grype 扫描，高危漏洞阻断发布。
- 生成 SBOM；后续可使用 Cosign 签名并在部署前验签。

最优先固定能读取生产 SSH 私钥的 `appleboy/ssh-action`。

#### 最小权限

工作流顶层应设置：

```yaml
permissions:
  contents: read
```

仅发布镜像的 Job 单独提升 `packages: write`。这样即使仓库默认 Token 权限改变，构建和测试 Job 也不会获得多余写权限。

### 8.4 P2：完善发布治理

#### 拆分镜像发布与生产部署

当前镜像构建、推送和 SSH 部署位于同一个 `deploy` Job。关闭生产部署时无法只发布镜像，重新部署也会重复构建。

推荐依赖图：

```text
frontend-quality ─┐
backend-quality  ─┼─ publish-images ─ deploy-production ─ smoke-test
integration-test ─┘
```

`publish-images` 必须依赖所有质量门禁，不能恢复为无测试依赖的旧 Docker 工作流。最终达到“一次构建，按相同 digest 部署”的目标。

#### staging 或轻量预览环境

个人项目无需长期维护完整 staging 集群，可以选择：

- 在同一服务器使用独立 Compose project 和端口建立临时 staging。
- 仅对高风险提交手动部署 staging。
- PR 只运行构建和 E2E，不创建长期预览环境。

生产部署前至少应有一次基于最终镜像的 smoke test。

#### 超时、并发和失败诊断

- 为构建、测试、镜像发布和 SSH 部署设置 `timeout-minutes`。
- MySQL 等待循环耗尽后显式退出非零状态，不要继续执行测试掩盖根因。
- 将生产部署 concurrency 与普通 CI 分离，确保同一时间只有一个生产部署。
- 避免旧提交排队后依次部署；发布阶段应以最新通过门禁的提交为准。
- 失败时输出容器状态、健康检查结果和有限量日志。

#### 仓库治理

建议在 GitHub 设置中启用：

- `main` 分支保护。
- CI/CD 必需状态检查。
- 禁止 force push。
- Secret scanning 和 push protection。
- Dependabot alerts。
- CodeQL（若私有仓库套餐和权限允许）。

当前通过 API 无法确认这些仓库级设置是否已启用，因此实施时需要在 GitHub Settings 页面逐项核对。

### 8.5 暂不建议引入

以下能力对当前个人网站收益有限，维护成本可能高于价值：

- Kubernetes、Helm 和多集群发布。
- 自建 Jenkins、GitLab Runner 或制品仓库。
- 多人发布委员会和复杂变更审批流。
- 全量微服务矩阵测试。
- 多地域金丝雀和流量切分平台。
- 为追求形式完整而设置大量长期空闲环境。

只有当访问量、协作者数量、可用性目标或合规要求明显提升时再评估这些能力。

### 8.6 推荐实施顺序

| 阶段 | 内容 | 预期收益 |
|------|------|----------|
| 1 | SHA/digest 部署、健康检查、自动回滚、SSH 指纹 | 消除主要生产事故风险 |
| 2 | Production Environment、最小权限、Action 固定 SHA | 降低凭据和供应链风险 |
| 3 | 前端 lint/类型/Vitest、后端覆盖率/静态分析 | 提升合并前缺陷发现能力 |
| 4 | 镜像扫描、SBOM、Dependabot、分支保护 | 建立持续安全治理 |
| 5 | 拆分 publish/deploy、轻量 staging、E2E smoke test | 提升发布可重复性和可信度 |

每个阶段独立提交和验证，不应一次性重写全部流水线。

---

## 九、相关文件

| 文件 | 说明 |
|------|------|
| `.github/workflows/ci-cd.yml` | 唯一 GitHub Actions 工作流 |
| `.github/docker/Dockerfile.frontend` | 复制预构建 `dist/` 的前端镜像 |
| `.github/docker/Dockerfile.backend` | 复制预构建 JAR 的后端镜像 |
| `docker-compose.ci.yml` | GHCR 镜像的服务器编排 |
| `document/07-部署运维.md` | 本地开发、生产 Compose、环境变量与监控 |
| `document/11-CI-CD-GitHub-Actions.md` | 本文档 |
| `tasks/plan.md` | P0 生产发布闭环的分阶段实施计划 |
| `tasks/todo.md` | P0 修复执行清单与检查点 |
