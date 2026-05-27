# DemoNet 1.0 开发文档

> **目标**：全栈多品类"体验前置"平台
> **最后更新**：2026-05-28
> **当前状态**：阶段 5 完成（8 品类 / 50 条目 / 类型化详情模板）

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

---

## 3. 项目结构

```
DemoNet_1.0/
├── .gitignore / docker-compose.yml
├── readme.md / DEVELOG.md
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/example/demonet/
│       ├── config/    (Security, MybatisPlus, RabbitMQ)
│       ├── security/  (JwtTokenProvider, JwtAuthFilter)
│       ├── entity/    (Item, User, UserItem, Tag)
│       ├── mapper/    (Item, User, UserItem, Tag)
│       ├── service/   (Auth, DataImport, FetchConsumer, Item, Steam, TMDB, Tag, UserItem)
│       ├── controller/(Auth, Admin, Item, Tag, UserItem)
│       └── resources/
│           ├── application.yml
│           └── schema.sql
└── frontend/src/
    ├── main.js / App.vue / style.css
    ├── router/index.js
    ├── stores/auth.js
    ├── api/   (request, item, auth, tag, admin, review)
    ├── components/
    │   ├── layout/     (AppNavbar, AppFooter)
    │   ├── shared/     (SearchBar, ReviewSection, MediaEmbed)
    │   └── category/   (GameHero, MovieHero, AnimeHero, BoardgameHero,
    │                    ModelHero, BookHero, MusicHero, DigitalHero,
    │                    GameCard, MovieCard, AnimeCard, BoardgameCard,
    │                    ModelCard, BookCard, MusicCard, DigitalCard)
    └── views/
        ├── Home.vue
        ├── List.vue           (统一路由，按 type 加载不同卡片组件)
        ├── Detail.vue          (统一路由，按 type 渲染不同模板)
        ├── Login.vue / Register.vue / Profile.vue
        └── Admin.vue
```

---

## 4. 数据模型

现有 6 表：`items` / `users` / `user_items` / `tags` / `item_tag_mapping` / `reviews`

---

## 5. 已完成阶段

### 阶段 0：项目骨架 ✅
POM 修复、MyBatis-Plus/JSqlParser 依赖、建表、前端脚手架、通路闭环。

### 阶段 1：核心内容浏览 ✅
CRUD API、DataImportService (20 条)、Home/List/Detail 页面、AppCard/Navbar 组件。

### 阶段 2：用户系统 ✅
JWT 注册/登录、收藏标记、Pinia store、路由守卫、Login/Register/Profile 页面。

### 阶段 3：搜索与推荐 ✅
title+description 增强搜索、42 标签系统、多标签筛选、按收藏量推荐。

### 阶段 4：内容自动化管道 ✅
RabbitMQ 队列、Steam/TMDB 抓取、FetchConsumer 异步消费、Admin 审核上线。

### 阶段 5：多业务线扩展 ✅
8 品类 50 条目、info_json 品类字段、Detail.vue 信息卡网格、全组件类型扩展。

---

## 6. 阶段 7：品类视觉体验升级（核心改造）

> **目标**：每个品类拥有独立的视觉语言——不同的 Hero 动画、卡片布局、悬停效果、色彩体系。
> **原则**：不照搬旧版，基于 Tailwind + Vue 3 重新设计，保留"进入不同品类像进入不同展厅"的体验哲学。

### 前置准备：种子数据增强

| # | 任务 | 说明 |
|---|------|------|
| 7.0.1 | `items` 表新增 `media_url` 字段 | VARCHAR(512)，存视频/音频/iframe 嵌入链接 |
| 7.0.2 | `items` 表新增 `external_link` 字段 | VARCHAR(512)，存 Steam 商店/Tabletopia/试读等外部链接 |
| 7.0.3 | 迁移脚本 + DataImportService 更新 | ALTER TABLE + 种子数据补全链接 |
| 7.0.4 | 种子数据补充 `cover_url` | 为 50 条数据添加真实封面图 URL |
| 7.0.5 | 新增品类 seed：咖啡/茶 (3条) + 线下体验 (3条) | 补全 readme 全部 10 条业务线 |

---

