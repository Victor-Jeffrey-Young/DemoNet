# CI/CD 工作流修复计划

## 1. 背景与目标

2026-07-12 的 `main` 分支提交 `1191e6b` 同时触发了四套重叠的 GitHub Actions 工作流。结果表现为：

- `CI/CD` 的前后端构建成功，但后端测试出现 7 个失败和 1 个错误，生产部署被跳过。
- `ci.yml` 在创建 Job 前失败，没有可用日志。
- `docker-build.yml` 在测试失败的同时成功构建并推送镜像。
- `deploy.yml` 只依赖 Docker 构建结果，理论上可以绕过主 CI 的测试质量门禁。

本次修复目标是恢复测试、消除重复流水线，并确保只有通过全部质量门禁的提交才能构建生产镜像和触发部署。

## 2. 根因分析

### 2.1 Controller 测试认证对象未注入

`ReviewController` 和 `UserItemController` 接收的是 Spring Security `Authentication` 参数。Standalone MockMvc 的标准解析器从请求的 `Principal` 获取该对象；此前使用 `AuthenticationPrincipalArgumentResolver` 或追加自定义解析器，均没有覆盖标准 `PrincipalMethodArgumentResolver` 的解析路径，导致 Controller 收到 `null` 并返回 HTTP 500。

### 2.2 IGDB RestClient 链式 Mock 不完整

`IGDBService.executeQuery` 的实际调用链包含 `.body(bodyStr).retrieve()`。测试未模拟 `body(Object)` 的返回值，导致链条在 `retrieve()` 前中断；后续 stub 没有被消费，又触发 Mockito 严格模式的 `UnnecessaryStubbingException`。

### 2.3 多套工作流竞争且质量门禁割裂

仓库同时维护 `ci.yml`、`ci-cd.yml`、`docker-build.yml` 和 `deploy.yml`：

- 前后端构建重复执行，浪费 Runner 时间。
- 独立 Docker 流水线不依赖测试结果，失败提交仍会产出 `latest` 镜像。
- 独立部署流水线只检查 Docker 工作流，无法保证主 CI 已通过。
- 同一提交产生多个名称相近的 Check，失败定位和分支保护配置容易混淆。

## 3. 修复方案

### 阶段一：恢复后端测试

1. Controller 成功路径使用 MockMvc 请求的 `.principal(authentication)` 注入认证对象。
2. 参数校验失败路径不注入认证对象，保证测试只验证 DTO 校验行为。
3. 空响应使用原始响应体断言，避免 JsonPath 对空字符串求值失败。
4. IGDB 测试补齐 `body(Object)` 链式 stub，并仅对双重 token 查询造成的非必经 stub 使用宽松模式。

### 阶段二：收敛工作流

1. 保留 `.github/workflows/ci-cd.yml` 作为唯一主流水线。
2. 删除重复的 `ci.yml`、`docker-build.yml`、`deploy.yml`。
3. 主流水线继续并行构建前端和后端，后端测试依赖后端构建。
4. 生产部署必须依赖前端构建和后端测试，并受 `ENABLE_DEPLOY=true`、`main` push 两项条件约束。
5. Docker 镜像只在部署 Job 内、全部上游成功后构建和推送，防止失败提交覆盖 `latest`。

### 阶段三：质量门禁验证

提交前执行以下检查：

1. 目标回归测试：`mvn -B -ntp '-Dtest=IGDBServiceTest,ReviewControllerTest,UserItemControllerTest' test`
2. 后端完整测试：`mvn -B -ntp test`
3. 后端生产打包：`mvn -B -ntp package -DskipTests`
4. 前端锁定依赖安装：`pnpm install --frozen-lockfile`
5. 前端生产构建：`pnpm build`
6. GitHub Actions YAML 静态解析检查。
7. 暂存区敏感信息与非预期文件审查。

## 4. 发布与回滚策略

- 本次只提交到本地 Git，不自动推送；远程 CI 需在推送后验证。
- 若主流水线失败，优先回滚本次原子提交，恢复旧工作流文件，再根据失败 Job 单独修正。
- 生产镜像保留 `<commit-sha>` 标签；`latest` 部署异常时，可将 `IMAGE_TAG` 指向上一成功 SHA 并重新执行 Compose 部署。
- `ENABLE_DEPLOY` 是生产发布总开关，排障期间应保持为 `false`，待 CI 全绿后再启用。

## 5. 验收标准

- 后端 96 个测试全部通过，无跳过或禁用测试。
- 前端生产构建成功。
- 仓库 Actions 页面每次提交只出现一套主 CI/CD 流水线。
- 测试失败时不会构建或推送生产镜像，也不会执行 SSH 部署。
- `main` push 且全部上游成功、`ENABLE_DEPLOY=true` 时才允许生产部署。
- Git commit 符合 `<type>(<scope>): <中文简短描述>`，正文使用简体中文说明根因和修复内容。

## 6. 后续改进

以下事项不纳入本次原子修复，建议后续独立实施：

- 增加前端 lint、类型检查和单元测试门禁。
- 引入 `actionlint` 或等价工具作为仓库内固定检查。
- 配置 `main` 分支保护，将唯一的 CI/CD Check 设为必需状态检查。
- 部署后增加健康检查和自动回滚。
- 固定第三方 Action 到提交 SHA，并定期更新依赖。
