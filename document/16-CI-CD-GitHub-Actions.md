# 16. CI/CD（GitHub Actions）

> DemoNet 持续集成 / 持续部署工作流设计与使用说明

---

## 一、总览

基于项目实际技术栈设计的 GitHub Actions 三阶段流水线：

| 阶段 | Job | 作用 | 依赖 |
|------|-----|------|------|
| 构建 | `frontend-build` | Vue 3 + Vite 编译，产出 `dist/` | — |
| 构建 | `backend-build` | Maven 打包（跳过测试），产出可执行 JAR | — |
| 测试 | `backend-test` | `@SpringBootTest` 集成测试（MySQL/Redis/RabbitMQ 服务容器） | `backend-build` |
| 部署 | `deploy` | 构建 Docker 镜像 → 推送 GHCR → SSH 部署 | `frontend-build` + `backend-test` |

```
        ┌─ frontend-build ─────────────────────────┐
        │                                          │
触发 ───┤                                          ├──▶ deploy ──▶ 生产服务器
        │                                          │   (仅 main)
        └─ backend-build ──▶ backend-test ─────────┘
```

### 触发条件

| 事件 | 构建前/后端 | 测试 | 部署 |
|------|:----------:|:----:|:----:|
| 推送到 `main` | ✅ | ✅ | ✅ |
| 向 `main` 发起 PR | ✅ | ✅ | ❌ |
| 手动触发 `workflow_dispatch` | ✅ | ✅ | ✅ |

> 纯文档变更（`document/**`、`*.md`）不触发流水线，避免浪费资源。

---

## 二、技术栈对应关系

| 项目技术栈 | CI 中的体现 |
|-----------|------------|
| Spring Boot 4.0.6 / JDK 17 | `actions/setup-java@v4` (temurin 17) + `mvn` |
| Maven 依赖管理 | `~/.m2/repository` 缓存（setup-java 内置） |
| MyBatis-Plus + Flyway | 测试时 Flyway 自动迁移建表 + 种子数据 |
| MySQL 8.0 / Redis 7 / RabbitMQ 3.12 | `backend-test` 的 service containers |
| Vue 3 + Vite 8 + pnpm 11 | `pnpm/action-setup@v4` + `setup-node` pnpm 缓存 |
| Docker Compose 生产部署 | GHCR 镜像 + `docker-compose.ci.yml` |

### 注意事项

1. **Maven Wrapper 不完整**：项目 `backend/` 下有 `mvnw` 脚本但缺少 `.mvn/wrapper/maven-wrapper.properties`，wrapper 无法正常工作。CI 直接使用 GitHub runner 预装的 `mvn`（ubuntu-latest 自带 Maven 3.x）。如需修复 wrapper，可执行 `mvn -N wrapper:wrapper`。

2. **前端 Dockerfile 不一致**：仓库 `frontend/Dockerfile` 使用 `npm ci` + `package-lock.json`，但项目实际使用 pnpm（`pnpm-lock.yaml`）。CI 部署阶段不复用该 Dockerfile，而是使用 `.github/docker/Dockerfile.frontend`（直接复制 CI 预构建的 `dist/`），既绕过了此问题又提升了效率。建议后续修复 `frontend/Dockerfile` 改用 pnpm。

---

## 三、缓存策略

| 缓存项 | 机制 | 缓存键 |
|--------|------|--------|
| Maven 本地仓库 | `actions/setup-java` 内置 `cache: maven` | `pom.xml` 哈希 |
| pnpm store | `actions/setup-node` 内置 `cache: pnpm` | `pnpm-lock.yaml` 哈希 |
| Docker 构建层 | `docker/build-push-action` 的 `type=gha` | 前端/后端独立 scope |

缓存命中后，依赖安装与镜像构建显著加速。

---

## 四、部署前置配置

部署 Job 默认**不执行**，需手动启用。配置步骤如下：

### 4.1 GitHub 仓库设置

**Settings → Secrets and variables → Actions → Secrets**（新建）：

