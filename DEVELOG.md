# DemoNet 1.0 开发文档

> **目标**：全栈多品类"体验前置"平台
> **最后更新**：2026-05-28
> **当前状态**：阶段 7 进行中 — 游戏频道 ✅ 电影频道 ✅ 数据管道(TMDB) ✅

---

## 1. 项目定位

策展化的"试玩派对"——聚合游戏 Demo、电影预告片、桌游教学、模型开箱等一切能让你"先试试"的内容。

核心价值：**每个品类拥有独立的视觉语言和交互体验**，用户进入不同频道就像进入不同的展厅。

---

## 2. 技术选型

| 层级 | 技术 | 版本 |
|------|------|------|
| 前端 | Vue 3 + Vite | Composition API |
| 状态管理 | Pinia | |
| 路由 | Vue Router 4 | `/item/:slug` |
| UI 库 | Element Plus | 后台 |
| CSS | Tailwind CSS 4 | Vite plugin |
| HTTP | Axios | JWT 拦截器 |
| 后端 | Spring Boot | 4.0.6, JDK 17 |
| ORM | MyBatis-Plus | 3.5.16 |
| SQL 解析 | mybatis-plus-jsqlparser | 3.5.16 |
| JWT | jjwt (api/impl/jackson) | 0.12.6 |
| 认证 | Spring Security | 无状态 JWT |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis | 7.x |
| 消息队列 | RabbitMQ | 3.12 |
| 电影数据 | TMDB API v3 | `api.themoviedb.org/3` |

---

## 3. 实际项目结构

```
DemoNet_1.0/
├── .gitignore / docker-compose.yml
├── readme.md / DEVELOG.md
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/example/demonet/
│       ├── config/    (SecurityConfig, MybatisPlusConfig, RabbitMQConfig)
│       ├── security/  (JwtTokenProvider, JwtAuthFilter)
│       ├── entity/    (Item, User, UserItem, Tag)
│       ├── mapper/    (Item, User, UserItem, Tag)
│       ├── service/   (Auth, DataImport, FetchConsumer, Item, Steam, TMDB, Tag, UserItem)
│       ├── controller/(Auth, Admin, Item, Tag, UserItem)
│       └── resources/ (application.yml, schema.sql)
└── frontend/src/
    ├── main.js / App.vue / style.css
    ├── router/index.js / stores/auth.js
    ├── constants/types.js / styles/transitions.css
    ├── api/   (request, item, auth, tag, admin)
    ├── components/
    │   ├── AppNavbar.vue / AppFooter.vue / AppCard.vue / SearchBar.vue
    │   └── category/
    │       ├── GameHero.vue ✅
    │       ├── GameCard.vue ✅
    │       ├── MovieHero.vue ✅
    │       └── MovieCard.vue ✅
    └── views/
        ├── Home.vue / List.vue / Detail.vue / Search.vue
        ├── Login.vue / Register.vue / Profile.vue
        └── Admin.vue
```

---

## 4. 数据模型

6 表：`items` / `users` / `user_items` / `tags` / `item_tag_mapping` / `reviews`

`info_json` 统一结构（按 type 有不同字段）：

```json
// 游戏
{ "developer":"", "genre":"", "platform":"", "demo_available":true, "demo_url":"steam://install/xxx",
  "videos":{ "youtube":"https://www.youtube.com/embed/...", "bilibili":"//player.bilibili.com/...?bvid=..." } }

// 电影
{ "director":"", "year":2023, "duration":"180min", "genre":"剧情, 科幻",
  "trailer":"https://www.youtube.com/embed/...",
  "videos":{ "youtube":"...", "bilibili":"" } }
```

---

## 5. 已完成阶段

### 阶段 0-5 ✅
骨架搭建、CRUD API、用户系统(JWT)、搜索与推荐(标签)、异步抓取管道(Steam/TMDB/RabbitMQ)、8 品类扩展。

---

## 6. 阶段 7：品类视觉体验升级 — 实际进度

### 7.0 前置准备 ✅