### 7.1 🎮 游戏频道 — 动态画廊

**设计理念**：游戏展的感觉——大幅头图轮播、卡片 hover 时内容从底部浮出、深色渐变遮罩营造沉浸感。

| 组件 | 文件 | 设计规格 |
|------|------|---------|
| Hero | `GameHero.vue` | 横向滚动卡片画廊 (6 个卡片并排，自动滚动/拖拽)，左侧 1/4 宽度显示分类标题 + "热门推荐" 竖排文字，右侧 3/4 为 card strip。卡片 280×180px，hover 时 scale 1.05。dot 指示器在底部 |
| Card | `GameCard.vue` | 16:9 背景封面图 `bg-cover`。默认只显示标题（底部 4.5rem 渐变条），hover 时：封面图上移 4%（parallax）、渐变遮罩从底部展开至 60% 高度、描述文字 + "查看详情" 按钮从下方滑入、staggering 延迟 (title 0ms / desc 100ms / btn 200ms) |
| 色彩 | — | `from-emerald-950 to-gray-950` 底色，`emerald-500` 强调色，卡片渐变 `from-transparent via-gray-950/70 to-gray-950` |
| 列表页 | `List.vue` → type=game 分支 | 4 列 CSS Grid，列间距 1rem。顶部固定分类标题 + 标签筛选条。Hero 区域仅在无 keyword 搜索时展示 |

**动画细节：**
- Hero 画廊：`scroll-snap-type: x mandatory` + `overflow-x: auto`，平滑 snap
- Card hover：`transition: all 700ms cubic-bezier(0.19,1,0.22,1)`
- 内容层 stagger：`transition-delay: 0ms / 100ms / 200ms`

---

### 7.2 🎬 电影频道 — 沉浸式宽幅

**设计理念**：影院帷幕感——整页宽幅大图、前后景分层、标题占据视觉重心。

| 组件 | 文件 | 设计规格 |
|------|------|---------|
| Hero | `MovieHero.vue` | 全宽 100vw、高度 70vh 的大图 Banner。背景图为当前选中电影的 `wide_cover_url`（图片偏左，右侧留白给文字）。文字叠加层：顶部大字标题（5rem, font-black, text-shadow-lg）、副标题（英文名, 2rem, font-light）、底部一行标签（导演/年份/时长）。左下角 3 个 dot 指示器，右下角 "下一部 →" 箭头按钮。切换动画：背景 scale 1→1.05 缓入，文字 translateY 30px→0 fade-in |
| Card | `MovieCard.vue` | 横向宽卡（aspect-[21/9]），左侧为海报图（1/3 宽），右侧为信息面板（2/3 宽，深色半透背景）。hover 时海报图 scale 1.05 + 亮度提升，右侧信息面板中"观看预告"按钮从透明→不透明滑入 |
| 色彩 | — | `from-neutral-950 to-stone-900` 底色，`red-500` 强调色，卡片 `backdrop-blur bg-neutral-900/80` |
| 列表页 | `List.vue` → type=movie 分支 | 2 列宽卡布局。顶部 Hero 固定为全宽 Banner。滚动时 Navbar 加 backdrop-blur |

**动画细节：**
- Hero 切换：`transition: transform 900ms ease, opacity 600ms`
- 背景图层：`filter: blur(0)` → active 时 `blur(3px)` 创造景深感
- Card hover：`transition: 500ms cubic-bezier(0.4,0,0.2,1)`

---

### 7.3 🎭 动漫频道 — 角色卡片

**设计理念**：角色档案册——大头像 + 悬浮文字、hover 褪色处理，像翻开一页角色设定集。

| 组件 | 文件 | 设计规格 |
|------|------|---------|
| Hero | `AnimeHero.vue` | 大 Banner 静态图 (100vw, 40vh)，顶部叠加分类标题（白色大字 + 日文假名副标题）。下方标签行：国漫/日漫/美漫 切换 filter chips |
| Card | `AnimeCard.vue` | 圆形头像式卡片：`rounded-2xl overflow-hidden`。上半部分为大图（`aspect-[3/4]`），下半部为标题（`text-lg font-semibold`）+ tags 行。hover 效果：`filter: grayscale(1)` 缓慢褪色（`transition 400ms`），同时标题上浮 8px |
| 色彩 | — | `from-violet-950 to-fuchsia-950` 底色，`violet-400` 强调色，卡片背景 `bg-white/5 backdrop-blur` |
| 列表页 | `List.vue` → type=anime 分支 | 4 列 Grid。Hero 仅在顶部展示，滚动后缩为标签条。卡片 hover 灰度效果 |

