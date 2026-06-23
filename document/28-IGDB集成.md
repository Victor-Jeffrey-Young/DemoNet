# IGDB 集成

> **状态**：✅ 已完成（2026-06-24）
> **来源**：[IGDB API v4](https://api-docs.igdb.com/)（Twitch 旗下游戏数据库）

---

## 概述

IGDB（Internet Game Database）是 Twitch 旗下的免费游戏数据库，收录 20 万+ 游戏信息，包括元数据、封面、截图、视频、评分、多平台链接等。

集成的目标不是自动发现"有试玩版的游戏"（IGDB 也没有这个字段），而是**大幅降低手动录入游戏信息的工作量**。管理员搜索/拉取热门游戏后，只需审核并手动标注 `demo_available` 即可。

---

## 能力

| 端点 | 说明 | 输入 |
|------|------|------|
| `search` | 名称模糊搜索 | `query`="Hades" → 返回匹配游戏 |
| `popular` | 高评分热门游戏 | `limit`=10 → 评分 ≥80 且评分人数 ≥50 |
| `recent` | 近一年新游戏 | `limit`=10 → 一年内发布且评分 ≥70 |

每条游戏数据包含：

```
├── name              游戏名
├── summary           简介
├── cover.url         封面（自动补全为 https 分辨率版本）
├── first_release_date 首发日期
├── genres            [动作, 冒险, RPG...]
├── platforms         [PC, PS5, Switch...]
├── themes            [科幻, 恐怖, 奇幻...]
├── game_modes        [单人, 多人, 合作...]
├── rating / rating_count  IGDB 用户评分
├── screenshots       [截图 URL × N]
├── videos            [YouTube 预告片]
├── websites          [Steam/Epic/GOG/官网链接]
├── similar_games     相似游戏
├── involved_companies  开发商 / 发行商
```

---

## 接入方式

### 获取凭证

1. 注册 [Twitch 账号](https://www.twitch.tv/)
2. 进入 [Twitch Developer Console](https://dev.twitch.tv/console)
3. "Applications" → "Register Your Application"
4. Name 填 `DemoNet`，OAuth Redirect URL 填 `http://localhost`
5. 获取 **Client ID** 和 **Client Secret**

### 配置 .env

```bash
IGDB_CLIENT_ID=你的client_id
IGDB_CLIENT_SECRET=你的client_secret
```

### API 触发

```http
POST /api/admin/fetch/igdb
Content-Type: application/json
Authorization: Bearer <admin_jwt>

{
  "query": "Hades",
  "endpoint": "search",
  "limit": 10,
  "targetType": "game"
}
```

| 字段 | 说明 | 默认值 |
|------|------|--------|
| `query` | 搜索关键词 | 空 |
| `endpoint` | `search` / `popular` / `recent` | `search` |
| `limit` | 返回数量 | 10 |
| `targetType` | 入库 type | `game` |

---

## 架构

```
Admin UI → POST /api/admin/fetch/igdb
  → RabbitMQ (demonet.fetch.igdb)
    → FetchConsumer.handleIGDBFetch()
      → IGDBService.searchGames() / fetchPopularGames() / fetchRecentGames()
        → Twitch OAuth (id.twitch.tv/oauth2/token)
        → IGDB API (api.igdb.com/v4/games)
      → ItemService.createItem() → MySQL (status=0 待审核)
```

IGDB 使用 Twitch OAuth 认证（Client Credentials flow），token 自动缓存，过期前 60 秒刷新。

---

## IGDBService 代码位置

```
backend/src/main/java/com/example/demonet/service/IGDBService.java
```

| 方法 | 说明 |
|------|------|
| `searchGames(query, limit)` | 按名称搜索 |
| `fetchPopularGames(limit)` | 高评分热门 |
| `fetchRecentGames(limit)` | 近一年新品 |
| `fetchGameById(igdbId)` | 按 ID 获取详情 |

---

## 与其他数据源对比

| 能力 | IGDB | Steam Store API | RAWG |
|------|:--:|:--:|:--:|
| 游戏数量 | 200k+ | Steam only | 500k+ |
| 评分 | ✅ IGDB 用户 + 聚合评分 | ❌ | ✅ Metacritic |
| 多平台链接 | ✅ | ❌ | ✅ |
| 相似游戏 | ✅ | ❌ | ❌ |
| 截图 | ✅ | ✅ | ✅ |
| 预告片 | ✅ | ❌ | ✅ |
| API Key 难度 | 免费，需注册 Twitch | 无需 key | 免费，邮件注册 |
| 中文支持 | ⚠️ 游戏名以英文为主 | ✅ 价格/简介可中文 | ⚠️ 英文为主 |
