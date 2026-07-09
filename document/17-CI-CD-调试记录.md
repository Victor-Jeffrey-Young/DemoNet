# 17. CI/CD 调试记录

> GitHub Actions 工作流首次推送后的故障排查全过程（2026-07-10）

---

## 背景

CI/CD 工作流（`.github/workflows/ci-cd.yml`）编写完成后推送到 GitHub，首次运行即失败。经 3 轮调试修复，涉及两个版本升级导致的 breaking change。本文件记录完整诊断过程，供后续排查参考。

### 提交历史

| 轮次 | commit | 内容 | 结果 |
|------|--------|------|------|
| 初始 | `19738f4` | 首次推送 CI/CD 工作流 | ❌ 前端+后端测试失败 |
| 1 | `23d75cd` | 升级 Node 20 → 22 | ✅ 前端修复 |
| 2 | `be486ba` | flyway-core → spring-boot-starter-flyway | ✅ 后端测试修复 |
| 3 | `05c155b` | 清理诊断步骤 | ✅ 全部通过 |

---

## 问题 1：前端构建失败 — pnpm 与 Node 版本不兼容

### 现象

`构建前端 (Vue 3 + Vite)` Job 在"安装 Node.js"步骤崩溃：

```
warn: This version of pnpm requires at least Node.js v22.13
warn: The current version of Node.js is v20.20.2

Error [ERR_UNKNOWN_BUILTIN_MODULE]: No such built-in module: node:sqlite
    at Module._load (node:internal/modules/cjs/loader:1031:13)
    ...
    at ../store/index/lib/index.js (pnpm.mjs:56737:25)
```

### 诊断

错误信息已足够明确：
1. pnpm 11.10.0 要求 Node.js ≥ 22.13
2. 工作流配置的是 Node 20（`NODE_VERSION: '20'`）
3. pnpm 11 内部使用了 `node:sqlite` 内置模块，该模块在 Node 20 中不存在，直接崩溃

### 根因

pnpm 11.10.0 的 `package.json` 声明了 `packageManager: pnpm@11.10.0`，但该版本依赖 Node.js 22+ 才有的 `node:sqlite` 模块。工作流初始配置沿用了 Node 20（GitHub Actions 的旧默认），版本不匹配。

### 解决方案

```yaml
# .github/workflows/ci-cd.yml
env:
  NODE_VERSION: '22'   # 原为 '20'
```

**教训**：包管理器版本与 Node 版本有硬性依赖关系，CI 的 Node 版本必须与 `packageManager` 字段声明的版本兼容。

---

## 问题 2：后端集成测试失败 — Spring Boot 4.x Flyway 自动配置变更

### 现象

`后端集成测试` Job 在"运行测试"步骤失败（exit code 1），其余步骤（service container 初始化、MySQL 等待、JDK 安装）均成功。下载测试报告（surefire-reports artifact）后看到：

```
Tests run: 3, Failures: 0, Errors: 3, Skipped: 0

java.lang.IllegalStateException: Failed to load ApplicationContext
...
Caused by: org.springframework.jdbc.BadSqlGrammarException:
    bad SQL grammar [SELECT COUNT(*) FROM users]
    at DataImportService.seedUser(DataImportService.java:716)
    at DataImportService.run(DataImportService.java:101)
```

`DataImportService` 是 `CommandLineRunner`，启动时执行 `SELECT COUNT(*) FROM users`，但 `users` 表不存在。

### 诊断过程

**第一步：确认迁移文件是否在 git 中**

```bash
git ls-files 'backend/**/*.sql'
# → V1__Baseline.sql, V2__Add_indexes.sql, V3__Add_foreign_keys.sql 都在
```

排除"迁移文件未提交"的可能。

**第二步：在 CI 中加诊断步骤**

在"运行测试"前插入诊断步骤，检查迁移文件和数据库状态：

```yaml
- name: 诊断 Flyway 迁移状态
  working-directory: backend
  run: |
    mvn -B -ntp process-resources -q
    find target/classes/db/migration -name "*.sql"
    mysql -h 127.0.0.1 -uroot -pcitest demonet -e "SHOW TABLES;"
```