**动画细节：**
- Card hover：`filter: grayscale(1); transition: filter 400ms ease`
- Title float：`transform: translateY(0)` → `translateY(-8px); transition: 300ms`

---

### 7.4 🎲 桌游频道 — 信息面板

**设计理念**：桌游店的陈列架——重点展示参数（人数/时长/重度），卡片悬浮时参数放大。

| 组件 | 文件 | 设计规格 |
|------|------|---------|
| Hero | `BoardgameHero.vue` | 横幅 (100vw, 40vh) 展示 BGG 排名前 3 的桌游封面拼图。左侧分类标题，右侧半透明面板列出"BGG TOP 3"实时排行 |
| Card | `BoardgameCard.vue` | 竖版矩形卡（`aspect-[3/4]`）。上半部分封面图，下半部分为信息区——突出显示三行大号数字/文字：👥 人数、⏱ 时长、⚖️ 重度。hover 时：封面图微放大、信息区底色加深、数字变大 (scale 1.1) |
| 色彩 | — | `from-amber-950 to-yellow-950` 底色，`amber-400` 强调色，卡片 `bg-amber-900/40 border-amber-800/50` |
| 列表页 | `List.vue` → type=boardgame 分支 | 3 列 Grid。顶部 Hero 固定。每卡强调数字参数。排序选项：按 BGG 排名 / 按重度 / 按时长 |

**动画细节：**
- Stat hover：`transform: scale(1.1); transition: 300ms cubic-bezier(0.34,1.56,0.64,1)` (弹性放大)

---

### 7.5 🧩 模型频道 — 画廊展示

**设计理念**：展柜中的模型——精细大图、多角度展示、hover 放大镜效果。

| 组件 | 文件 | 设计规格 |
|------|------|---------|
| Hero | `ModelHero.vue` | 横向 scroll-snap 图片条 (100vw, 50vh)，展示多张模型图片 (cover + info_json 中的 extra_images)。左侧"级别筛选"(PG/MG/RG/HG/FM) 竖排多选标签 |
| Card | `ModelCard.vue` | 方形卡片 (`aspect-square`)。全图覆盖，底部叠加半透明信息条（等级/比例/系列）。hover 效果：图片 scale 1.1（模拟凑近观察），信息条上移露出更多图像，鼠标跟随放大镜效果 (CSS `clip-path` on `mousemove`) |
| 色彩 | — | `from-slate-950 to-blue-950` 底色，`sky-400` 强调色，卡片 `border-slate-700` |
| 列表页 | `List.vue` → type=model 分支 | 4 列 Grid。左侧级别多选 filter。卡 hover 放大镜 |

**动画细节：**
- 放大镜：通过 `mousemove` 事件设置 `background-position` 偏移，`background-size: 200%`，圆形 `clip-path`
- Hero 条：`scroll-snap-type: x mandatory`

---

### 7.6 📖 书籍频道 — 书架陈列

**设计理念**：实体书店的"本月推荐"展台——竖版书封、两列交错布局、hover 时书封微倾。

| 组件 | 文件 | 设计规格 |
|------|------|---------|
| Hero | `BookHero.vue` | 横版 (100vw, 35vh) — 浅色半透背景上的大字标题 + 左对齐副标题 "翻开第一页，遇见新世界"。右侧展示当前推荐书的封面大图（带阴影和 3D rotate -2deg） |
| Card | `BookCard.vue` | 竖版书封卡片 (`aspect-[2/3]`)。纯色背景（按分类不同：科幻-深蓝/历史-棕/漫画-橙）上叠加书名+作者。hover 时：卡片 `rotateY(-5deg) rotateX(2deg)`（模拟从书架取书），右侧出现阴影（perspective 效果） |
| 色彩 | — | 米白/暖灰浅色系 `from-stone-100 to-amber-50`，文字深色 `text-stone-900`，避免暗色主题疲劳。`amber-700` 强调色 |
| 列表页 | `List.vue` → type=book 分支 | 2-3 列交错 Grid（偶数行右偏移 2rem），模拟书架错落。Hero 最小化。排序：按出版年份 |

