# API 清单

> 全部端点，与后端 Controller 实际代码一致。

---

## 公开（无需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/items` | 分页列表，支持 `type` / `tag` / `keyword` 筛选 |
| GET | `/api/items/{slug}` | 按 slug 获取详情 |
| GET | `/api/items/hot` | 热门列表 |
| GET | `/api/items/types` | 品类统计 |
| GET | `/api/items/recommended` | 推荐列表 |
| GET | `/api/items/featured` | 精选列表（轮播用），支持 `type` 参数 |
| GET | `/api/tags` | 全部标签 |
| GET | `/api/tags/items/{id}` | 某 item 的标签 |
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录，返回 JWT token |

---

## 需认证（Bearer Token）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/auth/me` | 获取当前用户信息 |
| POST | `/api/user/items` | 收藏 item |
| GET | `/api/user/items` | 我的收藏列表 |
| GET | `/api/user/items/{itemId}` | 检查是否已收藏 |
| DELETE | `/api/user/items/{id}` | 取消收藏 |
| POST | `/api/reviews` | 提交评论 |
| GET | `/api/reviews/item/{itemId}` | 获取某 item 的评论（分页） |
| DELETE | `/api/reviews/{id}` | 删除自己的评论 |

---

## Admin（需 ADMIN 角色）

### Item CRUD

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/items` | 全部内容列表（含 status 筛选） |
| GET | `/api/admin/items/{id}` | 获取单条内容（含标签） |
| POST | `/api/admin/items` | 创建内容 |
| PUT | `/api/admin/items/{id}` | 更新内容 |
| DELETE | `/api/admin/items/{id}` | 删除内容 |
| PUT | `/api/admin/items/{id}/status` | 更新状态（body: `{ "status": 1 }`） |
| PUT | `/api/admin/items/{id}/featured` | 切换轮播入选状态 |

### 审核

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/pending` | 待审核列表（status=0） |
| PUT | `/api/admin/approve/{id}` | 审核通过（status→1） |
| PUT | `/api/admin/reject/{id}` | 审核拒绝（删除） |

### 轮播管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/carousel/{type}` | 获取当前轮播序列 |
| POST | `/api/admin/carousel/{type}` | 保存轮播序列（body: `{ "itemIds": [...] }`） |
| DELETE | `/api/admin/carousel/{type}/{itemId}` | 从轮播移除 |

### 图片上传

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/admin/upload/{id}` | 上传图片（multipart/form-data, `file` + `type` 参数） |

### 标签管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/tags` | 全部标签列表 |
| GET | `/api/admin/tags/paged` | 分页标签列表（支持 keyword 搜索） |
| POST | `/api/admin/tags` | 创建标签 |
| DELETE | `/api/admin/tags/{id}` | 删除标签 |
| POST | `/api/admin/items/{id}/tags` | 关联标签（body: `[tagId1, tagId2, ...]`） |
| DELETE | `/api/admin/items/{id}/tags/{tagId}` | 移除标签关联 |

### 数据抓取

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/admin/fetch/steam` | 触发 Steam 抓取（body: `{ "appIds": [...], "targetType": "game" }`） |
| POST | `/api/admin/fetch/tmdb` | 触发 TMDB 电影抓取（body: `{ "query": "...", "targetType": "movie" }`） |
| POST | `/api/admin/fetch/tmdb-tv` | 触发 TMDB 剧集抓取 |
| POST | `/api/admin/fetch/anilist` | 触发 AniList 动漫抓取 |
| POST | `/api/admin/fetch/bangumi` | 触发 Bangumi 中文动漫抓取 |
| POST | `/api/admin/fetch/itunes` | 触发 iTunes 音乐抓取 |
| POST | `/api/admin/fetch/igdb` | 触发 IGDB 游戏抓取（支持 search/popular/recent） |

### 统计

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/stats` | 管理后台统计数据 |

---

## 认证说明

- 公开接口无需任何认证头
- Bearer Token 接口需携带 `Authorization: Bearer <jwt_token>`
- Admin 接口除 JWT 外，还需用户的 `role = ADMIN`
- JWT 无状态，不存储服务端 Session
