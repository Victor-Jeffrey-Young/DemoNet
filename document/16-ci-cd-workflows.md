# DemoNet CI/CD 工作流程指南

本文档说明 DemoNet 项目的 GitHub Actions 工作流配置和使用方式。

## 工作流总览

| 工作流文件 | 触发条件 | 作用 |
|-----------|---------|------|
| `ci.yml` | push/PR → main | 编译后端测试 + 前端构建 |
| `docker-build.yml` | push → main / tag v* | 构建并推送 Docker 镜像到 GHCR |
| `deploy.yml` | 手动触发 (workflow_dispatch) | 部署到生产/预发布服务器 |

## 1. CI 工作流 (ci.yml)

**触发条件：** `push` 到 `main` 分支或针对 `main` 的 `pull_request`

**Job：**

### backend-test
- 启动 MySQL 8.0 + Redis 7 服务容器
- JDK 17 + Maven 缓存
- 运行 `mvn clean test -Pci`
- 上传测试报告（Artifact，保留 7 天）

### frontend-build
- Node.js 20 + pnpm
- 安装依赖 & 构建
- main 分支上传构建产物（Artifact，保留 3 天）

### result
- 汇总各 job 结果到 GitHub Step Summary

### 本地模拟 CI 测试环境

```bash
# 启动 MySQL + Redis
docker run -d --name ci-mysql -e MYSQL_ROOT_PASSWORD=testpass -e MYSQL_DATABASE=demonet_test -p 3306:3306 mysql:8.0
docker run -d --name ci-redis -p 6379:6379 redis:7-alpine

# 运行测试
cd backend
mvn clean test -Pci
```

## 2. Docker 构建工作流 (docker-build.yml)

**触发条件：** push 到 `main` 或推送语义化版本 tag（如 `v1.2.3`）

**功能：**
- 并行构建 `backend/` 和 `frontend/` 两个镜像
- 推送到 `ghcr.io/{owner}/demonet-backend` 和 `demonet-frontend`
- 标签策略：
  - `main` 分支 → `latest`
  - 版本 tag → `vX.Y.Z`
  - 始终推送 SHA 标签
- 使用 Docker Buildx + layer 缓存加速构建

**前置条件：** 需要在 GitHub 仓库启用 GHCR（默认开启）。

**本地构建测试：**

```bash
# 构建并测试镜像
docker build -t demonet-backend:test ./backend
docker build -t demonet-frontend:test ./frontend

# 使用 CI 编排文件启动完整环境
docker compose -f docker-compose.ci.yml up -d
```

## 3. 部署工作流 (deploy.yml)

**触发条件：** 手动触发（GitHub Actions 页面 → Deploy → Run workflow）

**参数：**
| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `environment` | choice | production | production / staging |
| `image_tag` | string | latest | 要部署的镜像标签 |

**功能：**
- SSH 连接到服务器
- 拉取最新镜像（`docker compose pull`）
- 零停机重启 backend + frontend
- 健康检查（最多等待 2 分钟）
- 输出部署结果到 Step Summary

### 部署前配置

服务器端需完成：

1. **项目目录**：`/opt/demonet`（或自定义路径）
2. **环境变量**：确保 `.env` 文件包含所有必需密钥
3. **SSH 密钥**：将部署公钥添加到服务器的 `~/.ssh/authorized_keys`

GitHub 仓库 Secrets 需配置：

| Secret | 说明 | 示例 |
|--------|------|------|
| `DEPLOY_SSH_KEY` | SSH 私钥 | `-----BEGIN OPENSSH PRIVATE KEY-----...` |
| `DEPLOY_HOST` | 服务器 IP 或域名 | `123.45.67.89` |
| `DEPLOY_PORT` | SSH 端口（可选，默认 22） | `22` |
| `DEPLOY_USER` | 登录用户名（可选，默认 deploy） | `deploy` |
| `DEPLOY_PATH` | 部署路径（可选，默认 /opt/demonet） | `/opt/demonet` |
| `DEPLOY_DOMAIN` | 健康检查域名（可选） | `demonet.example.com` |

## 4. 工作流依赖关系

```
push/PR → CI → (通过) → Docker Build → (完成) → 手动 Deploy
                ↓
            PR 检查失败禁止合并
```

## 5. Maven Profile 说明

测试使用 `-Pci` profile。需要在 `pom.xml` 或 `application-test.yml` 中配置：

```yaml
# backend/src/test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/demonet_test?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: testpass
  redis:
    host: localhost
    port: 6379
app:
  jwt:
    secret: test-secret-key-for-ci-at-least-256-bits-long
    expiration: 86400000
```

CI 工作流会自动生成此文件。

## 6. 故障排查

### 测试失败
1. 检查 Surefire 测试报告（Artifact 的 `surefire-reports/` 目录）
2. 本地重现：`cd backend && mvn clean test -Pci`

### Docker 构建失败
1. 检查 Dockerfile 语法
2. 确保依赖文件 (`pom.xml`, `package.json`) 无错误
3. 查看 Buildx 日志中的具体错误行

### 部署失败
1. 检查 SSH 连接：`ssh -i deploy_key deploy@host`
2. 检查服务器磁盘空间：`df -h`
3. 检查 Docker 守护进程：`systemctl status docker`
4. 检查容器日志：`docker compose -f docker-compose.ci.yml logs backend`
