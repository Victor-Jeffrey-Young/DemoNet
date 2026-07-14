# ADR-013: API 响应包络规范

## 状态
已接受（Accepted）

## 日期
2025-04-20

## 上下文

DemoNet 的前后端通过 REST API 通信，需要一个统一的响应格式：

- **前端拦截器**：需要统一的响应格式才能解包 `{code, message, data}`
- **错误处理**：统一的错误码和错误信息
- **类型安全**：前端需要知道响应的数据类型
- **第三方集成**：外部 API（如 Steam、TMDB）的响应格式不统一

### 关键需求
- **统一格式**：所有接口返回相同的结构
- **类型安全**：前端和后端都能推断出响应类型
- **易于解析**：前端拦截器可以自动解包
- **向后兼容**：新增字段不影响旧版本前端

### 可选方案

| 方案 | 优点 | 缺点 |
|------|------|------|
| **ApiResponse<T> 包络（当前方案）** | 统一、类型安全、易于解析 | 需要手动包装每个响应 |
| **直接返回数据** | 简单 | 无法统一处理错误 |
| **Spring HATEOAS** | RESTful、超媒体驱动 | 复杂度高、学习曲线陡 |

## 决策

**使用统一的 `ApiResponse<T>` 包络封装所有 API 响应**：

### ApiResponse 结构

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;           // HTTP 状态码或业务状态码
    private String message;     // 提示信息
    private T data;            // 响应数据
    private long timestamp;    // 时间戳（毫秒）

    // 快捷方法
    public static <T> ApiResponse<T> success(T data) { ... }
    public static <T> ApiResponse<T> error(int code, String message) { ... }
}
```

### 响应示例

**成功响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 123,
    "title": "黑神话：悟空",
    "type": "game",
    "slug": "black-myth-wukong"
  },
  "timestamp": 1711954800000
}
```

**失败响应**：
```json
{
  "code": 404,
  "message": "物品未找到",
  "data": null,
  "timestamp": 1711954800000
}
```

### Controller 实现

```java
@RestController
@RequestMapping("/api/items")
public class ItemController {

    @GetMapping("/{slug}")
    public ApiResponse<Item> getItemBySlug(@PathVariable String slug) {
        Item item = itemService.findBySlug(slug);
        if (item == null) {
            return ApiResponse.error(404, "物品未找到");
        }
        return ApiResponse.success(item);
    }

    @GetMapping("/hot")
    public ApiResponse<List<Item>> getHotItems(@RequestParam(defaultValue = "10") int limit) {
        List<Item> items = itemService.getHotItems(limit);
        return ApiResponse.success(items, "获取热门物品成功");
    }

    @PostMapping
    public ApiResponse<Item> createItem(@Valid @RequestBody CreateItemRequest request) {
        Item item = itemService.create(request);
        return ApiResponse.success(item, "创建物品成功");
    }
}
```

### 前端拦截器解包

```javascript
// frontend/src/api/request.js
axios.interceptors.response.use(
    response => {
        const { code, message, data } = response.data;

        // code == 200 时解包 data
        if (code === 200) {
            return data;
        }

        // code != 200 时 reject
        return Promise.reject(new Error(message || '请求失败'));
    },
    error => {
        // 401/403 跳转到登录页
        if (error.response?.status === 401 || error.response?.status === 403) {
            router.push('/login');
        }
        return Promise.reject(error);
    }
);
```

### 全局异常处理器

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ApiResponse.error(400, message);
    }

    @ExceptionHandler(ItemNotFoundException.class)
    public ApiResponse<Void> handleNotFound(ItemNotFoundException e) {
        return ApiResponse.error(404, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleGeneric(Exception e) {
        log.error("Unhandled exception", e);
        return ApiResponse.error(500, "服务器内部错误");
    }
}
```

### 业务状态码规范

| code | 含义 | HTTP 状态码 |
|------|------|------------|
| **200** | 成功 | 200 OK |
| **400** | 参数错误 | 400 Bad Request |
| **401** | 未认证 | 401 Unauthorized |
| **403** | 无权限 | 403 Forbidden |
| **404** | 资源未找到 | 404 Not Found |
| **409** | 冲突（如 slug 重复） | 409 Conflict |
| **429** | 请求过于频繁 | 429 Too Many Requests |
| **500** | 服务器内部错误 | 500 Internal Server Error |

## 替代方案被拒绝的原因

### 直接返回数据
- ❌ **无法统一处理错误**：每个 Controller 需要单独处理异常
- ❌ **前端拦截器无法解包**：前端无法区分成功和失败

### Spring HATEOAS
- ❌ **复杂度高**：需要学习 HATEOAS 的概念和 API
- ❌ **过度设计**：DemoNet 是简单的 CRUD 应用，不需要超媒体驱动
- ❌ **前后端约定**：前后端已经约定好 `{code, message, data}` 格式，无需 HATEOAS

## 后果

### ✅ 正面影响
- **统一格式**：所有接口返回相同的结构，前端拦截器可以自动解包
- **类型安全**：`ApiResponse<T>` 的泛型确保类型安全
- **易于解析**：前端只需要解包一次，后续代码直接使用 `data`
- **向后兼容**：新增字段（如 `timestamp`）不影响旧版本前端

### ⚠️ 负面影响
- **代码冗余**：每个 Controller 方法都需要手动包装 `ApiResponse.success()`
- **嵌套层级**：响应多一层 `data` 包裹，增加序列化开销（可忽略不计）
- **HTTP 状态码被忽略**：前端拦截器仅判断 `code`，不关注 HTTP 状态码

### 📋 优化方向
1. **自动包装**：使用 `ResponseBodyAdvice` 自动包装所有响应，无需手动调用 `ApiResponse.success()`
2. **HTTP 状态码对齐**：`code` 与 HTTP 状态码保持一致（如 404 → code=404）
3. **OpenAPI 文档**：使用 SpringDoc 自动生成 API 文档，标注响应格式

## 参考资料
- [REST API 设计最佳实践](https://restfulapi.net/)
- 代码实现：`backend/src/main/java/com/example/demonet/common/ApiResponse.java`
- 异常处理器：`backend/src/main/java/com/example/demonet/controller/GlobalExceptionHandler.java`
- 前端拦截器：`frontend/src/api/request.js`
