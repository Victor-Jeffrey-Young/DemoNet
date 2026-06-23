# DemoNet 新电脑环境搭建指南

## 前置条件

| 工具 | 最低版本 | 安装方式 |
|------|----------|----------|
| **Docker** + Docker Compose | Docker 24+ | `brew install docker` (macOS) 或 [Docker Desktop](https://www.docker.com/) |
| **JDK** | 17 | `brew install openjdk@17` (macOS) 或 [Adoptium](https://adoptium.net/) |
| **Maven** | 3.9+ | `brew install maven` 或 [官网下载](https://maven.apache.org/) |
| **Node.js** | 20+ | `brew install node@20` 或 [nvm](https://github.com/nvm-sh/nvm) |

验证安装：
```bash
docker --version
java --version
mvn --version
node --version
npm --version
```

---

## 1. 克隆项目

```bash
git clone https://github.com/<你的用户名>/<仓库名>.git DemoNet_1.0
cd DemoNet_1.0
```

---

## 2. 配置敏感信息

### 2.1 恢复后端配置文件

项目提供了模板文件，复制并填写你自己的配置：

```bash
cp backend/src/main/resources/application-example.yml backend/src/main/resources/application.yml
```

编辑 `backend/src/main/resources/application.yml`，将以下 `changeme` 占位符替换为真实值：

| 配置项 | 说明 | 获取方式 |
|--------|------|----------|
| `app.jwt.secret` | JWT 签名密钥，至少 256 位随机字符串 | 用 `openssl rand -base64 64` 生成 |
| `spring.datasource.password` | MySQL root 密码 | 与 docker-compose 中保持一致即可 |
| `spring.rabbitmq.password` | RabbitMQ 密码 | 与 docker-compose 中保持一致即可 |
| `app.tmdb.api-key` | TMDB API Key（电影/电视剧抓取） | [TMDB Settings → API](https://www.themoviedb.org/settings/api) 免费注册获取 |
| `app.steam.api-key` | Steam API Key（可选，游戏抓取） | [Steam 开发者页面](https://steamcommunity.com/dev/apikey) |

> 💡 **不想每次改配置文件？** 也可以创建 `.env` 文件用环境变量注入（见下方 2.2）。

### 2.2 （可选）创建 .env 文件管理密码

在项目根目录创建 `.env` 文件（已被 `.gitignore` 忽略）：

```bash
# .env — 开发环境变量（不会被提交到 Git）
MYSQL_ROOT_PASSWORD=你选择的密码
RABBITMQ_PASS=你选择的密码
JWT_SECRET=$(openssl rand -base64 64)
TMDB_API_KEY=你的tmdb_api_key
STEAM_API_KEY=你的steam_api_key
```

然后使用 `docker compose --env-file .env up -d` 启动。

---

## 3. 启动开发基础设施

项目依赖 MySQL、Redis、RabbitMQ，使用 Docker Compose 一键启动：

```bash
docker compose up -d
```

这会启动：
- **MySQL 8.0** → `localhost:3306`，数据库 `demonet`
- **Redis 7** → `localhost:6379`
- **RabbitMQ 3.12**（含管理界面）→ `localhost:5672`，管理界面 `http://localhost:15672`

验证服务状态：
```bash
docker compose ps
# 三个服务应该都是 Up 状态
```

---

## 4. 初始化数据库

首次启动后，MySQL 容器会自动创建 `demonet` 数据库。然后执行建表脚本：

```bash
docker exec -i demonet-mysql mysql -uroot -p<你的密码> demonet < backend/src/main/resources/schema.sql
```

或者直接用 Docker 的初始化机制——`schema.sql` 已经在 `docker-compose.yml` 的 volumes 挂载中，容器首次启动时会自动执行（仅当数据目录为空时）。

---

## 5. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端启动在 `http://localhost:8080`。

验证：
```bash
curl http://localhost:8080/api/items
# 应该返回空数组 []（还没有数据）
```

---

## 6. 启动前端

另开一个终端：

```bash
cd frontend
npm install    # 首次运行需要安装依赖
npm run dev
```

前端启动在 `http://localhost:5173`，API 请求自动代理到后端 `:8080`。

---

## 7. 导入种子数据（可选）

项目包含少量示例数据用于开发测试：

```bash
docker exec -i demonet-mysql mysql -uroot -p<你的密码> demonet < backend/src/main/resources/data/seed.sql
```

导入后访问 `http://localhost:5173` 即可看到示例内容。

seed.sql 中包含的示例数据：
- 游戏：Hades II、黑神话：悟空
- 电影：奥本海默
- 动漫：胆大党
- 桌游：方舟动物园

---

## 8. 默认管理员账号

种子数据中包含默认管理员账号：

| 字段 | 值 |
|------|-----|
| 用户名 | `admin` |
| 密码 | `changeme` |

> ⚠️ **首次登录后请立即修改密码。** 生产环境部署前务必更换。

管理后台入口：`http://localhost:5173/admin`

---

## 9. 生产环境部署

使用 `docker-compose.prod.yml` 一键部署全栈：

```bash
# 1. 创建 .env 文件（生产环境务必使用强密码）
cat > .env << 'EOF'
MYSQL_ROOT_PASSWORD=<强密码>
RABBITMQ_PASS=<强密码>
JWT_SECRET=<随机 256 位字符串>
TMDB_API_KEY=<你的 TMDB API Key>
STEAM_API_KEY=<你的 Steam API Key>
EOF

# 2. 构建并启动所有服务
docker compose -f docker-compose.prod.yml up -d --build
```

生产架构：
```
                    ┌──────────────────┐
   :80 ──────────►  │  Nginx (frontend) │
                    │  静态 SPA + 反向代理 │
                    └────────┬─────────┘
                             │ /api/*  /uploads/*
                    ┌────────▼─────────┐
                    │  Spring Boot     │
                    │  (backend:8080)  │
                    └──┬──────┬──────┬─┘
                       │      │      │
              ┌────────▼┐ ┌──▼──┐ ┌─▼──────┐
              │ MariaDB │ │Redis│ │RabbitMQ│
              └─────────┘ └─────┘ └────────┘
```

---

## 10. TMDB API Key 获取步骤

1. 注册 [TMDB](https://www.themoviedb.org/signup) 账号
2. 进入 [API 设置页面](https://www.themoviedb.org/settings/api)
3. 选择 "Developer" 类型
4. 填写申请表单（用途写 "Personal project - content curation app" 即可）
5. 获取 API Key (v3 auth) 填入配置

> 免费申请，即时生效，无需审核。

---

## 常见问题

### Q: 端口被占用？
```bash
# 查看占用端口的进程
lsof -i :3306
lsof -i :6379
lsof -i :8080
lsof -i :5173
```

### Q: 后端启动报数据库连接失败？
确认 Docker 服务已启动且 MySQL 容器运行正常：
```bash
docker compose ps mysql
docker compose logs mysql
```

### Q: 前端 `npm install` 失败？
尝试清除缓存：
```bash
rm -rf node_modules package-lock.json
npm cache clean --force
npm install
```

### Q: 如何重置数据库？
```bash
# 删除并重建（数据会丢失）
docker compose down -v
docker compose up -d
```
