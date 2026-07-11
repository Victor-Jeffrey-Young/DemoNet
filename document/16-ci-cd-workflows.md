# DemoNet CI/CD 工作流程指南

## 工作流总览

| 工作流 | 触发条件 | 作用 |
|--------|---------|------|
| `CI` | push/PR → main | 后端编译+集成测试，前端构建，产出产物 |
| `构建 Docker 镜像` | push → main / tag | 复用 CI 产物打包镜像，推送到 GHCR |
| `部署` | 手动触发 / docker-build 完成后 | 拉取最新镜像并 SSH 部署到服务器 |

## CI 工作流

**文件：** `.github/workflows/ci.yml`

```
┌─────────────────────────────────────────────────────────┐
│  CI workflow                                            │
│  ┌─────────────────┐   ┌────────────────────────────┐  │
│  │ 后端测试 (🧪)     │   │ 前端构建 (🎨)              │  │
│  │ JDK 17 + Maven   │   │ Node.js 22 + pnpm          │  │
│  │ MySQL 8 + Redis 7│   │ pnpm install + pnpm build   │  │
│  │ mvn test -Pci    │   │                            │  │
│  │ mvn package      │   │                            │  │
│  ├─────────────────┤   ├────────────────────────────┤  │
│  │ 产物: *.jar      │   │ 产物: dist/                │  │
│  └─────────────────┘   └────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

**服务容器：**
- `mysql:8.0`（端口 3306，密码 `citest`）
- `redis:7-alpine`（端口 6379）

**环境变量（CI 专用）：**
```yaml
JWT_SECRET: ci-test-jwt-secret-for-ci-please-override-256bit-min-here
ADMIN_DEFAULT_PASSWORD: citest
SPRING_FLYWAY_ENABLED: 'true'
```

## Docker 镜像构建工作流

**文件：** `.github/workflows/docker-build.yml`

推送到 main 或 v* tag 时触发，从 CI 产物构建生产镜像。

**构建方式**（不同于本地 Dockerfile）：
- 前端：使用 `.github/docker/Dockerfile.frontend`，直接复制预构建 `dist/` 到 nginx 镜像
- 后端：使用 `.github/docker/Dockerfile.backend`，直接复制预构建 `app.jar` 到 JRE 镜像

**镜像标签：**
- `ghcr.io/<owner>/demonet-frontend:latest`
- `ghcr.io/<owner>/demonet-frontend:<sha>`
- `ghcr.io/<owner>/demonet-backend:latest`
- `ghcr.io/<owner>/demonet-backend:<sha>`

## 部署工作流

**文件：** `.github/workflows/deploy.yml`

### 触发方式

1. 手动触发（GitHub Web UI → Actions → 部署 → Run workflow）
2. docker-build 成功后自动触发

### 前置要求

仓库 Settings 需配置以下 Secrets：

| 名称 | 说明 |
|------|------|
| `DEPLOY_HOST` | 服务器 IP 或域名 |
| `DEPLOY_USER` | SSH 用户名 |
| `DEPLOY_SSH_KEY` | SSH 私钥 |
| `DEPLOY_PATH` | 仓库在服务器上的路径 |

以及以下 Variables：

| 名称 | 值 | 说明 |
|------|-----|------|
| `ENABLE_DEPLOY` | `true` | 部署功能开关 |

服务器需满足：
- 已安装 Docker + Docker Compose
- 已克隆本仓库并配置 `.env`
- 已登录 GHCR（或有权拉取镜像）

### 部署流程

1. 拉取最新代码 → `git reset --hard origin/main`
2. 拉取最新镜像 → `docker compose pull`
3. 重启服务 → `docker compose up -d`
4. 清理旧镜像 → `docker image prune -f`

## 本地测试命令

```bash
# 后端测试（使用 ci profile）
cd backend
mvn test -Pci

# 前端构建
cd frontend
pnpm install --frozen-lockfile
pnpm build
```

## 排错指南

### 1. 测试失败

检查 `actions/upload-artifact@v4` 上传的测试报告产物，下载后查看 `surefire-reports/*.txt`。

### 2. Docker 构建失败

- 确认 CI 产物已正确上传（JAR、dist）
- 确认 `.github/docker/` 目录下有对应的 Dockerfile
- 确认 `docker/login-action@v3` 的 GHCR 登录正常

### 3. 部署失败

- 检查服务器的 SSH 连接是否正常
- 确认 `DEPLOY_PATH` 路径存在且已克隆仓库
- 检查服务器 Docker 服务是否运行
- 确认服务器已 `docker login ghcr.io`