| 任务 | 状态 |
|------|------|
| ALTER TABLE 加 `media_url` + `external_link` 列 | ✅ |
| Entity Item.java 加新字段 | ✅ |
| `src/constants/types.js` (10 品类元数据) | ✅ |
| `src/styles/transitions.css` | ✅ |
| 默认用户 `admin/changeme` | ✅ |
| 种子数据补封面/外部链接 | ✅ |

### 7.1 🎮 游戏频道 ✅ (14 款，5 款有试玩)

| 组件 | 关键实现 |
|------|---------|
| GameHero | 三份拷贝无限循环轮播，`transform: translateX()` GPU 驱动，3.5s 自动播放，`radial-gradient` + 网格点阵背景，`aspect-[21/10]` 匹配 Steam 头图 |
| GameCard | 竖版 380px 卡片，hover 内容滑入视差(700ms cubic-bezier)，stagger 80ms/160ms，彩虹 DEMO 徽章 |
| 试玩业务 | `info_json.demo_url` → Steam `steam://install/APPID` / itch.io，卡片+详情页"🎮 免费试玩"按钮 |
| 视频嵌入 | 详情页卡片头部嵌入 iframe，Bilibili 优先→YouTube，右上角源切换 |
| Steam 数据 | Python 调用 Steam Store API 拉取 13/14 款真实封面(CDN)+商店描述+developer/genre/platform |

### 7.2 🎬 电影频道 ✅ (16 部，15 部有预告片)

| 组件 | 关键实现 |
|------|---------|
| MovieHero | `h-[calc(100vh-4rem)]` 全屏 Banner，900ms 渐入切换，5s 自动播放，深红遮罩 |
| MovieCard | 横向宽卡(1/3 TMDB 海报 + 2/3 信息)，hover 放大 |
| TMDB 数据管道 | `TMDBService.fetchMovieDetail(tmdbId)` 拉取：海报 `w500`、背景 `w1280`、中文简介、导演/年份/时长/类型、YouTube 预告片。通过 `append_to_response=videos,credits` 一次性获取完整信息 |
| 批量导入 | Python 脚本通过已知 TMDB 电影 ID 批量拉取，自动去重、清噪音 |

**电影清单**：奥本海默(2023)、沙丘2(2024)、你想活出怎样的人生(2023)、超级马力欧兄弟大电影(2023)、星际穿越(2014)、盗梦空间(2010)、蜘蛛侠：纵横宇宙(2023)、阿凡达：水之道(2022)、瞬息全宇宙(2022)、Top Gun: Maverick(2022)、蝙蝠侠：黑暗骑士(2008)、泰坦尼克号(1997)、肖申克的救赎(1994)、寄生虫(2019)、千与千寻(2001)、低俗小说(1994)

### 7.3-7.13 ⏳ 待实现

动漫/桌游/模型/书籍/音乐/数码/咖啡/线下频道、首页3D卡片、Navbar滚动效果。

---

## 7. 阶段 8：平台基建补齐（计划）

| # | 任务 | 说明 |
|---|------|------|
| 8.1 | 评论系统 | `ReviewController` + `ReviewSection.vue` |
| 8.2 | Redis 缓存 | `@Cacheable` 热门/推荐/搜索 |
| 8.3 | Swagger/SpringDoc | `springdoc-openapi` |
| 8.4 | 定时抓取 | `@Scheduled` 每日自动拉取 |
| 8.5 | Docker 部署 | Dockerfile×2 + nginx.conf |
| 8.6 | 管理员上传竖版图 | `poster_url` + `POST /api/admin/upload-poster/{id}` |

---

## 8. 问题日志

### 基础设施 (阶段 0-5)
1. **mvnw 缺失** → 直接用全局 `mvn`
2. **MyBatis-Plus 依赖** → `mybatis-plus-spring-boot4-starter:3.5.16`
3. **分页拦截器缺失** → 加 `mybatis-plus-jsqlparser:3.5.16`
4. **分页 total=0** → 同上
5. **JDBC utf8mb4** → `characterEncoding=UTF-8`
6. **中文双重编码** → Python `mysql-connector-python` 直连
7. **VSCode ClassNotFoundException** → `mvn clean`
8. **docker-compose version** → 移除 `version` 行
9. **Map.of 限制** → `Map.ofEntries()`
10. **收藏下拉消失** → JS `mouseenter/mouseleave` 替代 CSS `group-hover`
11. **Profile 无标题** → `UserItemService` JOIN items 返回 title/slug
12. **`seedTags()` 空异常** → `queryForList` 替代 `queryForObject`
13. **详情页 overflow-hidden** → 裁剪下拉菜单 → 移至图片区域

