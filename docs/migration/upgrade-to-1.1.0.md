# 升级到 1.1.0 指南

本指南帮助你从历史版本（1.0.x）平滑升级到 1.1.0 系列。

> ⚠️ **务必注意版本号**：`1.1.0`、`1.1.0-beta`、`1.1.0-beta2` 三个版本在 Spring Boot 2.7.x 下
> 运行期会报 `NoClassDefFoundError: ConnectionConfig`，**请勿升级到这三个版本**。
> 本次升级请直接使用 **1.1.0-beta3**，它修掉了该问题，功能与 1.1.0 完全一致。
> 原因详见 [v1.1.0-beta3 版本说明](../versions/v1.1.0-beta3.md)。

## 快速升级（90% 的情况）

### 步骤 1：更新依赖版本

```xml
<dependency>
    <groupId>com.jeesoul</groupId>
    <artifactId>jeesoul-ai-model</artifactId>
    <version>1.1.0-beta3</version>  <!-- 从 1.0.x 改为 1.1.0-beta3 -->
</dependency>
```

无需额外挂 `httpclient5` / `httpcore5` 补丁依赖。若你之前为绕开 1.1.0 系列的问题手工加过，可全部删除。

### 步骤 2：重新编译测试

```bash
mvn clean compile
mvn test
```

### 步骤 3：启动应用验证

```bash
mvn spring-boot:run
```

**完成！大多数情况下无需任何代码修改。**

---

## 特殊情况处理

### 情况 1：依赖了 Hutool 的传递依赖

**症状**：升级后出现 `ClassNotFoundException: cn.hutool.xxx`

**原因**：1.1.0 彻底移除了 Hutool 依赖，若你的代码通过本库传递依赖了 Hutool，升级后会找不到类。

**解决方案**：在自己的 `pom.xml` 中显式引入 Hutool

```xml
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-all</artifactId>
    <version>5.8.16</version>  <!-- 选择合适的版本 -->
</dependency>
```

### 情况 2：自定义了 HTTP 超时等参数

**旧方式**（1.0.x）：
```java
// 通过代码硬编码超时
HttpUtils.HttpConfig config = HttpUtils.HttpConfig.builder()
    .timeout(30000)
    .build();
```

**新方式**（1.1.0，推荐）：
```yaml
# application.yml
ai:
  http:
    timeout:
      socket: 30000  # 响应超时
```

**兼容性**：旧方式仍然有效，但推荐迁移到 YML 配置。

### 情况 3：高并发场景需要调整连接池

**新增配置**（1.1.0+）：

```yaml
ai:
  http:
    pool:
      max-total: 500          # 从默认 200 调大
      max-per-route: 500
    timeout:
      socket: 30000           # LLM 响应较慢，增加超时
```

详见 [HTTP_CONFIG.md](../HTTP_CONFIG.md)

---

## 验证清单

升级后请检查：

- [ ] 应用启动成功
- [ ] AI 模型调用正常（同步接口）
- [ ] 流式对话正常
- [ ] Token 统计准确
- [ ] 多模态功能正常（如使用）
- [ ] 思考模式正常（如使用）
- [ ] 无 Hutool 相关异常

---

## 性能优化建议

升级到 1.1.0 后，可以利用新的配置能力优化性能：

### 高并发场景

```yaml
ai:
  http:
    pool:
      max-total: 500
      max-per-route: 500
    timeout:
      connection-request: 10000
```

### LLM 流式输出场景

```yaml
ai:
  http:
    timeout:
      socket: 60000  # LLM 流式输出可能较慢
    keep-alive:
      duration: 30000
```

### 网络不稳定场景

```yaml
ai:
  http:
    timeout:
      connect: 15000
      socket: 60000
```

---

## 回滚方案

若升级后遇到问题，可以快速回滚：

```xml
<dependency>
    <groupId>com.jeesoul</groupId>
    <artifactId>jeesoul-ai-model</artifactId>
    <version>1.0.9-GA</version>  <!-- 回退到上一版本 -->
</dependency>
```

然后重新编译部署。

---

## 常见问题

### Q1：升级后性能有变化吗？

**A**：理论上性能会略有提升（Apache HttpClient 5.x 更优），实际使用中差异不大。可以通过配置调优进一步优化。

### Q2：需要修改代码吗？

**A**：大多数情况下不需要。除非你通过本库传递依赖了 Hutool，需要显式引入。

### Q3：配置了 HTTP 参数后需要重启吗？

**A**：是的。HTTP 客户端在首次请求时初始化，配置在应用启动后不可动态修改。

### Q4：如何监控连接池状态？

**A**：1.1.0 暂未提供连接池监控，可在日志中开启 DEBUG 级别查看请求耗时：

```yaml
logging:
  level:
    com.jeesoul.ai.model.http.engine.HttpClientEngine: DEBUG
```

### Q5：响应超时和连接超时有什么区别？

**A**：
- **连接超时（connect）**：建立 TCP 连接的时间
- **响应超时（socket）**：等待服务器返回数据的时间，也叫读取超时

LLM 场景主要调整响应超时。

---

## 获取帮助

- 📖 查看 [HTTP_CONFIG.md](../HTTP_CONFIG.md) 了解详细配置
- 📖 查看 [v1.1.0.md](../versions/v1.1.0.md) 了解版本详情
- 📖 查看 [v1.1.0-beta3.md](../versions/v1.1.0-beta3.md) 了解 HttpClient5 兼容性修复详情
- 🐛 遇到 Bug？提交 [Issue](https://github.com/jeesoul/jeesoul-ai-model/issues)
- 💬 技术交流？查看项目 README 联系方式

---

## 总结

1.1.0 系列是**完全向后兼容**的版本，升级非常简单：
1. 修改依赖版本号（用 **1.1.0-beta3**，不要用 1.1.0 / beta / beta2）
2. 重新编译测试
3. （可选）利用新的 HTTP 配置优化性能

运行环境不受限制：编译产物为 Java 8 字节码，JDK 8/11/17/21 均可运行，
Spring Boot 2.7.x 与 3.x 均已验证可用。

**升级收益**：
- ✅ 更稳定的 HTTP 客户端（Apache HttpClient 5.x）
- ✅ 可配置的连接池和超时参数
- ✅ 更少的依赖传递
- ✅ 更好的企业级支持
