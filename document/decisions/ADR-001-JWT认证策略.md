# ADR-001: 使用 JWT 进行无状态认证

## 状态
已接受（Accepted）

## 日期
2025-01-15（项目初期）

## 上下文

我们需要为 DemoNet 选择认证策略。关键需求：

- **前后端分离架构**：前端 Vue 3 + 后端 Spring Boot，通过 REST API 通信
- **无状态 API**：便于水平扩展和负载均衡
- **移动端友好**：便于未来开发移动端应用
- **第三方集成**：需要支持外部服务调用 API（如抓取管道）

### 可选方案

| 方案 | 优点 | 缺点 |
|------|------|------|
| **Session + Cookie** | 简单、内置 Spring Security 支持 | 需要 Session 存储（Redis）、CSRF 防护、跨域复杂 |
| **JWT（当前方案）** | 无状态、自包含、便于水平扩展 | Token 撤销复杂、Token 体积较大 |
| **OAuth2** | 标准协议、第三方登录支持 | 复杂度高、需要授权服务器 |
| **API Key** | 简单、适合机器调用 | 不适合用户认证、权限粒度粗 |

## 决策

**使用 JWT（JSON Web Token）进行认证**：

- Token 包含用户 ID、角色（`ROLE_USER` / `ROLE_ADMIN`）
- 签名算法：HMAC-SHA256（对称加密，性能好）
- 密钥通过环境变量 `JWT_SECRET` 注入（256-bit）
- Token 过期时间：24 小时（`86400000ms`）
- 前端通过 `Authorization: Bearer <token>` 传递
- 后端通过 `JwtAuthFilter`（在 `UsernamePasswordAuthenticationFilter` 之前执行）解析

### 实现细节

```java
// JwtTokenProvider.java
String userId = Jwts.parser()
    .verifyWith(secretKey)
    .build()
    .parseSignedClaims(token)
    .getPayload()
    .get("userId", Long.class);
```

**注意**（审计报告 SEC-1）：
- ✅ 密钥必须通过环境变量注入，禁止硬编码
- ⚠️ 当前 `auth.getPrincipal()` 返回 `Long` 但无类型检查（待优化）

## 替代方案被拒绝的原因

### Session + Cookie
- ❌ 需要 Session 共享存储（Redis），增加基础设施复杂度
- ❌ 跨域配置复杂（需要设置 `SameSite`、`Secure` 等 Cookie 属性）
- ❌ 移动端 API 调用不方便（需要手动管理 Cookie）

### OAuth2
- ❌ 当前无第三方登录需求（微信/QQ/GitHub 登录暂未规划）
- ❌ 需要额外的授权服务器（如 Keycloak、Auth0）
- ❌ 过度设计，增加开发和维护成本

### API Key
- ❌ 不适合用户认证（粒度太粗）
- ❌ 无法表达用户角色和权限

## 后果

### ✅ 正面影响
- **无状态**：后端服务可以水平扩展，无需 Session 共享
- **跨域友好**：无需 Cookie，避免 CORS + CSRF 复杂配置
- **移动端友好**：Token 可以存储在本地，便于移动 App 调用
- **性能好**：HMAC-SHA256 比 RSA 快，适合高并发场景

### ⚠️ 负面影响
- **Token 撤销复杂**：JWT 无法主动失效，需要等待过期或维护黑名单
  - 当前通过 `logout` 删除 Redis 中的 Token 黑名单实现
- **Token 体积**：JWT 比 Session ID 大（约 200-500 bytes），增加带宽消耗
- **密钥管理**：密钥泄露会导致所有 Token 失效，需要严格的密钥轮换机制

### 📋 待优化项
1. **类型安全**：`auth.getPrincipal()` 应返回 `Long` 类型，而非强制转型
2. **Token 黑名单**：使用 Redis 存储已注销的 Token
3. **密钥轮换**：支持多密钥版本，实现无缝密钥轮换

## 参考资料
- [RFC 7519 - JSON Web Token](https://tools.ietf.org/html/rfc7519)
- [Spring Security JWT 官方文档](https://spring.io/guides/tutorials/spring-boot-oauth2/)
- 审计报告：`document/10-代码质量与后端架构审查报告.md`（SEC-1）
