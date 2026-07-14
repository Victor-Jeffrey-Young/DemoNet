# ADR-011: 外部 API 适配器模式

## 状态
已接受（Accepted）

## 日期
2025-04-10

## 上下文

DemoNet 需要从 7 个不同的外部 API 抓取数据并统一存储到 `items` 表：

- **Steam**（游戏）、**TMDB**（电影/动漫）、**IGDB**（游戏）
- **AniList**（动漫）、**Bangumi**（动漫）、**iTunes**（音乐）、**SteamGridDB**（海报）

每个外部 API 的数据格式、认证方式、响应结构都完全不同，但最终都需要转换为统一的 `List<Item>` 格式。

### 关键需求
- **统一接口**：所有适配器对外暴露相同的接口
- **易于扩展**：新增数据源时只需实现适配器接口
- **错误隔离**：一个适配器的失败不影响其他适配器
- **认证管理**：每个适配器管理自己的认证（API Key、OAuth）

### 可选方案

| 方案 | 优点 | 缺点 |
|------|------|------|
| **适配器模式（当前方案）** | 统一接口、易于扩展、错误隔离 | 需要实现 7 个适配器 |
| **策略模式** | 运行时切换适配器 | 不适合静态已知的数据源 |
| **直接调用** | 简单直接 | 代码重复、难以维护、无法统一处理 |

## 决策

**使用适配器模式（Adapter Pattern）封装 7 个外部 API**：

### 适配器接口

```java
public interface ExternalApiAdapter {
    /**
     * 根据外部 ID 抓取物品数据
     * @param externalId 外部系统 ID（如 Steam AppID、TMDB ID）
     * @return 归一化的 Item 列表（通常为 1 个，也可能为多个）
     */
    List<Item> fetchById(String externalId);

    /**
     * 搜索物品
     * @param query 搜索关键词
     * @return 匹配的物品列表
     */
    List<Item> search(String query);

    /**
     * 适配器类型
     */
    String getType();
}
```

### 适配器实现示例（Steam）

```java
@Service
public class SteamService implements ExternalApiAdapter {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<Item> fetchById(String appId) {
        try {
            // 1. 调用 Steam API
            String url = "https://store.steampowered.com/api/appdetails?appids=" + appId;
            String response = restClient.get(url);

            // 2. 解析 Steam 特有的 JSON 格式
            JsonNode root = objectMapper.readTree(response);
            JsonNode appData = root.path(appId).path("data");

            if (appData.isMissingNode()) {
                return List.of();
            }

            // 3. 转换为统一 Item 格式
            Item item = new Item();
            item.setType("game");
            item.setExternalId(appId);
            item.setTitle(appData.get("name").asText());
            item.setSlug(slugify(appData.get("name").asText()));
            item.setDescription(appData.get("short_description").asText());
            item.setCoverUrl(appData.path("screenshots").get(0).get("path_full").asText());
            item.setSource("steam");

            // 4. 设置 info_json（品类专属字段）
            Map<String, Object> info = Map.of(
                "developer", appData.get("developers").toString(),
                "publisher", appData.get("publishers").toString(),
                "price", appData.path("price_overview").path("final_formatted").asText(),
                "release_date", appData.path("release_date").get("date").asText()
            );
            item.setInfoJson(objectMapper.writeValueAsString(info));

            return List.of(item);

        } catch (Exception e) {
            log.error("Steam fetch failed: appId={}, error={}", appId, e.getMessage());
            return List.of();
        }
    }

    @Override
    public String getType() {
        return "game";
    }
}
```

### FetchConsumer 路由

```java
@Component
public class FetchConsumer {

    private final Map<String, ExternalApiAdapter> adapters;

    // Spring 自动注入所有 ExternalApiAdapter 实现
    public FetchConsumer(List<ExternalApiAdapter> adapterList) {
        this.adapters = adapterList.stream()
            .collect(Collectors.toMap(ExternalApiAdapter::getType, Function.identity()));
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_STEAM)
    public List<Item> handleSteamFetch(Map<String, Object> message) {
        String externalId = (String) message.get("externalId");
        return adapters.get("steam").fetchById(externalId);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_TMDB)
    public List<Item> handleTMDBFetch(Map<String, Object> message) {
        String externalId = (String) message.get("externalId");
        return adapters.get("movie").fetchById(externalId);  // TMDB 可能是 movie 或 anime
    }
}
```

### 7 个适配器清单

| 适配器 | 类型 | 认证方式 | 数据格式 |
|--------|------|---------|---------|
| **SteamService** | game | 无认证 | JSON（非标准格式） |
| **TMDBService** | movie/anime | API Key | JSON（标准格式） |
| **IGDBService** | game | OAuth2（Twitch） | JSON（非标准格式） |
| **AniListService** | anime | 无认证 | GraphQL → JSON |
| **BangumiService** | anime | 无认证 | JSON（中文） |
| **ItunesService** | music | 无认证 | XML → JSON |
| **SteamGridDBService** | 图片 | API Key | JSON（图片 URL） |

## 替代方案被拒绝的原因

### 直接调用
- ❌ **代码重复**：每个 API 的认证、错误处理、转换逻辑都重复
- ❌ **难以维护**：修改统一逻辑需要改 7 个地方
- ❌ **无法统一错误处理**：每个 API 的错误处理不一致

### 策略模式
- ❌ **过度设计**：策略模式适合运行时切换，但 DemoNet 的数据源是静态已知的
- ❌ **配置复杂**：需要在配置文件或数据库中管理策略映射

## 后果

### ✅ 正面影响
- **统一接口**：所有适配器对外暴露相同的 `fetchById()` / `search()` 接口
- **易于扩展**：新增数据源只需实现 `ExternalApiAdapter` 接口 + 添加队列配置
- **错误隔离**：一个适配器的异常不会影响其他适配器
- **认证管理**：每个适配器独立管理认证（API Key、OAuth Token）

### ⚠️ 负面影响
- **代码量增加**：需要实现 7 个适配器，每个约 100-200 行代码
- **维护成本**：外部 API 更新时需要修改对应适配器
- **测试复杂度**：每个适配器都需要独立的测试用例

### 📋 优化方向
1. **统一错误处理**：提取公共的异常处理逻辑（如重试、降级）
2. **Token 缓存**：IGDB 的 OAuth Token 需要缓存到 Redis（有效期 1 天）
3. **速率限制**：Steam、TMDB 等 API 有速率限制，需要添加延迟或重试机制
4. **并行抓取**：一次抓取多个 ID 时，使用 `CompletableFuture` 并行调用多个适配器

## 参考资料
- [适配器模式（GoF）](https://en.wikipedia.org/wiki/Adapter_pattern)
- 适配器实现：`backend/src/main/java/com/example/demonet/service/*Service.java`
- Consumer 路由：`backend/src/main/java/com/example/demonet/service/FetchConsumer.java`
- 队列配置：`backend/src/main/java/com/example/demonet/config/RabbitMQConfig.java`
- 外部数据源文档：`document/09-外部数据源集成.md`
