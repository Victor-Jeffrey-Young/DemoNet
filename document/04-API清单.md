# API 清单

> 全部端点，与后端 Controller 实际代码一致（截至 2026-07-04）。

---

## 公开（无需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/items` | 分页列表，支持 `type` / `tag` / `keyword` 筛选 |
| GET | `/api/items/{slug}` | 按 slug 获取详情 |
| GET | `/api/items/hot` | 热门列表（支持 `type`/`limit` 参数；热度公式加权排序） |
| GET | `/api/items/types` | 品类统计 |
| GET | `/api/items/recommended` | 推荐列表 |
| GET | `/api/items/featured` | 精选列表（轮播用），支持 `type` 参数 |
| GET | `/api/tags` | 全部标签 |
| GET | `/api/tags/items/{id}` | 某 item 的标签 |
| POST | `/api/auth/register` | 注册（含 IP 限流 / Turnstile / 邀请码校验） |
| POST | `/api/auth/login` | 登录，返回 JWT token |
| GET | `/api/auth/config` | 公开配置（Turnstile site key、邀请码开关状态） |
| GET | `/api/categories/visible` | 前台可见品类列表（按 sort_order 排序，仅 visible=1） |

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
| GET | `/api/admin/items` | 全部内容列表（支持 `type`/`keyword`/`status` 筛选，分页） |
| GET | `/api/admin/items/{id}` | 获取单条内容（含标签） |
| POST | `/api/admin/items` | 创建内容 |
| PUT | `/api/admin/items/{id}` | 更新内容 |
| DELETE | `/api/admin/items/{id}` | 删除内容 |
| PUT | `/api/admin/items/{id}/status` | 更新状态（body: `{ "status": 1 }`） |
| PUT | `/api/admin/items/{id}/featured` | 切换轮播入选状态 |
| POST | `/api/admin/items/batch-delete` | 批量删除（body: `{ "ids": [1,2,3] }`） |
| POST | `/api/admin/items/batch-status` | 批量上线/下架（body: `{ "ids": [1,2], "status": 1 }`） |

### 审核

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/pending` | 待审核列表（status=0，分页） |
| PUT | `/api/admin/approve/{id}` | 审核通过（status→1） |
| PUT | `/api/admin/reject/{id}` | 审核拒绝（删除） |
| PUT | `/api/admin/reject/batch` | 批量拒绝（body: `[id1, id2, ...]`） |

### 轮播管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/carousel/{type}` | 获取当前轮播序列 |
| POST | `/api/admin/carousel/{type}` | 保存轮播序列（body: `{ "itemIds": [...] }`） |
| DELETE | `/api/admin/carousel/{type}/{itemId}` | 从轮播移除 |

### 图片上传

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/admin/upload/{id}` | 上传图片（multipart/form-data，`file` + `type` 参数；type: cover/wide_cover/poster/reader） |
| POST | `/api/admin/items/{id}/fetch-sgdb-poster` | 从 SteamGridDB 拉取竖版封面（需 Steam AppID） |
| POST | `/api/admin/backfill/poster-urls` | 批量回填游戏竖版封面（管理员维护用） |

### 标签管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/tags` | 全部标签列表 |
| GET | `/api/admin/tags/paged` | 分页标签列表（支持 keyword 搜索） |
| POST | `/api/admin/tags` | 创建标签（body: `{ "name": "..." }`） |
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
| GET | `/api/admin/steam/search` | Steam 商店搜索（`?q=关键词`，自动过滤 DLC） |

### 品类管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/categories/settings` | 获取全部品类设置（含隐藏品类） |
| PUT | `/api/admin/categories/settings` | 批量更新品类可见性与排序（body: `[{type, visible, sort_order}, ...]`） |

### 用户管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/users` | 用户列表（含 id/username/email/role/enabled/created_at） |
| PUT | `/api/admin/users/{id}/ban` | 封禁/解封用户（body: `{ "enabled": 0 }`；禁止封自己和管理员） |
| PUT | `/api/admin/users/{id}/reset-password` | 重置密码（返回新随机密码） |

### 邀请码管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/invite-codes` | 邀请码列表（含使用者名称） |
| POST | `/api/admin/invite-codes/generate` | 生成邀请码（body: `{ "count": 5 }`，最多一次生成10个） |

### 系统设置

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/settings` | 全部系统设置（API keys / 开关） |
| PUT | `/api/admin/settings/{key}` | 更新单个设置（body: `{ "value": "..." }`） |

### 统计

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/stats` | 管理后台统计数据（total/online/pending/tagCount/byType） |

---

## 认证说明

- 公开接口无需任何认证头
- Bearer Token 接口需携带 `Authorization: Bearer <jwt_token>`
- Admin 接口除 JWT 外，还需用户的 `role = ADMIN`（Spring Security `.hasRole("ADMIN")`）
- JWT 无状态，不存储服务端 Session，过期时间 24h
- 注册限流：同 IP 1 分钟最多 3 次（Redis INCR + EXPIRE）

---

## 统一响应格式

**成功**：直接返回数据对象/列表/分页对象。

**错误**：
```json
{ "error": "错误描述" }
```

**分页对象**（MyBatis-Plus IPage）：
```json
{
  "records": [...],
  "total": 100,
  "size": 12,
  "current": 1,
  "pages": 9
}
```

---

## Swagger UI

开发环境：`http://localhost:8080/swagger-ui/index.html`

> 生产环境建议在 `SecurityConfig` 中限制 Swagger 访问（或完全关闭）。
