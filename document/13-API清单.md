# API 清单

---

## 公开（无需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/items` | 分页列表，支持 type/tag/keyword 筛选 |
| GET | `/api/items/{slug}` | 按 slug 获取详情 |
| GET | `/api/items/hot` | 热门列表 |
| GET | `/api/items/types` | 品类统计 |
| GET | `/api/items/recommended` | 推荐列表 |
| GET | `/api/items/featured` | 精选列表（轮播用），支持 type 参数 |
| GET | `/api/tags` | 全部标签 |
| GET | `/api/tags/items/{id}` | 某 item 的标签 |
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录，返回 JWT token |

## Bearer Token

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/auth/me` | 获取当前用户信息 |
| POST | `/api/user/items` | 收藏 item |
| GET | `/api/user/items` | 我的收藏列表 |
| DELETE | `/api/user/items/{id}` | 取消收藏 |
| POST | `/api/admin/fetch/steam` | 触发 Steam 抓取（含 targetType） |
| POST | `/api/admin/fetch/tmdb` | 触发 TMDB 电影抓取（含 targetType） |
| POST | `/api/admin/fetch/tmdb-tv` | 触发 TMDB 剧集抓取 |
| POST | `/api/admin/fetch/anilist` | 触发 AniList 动漫抓取 |
| POST | `/api/admin/fetch/bangumi` | 触发 Bangumi 中文动漫抓取 |
| POST | `/api/admin/fetch/itunes` | 触发 iTunes 音乐抓取 |
| POST | `/api/admin/fetch/igdb` | 触发 IGDB 游戏抓取（支持 search/popular/recent） |
| GET | `/api/admin/pending` | 待审核列表 |
| PUT | `/api/admin/approve/{id}` | 审核通过 |
| PUT | `/api/admin/reject/{id}` | 审核拒绝 |
| PUT | `/api/admin/items/{id}/featured` | 切换轮播状态（追加/移除） |
| GET | `/api/admin/carousel/{type}` | 获取当前轮播序列 |
| POST | `/api/admin/carousel/{type}` | 保存轮播序列 |
| DELETE | `/api/admin/carousel/{type}/{itemId}` | 从轮播移除 |

## 其他（需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/items` | 创建 item |
| PUT | `/api/items/{id}` | 更新 item |
| DELETE | `/api/items/{id}` | 删除 item |
| POST | `/api/tags` | 创建标签 |
| DELETE | `/api/tags/{id}` | 删除标签 |
| POST | `/api/tags/items/{id}` | 给 item 打标签 |
| DELETE | `/api/tags/items/{id}` | 移除 item 标签 |

## 待实现

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/reviews` | 获取评论列表 |
| POST | `/api/reviews` | 提交评论 |
| POST | `/api/admin/upload-poster/{id}` | 上传竖版海报 |