**动画细节：**
- Card hover：`transform: perspective(800px) rotateY(-5deg) rotateX(2deg); transition: 500ms`
- Shadow：hover 时 `box-shadow` 从左侧出现加深

---

### 7.7 🎵 音乐频道 — 专辑封面墙

**设计理念**：唱片店的专辑墙——正方形封面排列、内置音频播放器、hover 旋转。

| 组件 | 文件 | 设计规格 |
|------|------|---------|
| Hero | `MusicHero.vue` | 居中大标题 + 当前播放曲目信息条。整页专辑封面墙（5×N Grid, 正方形卡片）作为背景，`opacity-15` 模糊 |
| Card | `MusicCard.vue` | 正方形专辑封面卡 (`aspect-square`)。中部圆形播放按钮（hover 时出现，`scale(0)` → `scale(1)` with elastic）。封面图 hover 时 `rotate(3deg)` + 微放大。卡片下方显示曲名/艺人/年份 |
| 色彩 | — | 深紫渐变 `from-fuchsia-950 to-pink-950`，`pink-400` 强调色，卡片 `bg-fuchsia-900/50` |
| 列表页 | `List.vue` → type=music 分支 | 5 列 Grid，封面墙布局。无 Hero 时顶部标题居中。排序：按年份/艺人 |

**动画细节：**
- Play button：`transition: 400ms cubic-bezier(0.34,1.56,0.64,1)` (弹性)
- Cover rotate：`transform: rotate(3deg); transition: 500ms`

---

### 7.8 📱 数码频道 — 产品聚焦

**设计理念**：Apple 产品页风格——极简、留白、大图居中、参数列表清晰。

| 组件 | 文件 | 设计规格 |
|------|------|---------|
| Hero | `DigitalHero.vue` | 居中布局、大标题 + 一句话卖点（`text-5xl font-bold tracking-tight`）。下方 3 张产品图并排（`aspect-[4/3]`，圆角），hover 时切换图片展示不同角度/颜色 |
| Card | `DigitalCard.vue` | 横向产品卡：左侧占 1/3 的产品图（白底或透明 png），右侧 2/3 为品牌/类别/年/特性标签列表。hover 时图片 scale 1.03，标签逐个 highlight |
| 色彩 | — | 浅色极简风 `bg-white text-gray-900`（全站唯一亮色频道）。`cyan-600` 强调色。避免暗色调，突出产品质感 |
| 列表页 | `List.vue` → type=digital 分支 | 2 列横向卡布局。左侧品牌 filter（Sony/Apple/Cherry 等）。Hero 最小化 |

**动画细节：**
- 产品图切换：`opacity` fade 300ms
- Tag highlight：`transition: background 200ms, color 200ms`

---

### 7.9 ☕ 咖啡/茶频道 — 风味轮盘（新增品类）

**设计理念**：精品咖啡店的手冲吧台——暖色调、圆形容器、风味描述卡片。

| 组件 | 文件 | 设计规格 |
|------|------|---------|
| Hero | `CoffeeHero.vue` | 全宽暖色 Banner（烘焙豆/茶叶纹理背景），中央圆形"今日推荐"风味轮盘（SVG 雷达图，6 个维度：酸度/苦度/甜度/醇厚度/花果香/回甘）。右侧竖排标题 |
| Card | `CoffeeCard.vue` | 圆形卡片（`rounded-full aspect-square`）包裹咖啡豆/茶叶图像。hover 时旋转 15deg + scale 1.05，下方出现风味标签。卡片底部显示产地/烘焙度/冲泡方式 |
| 色彩 | — | `from-amber-900 to-orange-950` 暖色调，`orange-300` 强调色，卡片 `bg-amber-800/40` |
| 列表页 | `List.vue` → type=coffee 分支 | 3 列圆形卡片 Grid。顶部风味轮 filter（酸/甜/苦 三轴滑块？或标签 chips） |