### 游戏频道 (7.1)
14. **轮播图卡顿** → `scrollTo` → `transform: translateX()` GPU 加速
15. **轮播克隆闪白** → 2 克隆 → 3 份全量拷贝，中间区操作
16. **Steam 图裁切** → `aspect-[16/10]` → `aspect-[21/10]`；`bg-center` → `bg-top`
17. **`@Value` 缺失 import** → SteamService 缺 `import org.springframework.beans.factory.annotation.Value`
18. **竖版卡片缺图** → Steam 无竖版图，暂用横版。长期方案见 8.6

### 用户/导航 (7.13)
19. **默认用户丢失** → `seedUser()` 移到 `items` 判断之前
20. **Navbar 用户消失** → App.vue `onMounted` 调用 `auth.fetchUser()`
21. **List 头部重复** → `meta.desc` → `共 X 条内容`

### 视频 & 数据 (7.12)
22. **视频过早播放** → iframe 移到卡片头部内替代 emoji
23. **`async function setStatus` 被误删** → 编辑误删函数声明 → 编译错误
24. **TMDB 网络不通** → 终端直连 `api.themoviedb.org` 超时。偶尔恢复，需要重试
25. **`language=zh-CN` 使 videos 为空** → TMDB 中文参数会屏蔽 YouTube 预告片！必须不带 language 参数单独请求 `append_to_response=videos`，然后用 `site=YouTube && type IN (Trailer,Teaser)` 筛选
26. **TMDB 搜索噪声** → `search/movie?query=Oppenheimer` 返回 20+ 个纪录片/变体。解决：先搜索获取 ID，再用 `fetchMovieDetail(tmdbId)` 获取精确数据；导入后用 `GROUP BY title HAVING COUNT>1` 清理重复
27. **电影 FK 约束** → 删除旧电影时 `user_items` 外键阻止 → 先删 `user_items` 再删 `items`
28. **AppCard 封面图遗失** → 重写类型映射时漏掉 `v-if="item.coverUrl"` 条件渲染，始终显示 emoji → 恢复封面图 `bg-cover` + emoji fallback

---

## 9. 当前状态

| 项 | 值 |
|----|-----|
| 完成 | 阶段 0-5 + 7.0 + 7.1(游戏14款) + 7.2(电影16款) |
| 游戏数据 | 14 款，13 YT+B站双视频源，5 试玩，全 Steam 真实封面 |
| 电影数据 | 16 款，15 有 YouTube 预告片，全 TMDB 真实海报+背景 |
| 前端组件 | GameHero/Card, MovieHero/Card, AppCard/Navbar/Footer/SearchBar |
| 后端 | 25 Java 文件 |
| 前端 | 25 Vue/JS 文件 |

---

## 10. 全部 API

### 公开
`GET /api/items`, `GET /api/items/{slug}`, `GET /api/items/hot`, `GET /api/items/types`, `GET /api/items/recommended`, `GET /api/tags`, `GET /api/tags/items/{id}`, `POST /api/auth/register`, `POST /api/auth/login`

### Bearer
`GET /api/auth/me`, `POST /api/user/items`, `GET /api/user/items`, `DELETE /api/user/items/{id}`, `POST /api/admin/fetch/steam`, `POST /api/admin/fetch/tmdb`, `GET /api/admin/pending`, `PUT /api/admin/approve/{id}`, `PUT /api/admin/reject/{id}`

### 其他
`POST/PUT/DELETE /api/items/{id}`, `POST/DELETE /api/tags/{id}`, `POST /api/tags/items/{id}`

### 待实现
`GET/POST /api/reviews`, `POST /api/admin/upload-poster/{id}`