| Secret 名 | 说明 |
|-----------|------|
| `DEPLOY_HOST` | 生产服务器 IP 或域名 |
| `DEPLOY_USER` | SSH 登录用户名 |
| `DEPLOY_SSH_KEY` | SSH 私钥（完整内容，含 BEGIN/END 行） |
| `DEPLOY_PATH` | 服务器上项目目录路径（如 `/opt/demonet`） |

**Settings → Secrets and variables → Actions → Variables**（新建）：

| Variable 名 | 值 | 说明 |
|-------------|----|------|
| `ENABLE_DEPLOY` | `true` | 启用部署 Job（设为 `false` 或删除则跳过部署） |
| `DEPLOY_PORT` | `22`（可选） | SSH 端口，默认 22 |

> `GITHUB_TOKEN`（推送镜像到 GHCR 用）由 GitHub 自动提供，无需手动配置。

### 4.2 生产服务器配置

1. **安装 Docker + Docker Compose**（v2+）

2. **克隆仓库**：
   ```bash
   git clone <仓库地址> /opt/demonet
   cd /opt/demonet
   ```

3. **创建 `.env`**（参考 `.env.example`，填入生产密钥），并追加两行：
   ```env
   REGISTRY_OWNER=<你的 GitHub 用户名（小写）>
   IMAGE_TAG=latest
   ```

4. **登录 GHCR**（拉取镜像用）：
   - 若镜像设为**公开**：跳过此步
   - 若镜像为**私有**：创建 Personal Access Token（权限勾选 `read:packages`），然后：
     ```bash
     echo <PAT> | docker login ghcr.io -u <GitHub用户名> --password-stdin
     ```

5. **首次部署**（手动）：
   ```bash
   docker compose -f docker-compose.ci.yml pull
   docker compose -f docker-compose.ci.yml up -d
   ```

完成以上配置后，每次推送到 `main` 分支将自动触发部署。

---

## 五、镜像与版本管理

CI 推送两个镜像到 GHCR：

| 镜像 | 用途 |
|------|------|
| `ghcr.io/<owner>/demonet-frontend` | Nginx 静态服务 |
| `ghcr.io/<owner>/demonet-backend` | Spring Boot 应用 |

每个镜像打两个 tag：

- `latest` — 服务器默认拉取
- `<commit-sha-7位>` — 用于回滚定位

### 回滚操作

```bash
# 在服务器上
cd /opt/demonet
# 修改 .env 中的 IMAGE_TAG 为目标 commit SHA
# 例如：IMAGE_TAG=a1b2c3d
docker compose -f docker-compose.ci.yml pull
docker compose -f docker-compose.ci.yml up -d
```

---

## 六、文件清单

| 文件 | 说明 |
|------|------|
| `.github/workflows/ci-cd.yml` | 主工作流（构建/测试/部署） |
| `.github/docker/Dockerfile.frontend` | 前端部署镜像（复用 CI 构建的 dist） |
| `.github/docker/Dockerfile.backend` | 后端部署镜像（复用 CI 构建的 JAR） |
| `docker-compose.ci.yml` | 服务器部署编排（使用 GHCR 镜像） |
| `document/16-CI-CD-GitHub-Actions.md` | 本文档 |

---

## 七、本地验证

推送前可在本地模拟 CI 流程：

```bash
# 前端
cd frontend && pnpm install --frozen-lockfile && pnpm build

# 后端（需先启动 docker compose up -d）
cd backend && mvn -B -ntp test
```

---

## 八、后续优化建议

- [ ] 修复 `frontend/Dockerfile` 改用 pnpm（消除 npm/pnpm 不一致）
- [ ] 补全 Maven Wrapper（`mvn -N wrapper:wrapper`），CI 改用 `./mvnw` 保证版本一致
- [ ] 前端添加单元测试（Vitest）后，在 `frontend-build` 中增加 `pnpm test` 步骤
- [ ] 部署阶段增加健康检查（部署后 curl 健康接口，失败则回滚）
- [ ] 配置 Slack / 企业微信通知（`workflow_run` 事件）