---

### 7.10 🏛️ 线下体验频道 — 活动海报（新增品类）

**设计理念**：音乐节/展览海报墙——大幅海报图、日期地点突出、即将上线倒计时。

| 组件 | 文件 | 设计规格 |
|------|------|---------|
| Hero | `OfflineHero.vue` | 3 张即将开始的活动海报并排（每张 1/3 宽），每张叠加倒计时（DD:HH:MM）和"立即预约"按钮。背景为全宽模糊活动图 |
| Card | `OfflineCard.vue` | 海报式卡片 (`aspect-[2/3]`)。大图覆盖、上方标签（类型：密室/展览/工坊）、中部半透明日期条（月/日 大号）、下方地点。hover 时海报亮度提升 + "了解详情"浮动按钮出现 |
| 色彩 | — | `from-indigo-950 to-slate-950`，`indigo-400` 强调色，卡片 `border-indigo-800/40` |
| 列表页 | `List.vue` → type=offline 分支 | 3 列海报 Grid。排序：按活动日期升序（即将开始在前）。类型 filter chips（密室/展览/工坊/音乐节） |

---

### 7.11 首页 — 3D 翻转卡片 + 品类入口

**设计理念**：展览馆入口大厅——每个品类一个入口展台，精选内容用 3D 卡片展示。

| 组件 | 文件 | 设计规格 |
|------|------|---------|
| 品类入口 | `Home.vue` 改造 | 保留 10 个品类入口卡片，改为更大尺寸（2 行 5 列 → 交错排列），每个卡片内部嵌入该品类的代表性 emoji/icon 动画（hover 时 icon 弹跳）。下方显示该品类最新 1 条内容的标题预览 |
| 3D 卡片 | `FeaturedCard.vue` | CSS 3D Transform 翻转卡：正面为内容封面图 + 标题，背面为信息面板（类型标签 + 描述 + "去看看"按钮）。hover 时 `rotateY(-90deg)` 翻转。`perspective: 1200px`、`transform-style: preserve-3d`。每行 4 张 |
| Banner | `Home.vue` 改造 | 全宽 (100vw, 60vh) 渐变背景（`from-gray-950 via-indigo-950 to-gray-950`），中央竖排大字标题 "试玩派对" + 搜索框 + 滚动向下提示。下方滚动时 banner 缩小为 sticky header |

---

### 7.12 详情页 — 体验内容嵌入

| 组件 | 文件 | 设计规格 |
|------|------|---------|
| MediaEmbed | `MediaEmbed.vue` | 判断 `item.media_url` 类型（youtube.com → `<iframe>` / .mp3 → `<audio>` / .mp4 → `<video>` / 其他 → `<iframe sandbox>`），自动渲染正确的播放器。宽高比 `aspect-video` |
| ExternalLink | Detail 内联 | 如果 `item.external_link` 不为空，在标题下方显示醒目的"🎮 去体验" / "🎬 观看预告" / "🎲 在线试玩" 按钮（按 type 改变文案），`target="_blank"` 新窗口打开 |
| ReviewSection | `ReviewSection.vue` | 评论组件：评分星星 + 评论列表 + 发表评论表单（需登录）。调用 `/api/reviews` CRUD |
| 信息卡 | `Detail.vue` 改造 | 保留 7.0 的 4 列 infoFields 网格，但按 type 调整字段顺序和样式，使每个品类的重点信息更突出 |

---

### 7.13 通用增强

| 任务 | 说明 |
|------|------|
| Navbar 滚动效果 | 透明 → 深色过渡（`scrollY > 50` 时 `bg-gray-950/90 backdrop-blur`），首页全透明 |
| 品类色彩常量 | `src/constants/types.js` — 统一管理 10 个品类的 emoji/label/color/heroColor/cardColor，所有组件引用此常量 |
| CSS 动画文件 | `src/styles/transitions.css` — 集中定义 `cubic-bezier` 曲线、stagger 延迟、keyframes，供各组件 `@apply` 使用 |
| 字体 | 通过 Tailwind 配置引入 Google Fonts：游戏(Inter+Teko)、电影(Playfair Display)、动漫(Noto Sans SC)、桌游(Rubik)、模型(Orbitron)、书籍(Libre Baskerville)、音乐(JetBrains Mono)、数码(SF Pro Display via system) |

