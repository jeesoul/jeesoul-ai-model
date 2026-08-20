# HTTP 客户端配置说明

## 概述

从 1.1.0 版本开始，HTTP 客户端的所有核心参数均可通过 `application.yml` 配置文件进行调整，包括连接池大小、超时时间、连接保活等参数。

## 默认配置

如果不配置任何参数，框架将使用以下默认值（已针对 LLM 调用场景优化）：

```yaml
ai:
  http:
    pool:
      max-total: 200              # 连接池最大连接数
      max-per-route: 200          # 每个路由最大连接数
      evict-idle-seconds: 30      # 空闲连接回收阈值（秒）
      time-to-live-seconds: 30    # 连接最长存活时间（秒）
    timeout:
      connect: 5000               # 连接超时（毫秒）
      socket: 10000               # Socket 读取超时（毫秒）
      connection-request: 5000    # 从连接池获取连接的超时（毫秒）
    keep-alive:
      duration: 20000             # 连接保活时间（毫秒）
      enable-retry: false         # 是否启用自动重试（默认关闭）
```

## 配置项详解

### 1. 连接池配置 (`ai.http.pool`)

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `max-total` | int | 200 | 连接池最大连接数，建议根据并发量调整 |
| `max-per-route` | int | 200 | 每个路由（域名+端口）的最大连接数 |
| `evict-idle-seconds` | long | 30 | 空闲连接回收阈值（秒），超过此时间的空闲连接会被清理 |
| `time-to-live-seconds` | long | 30 | 连接最长存活时间（秒），无论是否活跃都会关闭 |

**调优建议**：
- 高并发场景：调大 `max-total`（如 500-1000）
- 单域名调用：确保 `max-per-route` >= `max-total`
- 长时间空闲：调小 `evict-idle-seconds` 释放资源

### 2. 超时配置 (`ai.http.timeout`)

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `connect` | int | 5000 | 建立 TCP 连接的超时时间（毫秒） |
| `socket` | int | 10000 | Socket 读取数据的超时时间（毫秒） |
| `connection-request` | int | 5000 | 从连接池获取连接的超时时间（毫秒） |

**调优建议**：
- LLM 流式输出：适当调大 `socket`（如 30000-60000）
- 网络较差：调大 `connect`（如 10000）
- 连接池不足：调大 `connection-request` 或增加 `max-total`

### 3. 连接保活配置 (`ai.http.keep-alive`)

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `duration` | long | 20000 | 连接保活时间（毫秒），服务端未指定 Keep-Alive 时使用 |
| `enable-retry` | boolean | false | 是否启用自动重试（LLM 场景建议关闭） |

**调优建议**：
- 频繁请求：调大 `duration` 复用连接
- 幂等接口：可开启 `enable-retry`（但 LLM 调用不建议）

## 配置示例

### 示例 1：默认配置（无需配置）

如果默认值满足需求，无需在 `application.yml` 中添加任何配置。

### 示例 2：高并发场景

```yaml
ai:
  http:
    pool:
      max-total: 500
      max-per-route: 500
      evict-idle-seconds: 60
    timeout:
      connect: 10000
      socket: 30000
      connection-request: 10000
```

### 示例 3：低并发、长连接场景

```yaml
ai:
  http:
    pool:
      max-total: 50
      max-per-route: 50
      evict-idle-seconds: 120
      time-to-live-seconds: 300
    keep-alive:
      duration: 60000
```

### 示例 4：网络不稳定场景

```yaml
ai:
  http:
    timeout:
      connect: 15000
      socket: 60000
      connection-request: 10000
    keep-alive:
      enable-retry: true  # 谨慎开启，仅用于幂等接口
```

### 示例 5：仅调整部分参数

```yaml
ai:
  http:
    timeout:
      socket: 30000  # 只调整 Socket 超时，其他参数使用默认值
```

## 完整配置示例（包含模型配置）

```yaml
ai:
  # HTTP 客户端配置
  http:
    pool:
      max-total: 300
      max-per-route: 300
    timeout:
      connect: 5000
      socket: 30000
      connection-request: 5000
    keep-alive:
      duration: 20000
      enable-retry: false

  # 通义千问配置
  qwen:
    api-key: sk-xxxxxxxxxxxxxx
    model: qwen-plus
    temperature: 0.7
    max-tokens: 2000

  # ChatGPT 配置
  chat-gpt:
    api-key: sk-xxxxxxxxxxxxxx
    endpoint: https://api.openai.com/v1/chat/completions
    model: gpt-4
```

## 配置生效说明

1. **懒加载机制**：HTTP 客户端在首次请求时才初始化，此时读取配置
2. **单例模式**：客户端实例全局唯一，配置在应用启动后不可动态修改
3. **降级策略**：如果 Spring 容器未找到 `HttpClientProperties` Bean，自动使用默认配置

## 监控与调优

### 日志输出

启用 DEBUG 日志可查看每次请求的耗时：

```yaml
logging:
  level:
    com.jeesoul.ai.model.http.engine.HttpClientEngine: DEBUG
```

日志格式：
```
[HttpClientEngine]|POST|https://api.openai.com/v1/chat/completions|1234ms|200
```

### 调优流程

1. **监控日志**：观察请求耗时和超时情况
2. **识别瓶颈**：
   - 频繁超时 → 调大 `timeout.socket`
   - 连接池获取超时 → 调大 `pool.max-total`
   - 网络波动 → 调大 `timeout.connect`
3. **压测验证**：调整后进行压测验证效果
4. **持续优化**：根据生产数据持续调优

## 注意事项

1. **连接数设置**：`max-total` 需结合服务器和目标 API 的承载能力设置
2. **超时时间**：LLM 流式输出可能耗时较长，`socket` 超时不宜过小
3. **重试策略**：LLM 调用通常非幂等，不建议开启 `enable-retry`
4. **JVM 参数**：高并发场景注意 JVM 堆内存和线程数配置
5. **向后兼容**：未配置时使用默认值，完全向后兼容历史版本

## 版本历史

- **1.1.0**：新增 HTTP 客户端配置支持，所有参数可通过 YML 配置