结果：
- ✅ 迁移文件**存在**于 `target/classes/db/migration/`（V1/V2/V3 三个文件）
- ❌ MySQL 数据库**完全为空**（SHOW TABLES 无输出）

**第三步：加 Flyway DEBUG 日志**

```yaml
run: mvn -B -ntp test -Dlogging.level.org.flywaydb=DEBUG
```

在日志中搜索 `FlywayAutoConfiguration`——**完全没有出现**。说明 Spring Boot 根本没有触发 Flyway 自动配置。

### 根因

**Spring Boot 4.x 的 breaking change**：

| 版本 | 行为 |
|------|------|
| Spring Boot 3.x | classpath 有 `flyway-core` → Flyway 自动配置生效，启动时自动迁移 |
| Spring Boot 4.x | `flyway-core` 单独存在**不再触发**自动配置，必须用 `spring-boot-starter-flyway` |

项目 pom.xml 原配置：
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>        <!-- 3.x 够用，4.x 不够 -->
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

**为什么本地没发现问题**：本地数据库的表在之前（可能 Spring Boot 3.x 时期或手动执行 SQL）已经建好，Flyway 不运行也不影响。CI 用全新空数据库，Flyway 不运行 → 表不存在 → DataImportService 崩溃。

### 解决方案

```xml
<!-- pom.xml：flyway-core 替换为 spring-boot-starter-flyway -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>        <!-- 保留：MySQL SQL 方言支持 -->
</dependency>
```

`spring-boot-starter-flyway` 会引入 `flyway-core` + Spring Boot 的 Flyway 自动配置模块，迁移在启动时自动执行。

**验证**：推送后 CI 重跑，后端集成测试通过（1m20s），3 个测试全绿。

---

## 调试方法论

### GitHub Actions CI 失败的诊断流程

```
1. gh run view <run-id>
   → 查看各 Job 状态，定位失败发生在哪个步骤

2. gh run view <run-id> --log-failed
   → 获取失败步骤的详细日志（可能因网络问题失败，需重试）

3. gh run download <run-id> -n <artifact-name> -D <dir>
   → 下载 artifact（测试报告、构建产物等）本地分析

4. 在工作流中加诊断步骤
   → 当日志不够时，在失败步骤前插入 shell 命令输出关键信息

5. 搜索自动配置报告
   → grep "AutoConfiguration\|did not match\|ConditionalOnClass" 定位配置问题
```

### 关键命令速查

```bash
# 查看 run 列表
gh run list --limit 5

# 查看 run 详情（Job 概览 + annotations）
gh run view <run-id>

# 等待 run 完成
gh run watch <run-id> --exit-status

# 下载测试报告 artifact
gh run download <run-id> -n backend-test-reports -D tmp-reports

# 查看单个 job 日志（grep 过滤）
gh run view --job <job-id> --log 2>&1 | grep -i "flyway"
```

---

## 经验总结

### 1. 大版本升级的 breaking change 是 CI 失败高发区

本项目使用 Spring Boot 4.0.6 + pnpm 11，都是相对新的大版本。CI 空环境会暴露本地因历史数据而隐藏的配置问题。

### 2. "本地能跑" ≠ "配置正确"

本地数据库残留的表掩盖了 Flyway 未自动运行的问题。CI 的价值正在于此——从零开始验证配置的正确性。

### 3. 诊断步骤是排查利器

当 CI 日志不够时，在失败步骤前插入诊断 shell 命令（检查文件、查询数据库、打印环境变量），是最直接的定位方法。确认问题后记得清理。

### 4. Spring Boot 4.x 升级检查清单

从 3.x 升级到 4.x 时，需检查：
- [ ] Flyway：`flyway-core` → `spring-boot-starter-flyway`
- [ ] 其他自动配置是否也改为需要显式 starter（查阅 [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)）
- [ ] Jackson 3 迁移（`tools.jackson.*` 包路径变化）
- [ ] `javax.*` → `jakarta.*` 命名空间迁移