---

## 7. 阶段 8：平台基建补齐

| # | 任务 | 说明 |
|---|------|------|
| 8.1 | 评论系统 | `ReviewController` (GET/POST /api/reviews) + `ReviewSection.vue`。表已建 |
| 8.2 | Redis 缓存 | `@Cacheable` 热门列表、首页推荐、搜索结果。TTL 10min |
| 8.3 | Swagger/SpringDoc | 添加 `springdoc-openapi-starter-webmvc-ui` 依赖 + 配置类 |
| 8.4 | 定时抓取调度 | `@Scheduled` + `@EnableScheduling`：每日凌晨 2 点自动抓取 Steam 热门新游、TMDB 新片 |
| 8.5 | Docker 部署 | `backend/Dockerfile` + `frontend/Dockerfile` + `nginx.conf` + 整合 `docker-compose.yml` |

---

## 8. 执行顺序

```
7.0 前置准备 (schema 扩展 + 数据补全)
  ↓
7.1-7.10 按品类逐个实施 (每个品类约 2-3h)
  优先：🎮游戏 → 🎬电影 → 🎭动漫 (核心三品类先出效果)
  其次：🎲桌游 → 🧩模型 → 📖书籍
  最后：🎵音乐 → 📱数码 → ☕咖啡 → 🏛️线下
  ↓
7.11 首页改造 (3D 卡片 + 品类入口)
  ↓
7.12 详情页嵌入体验内容
  ↓
7.13 通用增强 (字体/动画常量/Navbar 效果)
  ↓
阶段 8 (评论/Redis/Swagger/Docker)
```

---

## 9. 问题日志

1. **mvnw 缺失** → 直接用全局 `mvn`
2. **MyBatis-Plus 依赖** → 用 `mybatis-plus-spring-boot4-starter:3.5.16`
3. **分页拦截器缺失** → 加 `mybatis-plus-jsqlparser:3.5.16`
4. **分页 total=0** → 同上
5. **JDBC utf8mb4** → `characterEncoding=UTF-8`
6. **中文双重编码** → Python `mysql-connector-python` 直连
7. **VSCode ClassNotFoundException** → `mvn clean`
8. **docker-compose version** → 移除 `version` 行
9. **Map.of 限制** → `Map.ofEntries()`
10. **收藏下拉消失** → JS `mouseenter/mouseleave` 替代 CSS `group-hover`
11. **Profile 无标题** → `UserItemService` JOIN items 返回 title/slug
12. **seedTags() 空异常** → `queryForList` 替代 `queryForObject`
13. **详情页 overflow-hidden** → 裁剪下拉菜单 → 移至图片区域

---

## 10. 当前状态

| 项 | 值 |
|----|-----|
| 完成阶段 | 0-5 |
| 种子数据 | 50 items / 86 tags |
| 品类 | 8 (待扩展至 10) |
| 后端 | 25 Java 文件 |
| 前端 | 18 Vue/JS 文件 |

---

## 11. 全部 API

### 公开

`GET /api/items`, `GET /api/items/{slug}`, `GET /api/items/hot`, `GET /api/items/types`, `GET /api/items/recommended`, `GET /api/tags`, `GET /api/tags/items/{id}`

### 认证

`POST /api/auth/register`, `POST /api/auth/login`

### Bearer

`GET /api/auth/me`, `POST /api/user/items`, `GET /api/user/items`, `DELETE /api/user/items/{id}`, `POST /api/admin/fetch/steam`, `POST /api/admin/fetch/tmdb`, `GET /api/admin/pending`, `PUT /api/admin/approve/{id}`, `PUT /api/admin/reject/{id}`

### 其他

`POST /api/items`, `PUT /api/items/{id}`, `DELETE /api/items/{id}`, `POST /api/tags`, `DELETE /api/tags/{id}`, `POST /api/tags/items/{id}`

### 待实现 (阶段 8)

`GET /api/reviews?itemId=`, `POST /api/reviews`, `DELETE /api/reviews/{id}`
