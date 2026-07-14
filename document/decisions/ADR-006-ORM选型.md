# ADR-006: ORM 选型 — MyBatis-Plus

## 状态
已接受（Accepted）

## 日期
2025-01-20

## 上下文

DemoNet 后端需要 ORM 框架简化数据库操作，同时保持 SQL 的灵活性和性能。

### 关键需求
- **灵活的 SQL 控制**：复杂的 JOIN 查询、分页、排序
- **性能优化**：分页插件、懒加载、二级缓存
- **多态内容模型**：`info_json` JSON 字段的处理
- **代码生成**：Entity、Mapper、Service、Controller 的 CRUD 代码生成
- **团队熟悉度**：降低学习成本，提高开发效率

### 可选方案

| 方案 | 优点 | 缺点 |
|------|------|------|
| **MyBatis-Plus（当前方案）** | 灵活、性能好、插件丰富 | XML 配置繁琐 |
| **Spring Data JPA** | 声明式查询、自动化 | 复杂查询性能差、学习曲线陡 |
| **MyBatis（原生）** | 完全控制 SQL | 需要手动编写大量重复代码 |
| **jOOQ** | 类型安全的 SQL 构建 | 学习成本高、国内生态不如 MyBatis |

## 决策

**使用 MyBatis-Plus 3.5.16 作为 ORM 框架**：

### 核心特性
| 特性 | 配置/用法 |
|------|----------|
| **分页插件** | `PaginationInnerInterceptor`（物理分页） |
| **乐观锁插件** | `OptimisticLockerInnerInterceptor` |
| **逻辑删除** | `@TableLogic`（软删除） |
| **自动填充** | `@TableField(fill = FieldFill.INSERT)` |
| **JSON 处理器** | `tools.jackson` + `@TableField(typeHandler = JsonTypeHandler.class)` |

### 配置示例（MybatisPlusConfig.java）
```java
@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
```

### Mapper 继承结构
```java
public interface ItemMapper extends BaseMapper<Item> {
    // 自定义查询方法
    List<Item> selectHotItems(@Param("limit") int limit);
}
```

### XML Mapper 示例（ItemMapper.xml）
```xml
<select id="selectHotItems" resultType="com.example.demonet.entity.Item">
    SELECT * FROM items
    WHERE status = 1
    ORDER BY hot_score DESC
    LIMIT #{limit}
</select>
```

## 替代方案被拒绝的原因

### Spring Data JPA
- ❌ **复杂查询性能差**：JPA 的 JPQL 在处理复杂 JOIN 和多表查询时性能不如原生 SQL
- ❌ **N+1 查询问题**：懒加载容易导致 N+1 查询，需要手动优化
- ❌ **学习曲线陡峭**：团队对 MyBatis 更熟悉，JPA 的概念（Entity Manager、Persistence Context）需要时间掌握
- ❌ **分页插件不灵活**：JPA 的分页不如 MyBatis-Plus 的物理分页高效

### MyBatis（原生）
- ❌ **重复代码多**：Entity、Mapper、Service、Controller 的 CRUD 代码需要手动编写
- ❌ **分页需要手动实现**：MyBatis-Plus 的分页插件开箱即用
- ❌ **逻辑删除需要手动处理**：MyBatis-Plus 的 `@TableLogic` 自动处理软删除

### jOOQ
- ❌ **学习成本高**：jOOQ 的 DSL API 需要时间掌握
- ❌ **国内生态不如 MyBatis**：遇到问题更容易找到 MyBatis 的解决方案
- ❌ **JSON 支持不如 MyBatis-Plus**：MyBatis-Plus 的 `JsonTypeHandler` 更易用

## 后果

### ✅ 正面影响
- **SQL 灵活**：MyBatis 的 XML 配置允许完全控制 SQL，适合复杂查询
- **性能好**：物理分页、懒加载、二级缓存等插件提升性能
- **开发效率高**：`BaseMapper` 提供开箱即用的 CRUD 方法
- **团队熟悉**：团队成员对 MyBatis 更熟悉，降低维护成本

### ⚠️ 负面影响
- **XML 配置繁琐**：复杂的 SQL 需要写在 XML 文件中，代码分散
- **类型安全差**：XML 中的 SQL 字符串无法在编译时检查
- **调试困难**：XML 中的 SQL 错误在运行时才暴露

### 📋 优化方向
1. **减少 XML**：对于简单的 CRUD，尽量使用 MyBatis-Plus 的 `BaseMapper` 方法
2. **代码生成器**：使用 MyBatis-Plus 的代码生成器生成 Entity、Mapper、Service、Controller
3. **Lambda 表达式**：使用 `LambdaQueryWrapper` 替代 XML 查询（类型安全）

## 参考资料
- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [MyBatis 官方文档](https://mybatis.org/mybatis-3/zh/index.html)
- 数据模型：`document/03-数据模型.md`
