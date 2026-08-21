# jeesoul-ai-model

[![Maven Central](https://img.shields.io/maven-central/v/com.jeesoul/jeesoul-ai-model)](https://search.maven.org/artifact/com.jeesoul/jeesoul-ai-model)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-8+-blue)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.17-brightgreen)](https://spring.io/projects/spring-boot)

一个基于 Spring Boot 的 AI 大模型服务集成框架，把通义千问、ChatGPT、讯飞星火、DeepSeek、豆包等
主流大模型封装成统一调用方式。业务代码只面对统一入参 `ModelRequestVO` 和出参 `ModelResponseVO`，
**切换模型只需改一个模型名字符串**，其余代码零改动。

支持同步/流式对话、思考模式、多模态输入、Token 统计。引入依赖 + 配 api-key 即可使用。

> ## 🔴 升级必读：请删掉手工加的补丁依赖
>
> 若你曾为绕开旧版本（1.1.0 / beta / beta2）的 `NoClassDefFoundError: ConnectionConfig`，
> 在自己 pom 里手工补过 `httpclient5` / `httpcore5` / `httpcore5-h2` 这三个依赖，
> **升级到 1.1.0-beta3 后请全部删除**。beta3 已从代码层面修掉根因，补丁不再需要。
>
> 留着的三个后果：把你锁死在含 CVE-2026-64607 的 `5.2.3` 上；静默顶掉将来 Spring Boot BOM 的
> 安全更新（你以为升级了，实际没有）；不会立刻报错，所以极易被忘记。
>
> 删完执行 `mvn clean install -U` 刷新依赖即可，业务代码零改动。
> 要删的具体内容与验证命令见 📖 [兼容性说明](docs/compatibility.md#四旧版本补丁依赖必须删除)

## ✨ 核心特性

- 🎯 **统一接口** - 提供一致的 API，屏蔽不同模型的差异
- 🔌 **即插即用** - Spring Boot 自动配置，零代码集成
- 🌊 **流式支持** - 基于 WebFlux 的响应式流式对话
- 🧠 **思考模式** - 支持深度推理模式，获取模型思考过程
- 📊 **Token统计** - 完整的Token使用统计，支持成本分析
- 🎨 **多模态** - 支持文本、图片、视频等多种输入类型
- 🔧 **易扩展** - 支持动态注册自定义 AI 模型，无需改框架源码
- ✅ **参数校验** - 自动校验参数，提前发现错误

## 📦 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.jeesoul</groupId>
    <artifactId>jeesoul-ai-model</artifactId>
    <version>1.1.0-beta3</version>
</dependency>

<!-- 流式对话支持 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

同步 HTTP 基于内置的 Apache HttpClient 5.x 封装，无需额外引入 HTTP 依赖；
`spring-boot-starter-webflux` 仅在使用流式对话时需要。

运行环境：JDK 8 及以上（8 / 11 / 17 / 21 均可），Spring Boot 2.7.x / 3.x。
详见 📖 [兼容性说明](docs/compatibility.md)。

### 2. 配置参数

最小配置，配好 api-key 就能跑：

```yaml
ai:
  qwen:
    api-key: your-qwen-api-key
```

其余参数都有默认值。全部模型 + HTTP 连接池的完整配置见下方 [配置说明](#️-配置说明)。

### 3. 开始使用

```java
// 创建服务
AiService aiService = FactoryModelService.create("qWen");

// 构建请求
ModelRequestVO request = new ModelRequestVO()
    .setModelName("qWen")
    .setModel("qwen-turbo")
    .setPrompt("你好，请介绍一下自己");

// 同步调用
ModelResponseVO response = aiService.httpChat(request);
System.out.println(response.getResult());

// 流式调用
aiService.streamChatStr(request).subscribe(System.out::print);
```

## 🤖 支持的模型

| 模型名称 | 模型标识 | 思考模式 | Token统计 | 多模态 | 其他功能 |
|---------|---------|---------|----------|--------|---------|
| 通义千问 | `qWen` | ✅ 是 | ✅ 是 | ❌ 否 | System Prompt、多轮对话 |
| ChatGPT | `chatgpt` | ❌ 否 | ✅ 是 | ❌ 否 | System Prompt、多轮对话 |
| 讯飞星火 | `spark` | ✅ 是 | ✅ 是 | ❌ 否 | System Prompt、多轮对话 |
| DeepSeek | `deepSeek` | ✅ 是 | ✅ 是 | ❌ 否 | System Prompt、多轮对话 |
| 豆包 | `douBao` | ✅ 是 | ✅ 同步 | ❌ 否 | System Prompt、多轮对话 |
| 千问视觉 | `qwenVL` | ✅ 是 | ✅ 是 | ✅ 是 | 图片、视频分析、System Prompt、多轮对话 |

- **思考模式**：模型输出推理过程，提升答案准确性
- **Token统计**：详细的 Token 使用统计（豆包流式接口不支持）
- **多模态**：支持图片、视频等非文本输入

## 📚 使用指南

### 思考模式（深度推理）

```java
request.setEnableThinking(true);
ModelResponseVO response = aiService.httpChat(request);
if (response.getThinking()) {
    System.out.println("思考: " + response.getThinkingContent());
    System.out.println("答案: " + response.getResult());
}
```

### 流式对话

```java
// 纯文本流（最简单）
aiService.streamChatStr(request).subscribe(System.out::print);

// 完整信息流（含 Token 统计）
aiService.streamChat(request).subscribe(chunk -> {
    System.out.print(chunk.getResult());
    if (chunk.getUsage() != null) {
        System.out.println("\nToken: " + chunk.getUsage().getTotalTokens());
    }
});
```

流式调用**必须**调用 `subscribe()`，否则不会真正发起请求。

### Token 统计

```java
TokenUsageVO usage = aiService.httpChat(request).getUsage();
System.out.println("总Token: " + usage.getTotalTokens());
```

### 多模态（图片/视频分析）

```java
ModelRequestVO request = new ModelRequestVO()
    .setModelName("qwenVL")
    .setModel("qwen-vl-plus")
    .setContents(Arrays.asList(
        MessageContent.imageUrl("https://example.com/image.jpg"),
        MessageContent.text("这张图片里有什么？")
    ));
```

更多场景见 📖 [多模态视觉示例](examples/multimodal-vision/README.md)（8 个完整 API 示例，
含 OCR、视频理解、思考模式、流式分析）。

## ⚙️ 配置说明

**配置优先级**：请求参数 > YML 配置 > 代码默认值。所有参数均为可选，不配置就走默认值。
配置项用中划线（`api-key`）而非驼峰（`apiKey`）。

### 模型配置

```yaml
ai:
  qwen:
    api-key: your-qwen-api-key
    endpoint: https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation
    temperature: 0.7      # 采样温度，默认0.7
    top-p: 0.9            # 核采样参数，默认0.9
    max-tokens: 2000      # 最大token数，默认2000
    model: qwen-turbo     # 默认模型名称
  chat-gpt:
    api-key: your-chatgpt-api-key
    model: gpt-3.5-turbo
  spark:
    api-key: your-spark-api-key
    model: spark-v3.5
  deep-seek:
    api-key: your-deepseek-api-key
    model: deepseek-chat
  doubao:                 # 豆包（字节跳动）
    api-key: your-doubao-api-key
    endpoint: https://ark.cn-beijing.volces.com/api/v3/chat/completions
    model: doubao-seed-code-preview-251028
  qwen-vl:                # 多模态模型
    api-key: your-qwen-vl-api-key
    model: qwen-vl-plus
```

每个模型都支持 `temperature` / `top-p` / `max-tokens` / `model` 四个可选参数，
上面只在 `qwen` 下完整列出，其余模型按需添加即可。
模型服务仅在配置了对应 api-key 时才装配，没配的模型不会占用资源。

### HTTP 客户端配置（v1.1.0+）

同步 HTTP 的连接池、超时、保活参数都可以通过 YML 调整：

```yaml
ai:
  http:
    pool:
      max-total: 200              # 连接池最大连接数，默认200
      max-per-route: 200          # 每个路由最大连接数，默认200
      evict-idle-seconds: 30      # 空闲连接回收阈值（秒），默认30
      time-to-live-seconds: 30    # 连接最长存活时间（秒），默认30
    timeout:
      connect: 5000               # 连接超时（毫秒），默认5000
      socket: 10000               # 响应超时（毫秒），默认10000
      connection-request: 5000    # 从连接池获取连接的超时（毫秒），默认5000
    keep-alive:
      duration: 20000             # 连接保活时间（毫秒），默认20000
      enable-retry: false         # 是否自动重试，默认false（LLM 场景不建议开启）
```

两个常见场景的调整方向：

```yaml
# 高并发：加大连接池，放宽响应超时
ai:
  http:
    pool:
      max-total: 500
      max-per-route: 500
    timeout:
      socket: 30000

# 网络不稳定：放宽连接和响应超时
ai:
  http:
    timeout:
      connect: 15000
      socket: 60000
```

参数含义、调优流程、监控日志见 📖 [HTTP_CONFIG.md](HTTP_CONFIG.md)。

## 🔧 扩展自定义模型

给 Service 加个 `@AiModelService` 注解就能接入新模型，**不用改框架源码**：

```java
@Service
@AiModelService(modelName = "myai", serviceName = "myService")
public class MyService extends SparkService {
    public MyService(MyModelProperties modelConfig) {
        super(modelConfig);
    }
}
```

```java
AiService myService = FactoryModelService.create("myai");
```

完整三种扩展方式（注解注册、继承 `AbstractAiService` 完全自定义、手动注册）见
📖 [扩展自定义模型](docs/custom-model.md)。
可直接运行的范例见 📖 [Claude AI 集成示例](examples/claude-integration/README.md)。

## 📋 API 文档

**AiService** - 统一服务接口

| 方法 | 说明 |
|------|------|
| `httpChat()` | 同步对话，返回解析后的对象 |
| `streamChat()` | 流式对话，返回 `Flux<ModelResponseVO>`（完整信息） |
| `streamChatStr()` | 流式对话，返回 `Flux<String>`（纯文本） |
| `httpChatRaw()` / `streamChatRaw()` | 返回模型原始 JSON |

**ModelRequestVO** - 请求参数（支持链式调用）

- `modelName` 模型标识（必填）、`model` 模型版本（必填）
- `prompt` 用户提示词、`systemPrompt` 系统提示词
- `enableThinking` 思考模式开关
- `temperature` / `topP` / `maxTokens` 模型参数
- `contents` 多模态内容、`messages` 多轮对话消息

**ModelResponseVO** - 响应结果

- `result` 文本内容
- `thinking`（Boolean，是否思考）/ `thinkingContent`（String，思考内容）
- `modelProvider` / `modelName` 模型信息
- `usage` Token 使用统计

## 🚀 技术栈

| 技术/框架 | 版本 | 说明 |
|---------|------|------|
| Java | 8+ | 编译为 Java 8 字节码，可在 JDK 8/11/17/21 运行 |
| Spring Boot | 2.7.17 | 应用框架（兼容 2.7.x / 3.x） |
| Spring WebFlux | 2.7.17 | 响应式流式对话 |
| Apache HttpClient | 5.1.4 | 同步 HTTP（内置封装），兼容 5.1.x ~ 5.6.x |
| Lombok | Latest | 简化代码 |
| SLF4J | 1.7.36 | 日志门面 |

## 📖 架构设计

调用链：

```
FactoryModelService.create(模型名)
        ↓
AiStrategyContext（策略表：模型名 → Spring Bean 名）
        ↓
XxxService extends AbstractAiService
        ↓
HttpUtils（同步）/ StreamHttpUtils（流式）
        ↓
各家大模型 HTTP 端点
```

设计模式：**工厂**（`FactoryModelService` 统一创建）+ **策略**（`AiStrategyContext` 动态选择）
+ **模板方法**（`AbstractAiService` 抽出公共逻辑）+ **建造者**（`ModelRequestVO` 链式调用）。

核心包结构：

```
com.jeesoul.ai.model/
├── annotation/   # @AiModelService 自定义模型注册注解
├── config/       # 自动配置类 + AiProperties 配置属性
├── constant/     # AiModel 模型枚举等
├── factory/      # FactoryModelService 工厂入口
├── http/         # 内置 HTTP 封装（基于 Apache HttpClient 5.x）
├── request/      # 各模型请求体
├── response/     # 各模型响应体
├── service/      # 各模型实现 + AbstractAiService 基类
├── strategy/     # AiStrategyContext 策略表
├── util/         # HttpUtils / StreamHttpUtils / JsonUtils
└── vo/           # 统一出入参 ModelRequestVO / ModelResponseVO
```

## 💡 最佳实践与注意事项

- 🔐 **API 密钥安全** - 用环境变量或配置中心，不要硬编码
- 💰 **成本控制** - 思考模式消耗更多 Token，监控用量、按需开启
- 📊 **Token 统计** - 豆包流式接口不支持，需要统计时用同步接口
- 🖼️ **图片大小** - 建议 < 10MB，Base64 < 5MB
- ⏱️ **响应时间** - 视频分析和思考模式耗时较长，建议异步处理
  （`@Async` 或 `CompletableFuture`）
- ⚠️ **错误处理** - 捕获 `AiException` 和 `IllegalArgumentException`
- 🔁 **重试机制** - 用 Spring Retry 或自定义重试逻辑，HTTP 层默认不重试
- 🗂️ **缓存优化** - 相同请求可加缓存，减少 API 调用

**模型选型参考**：代码生成 → DeepSeek；通用对话 → QWen / ChatGPT；图片分析 → QWenVL。

## 🔄 版本历史

**当前版本：1.1.0-beta3**（已发布至 Maven 中央仓库）

- 从代码层面修掉 `NoClassDefFoundError: ConnectionConfig` 根因，
  改用 5.1.x ~ 5.6.x 全区间通用的 API
- 已实测 httpclient5 5.1.4 / 5.2.3 / 5.5.1 / 5.6.4 四组运行时均正常
- 已实测 JDK 8 与 JDK 17 运行时均正常，Spring Boot 2.7.x / 3.x 均可使用
- 使用方**不再需要**手工补 httpclient5 / httpcore5 依赖

> ⚠️ **1.1.0、1.1.0-beta、1.1.0-beta2 在 Spring Boot 2.7.x 下运行期均会报
> `NoClassDefFoundError: ConnectionConfig`，请勿使用**，直接上 1.1.0-beta3。

完整更新日志见 📖 [CHANGELOG.md](CHANGELOG.md)。

## 📚 文档导航

| 文档 | 内容 |
|------|------|
| [CHANGELOG.md](CHANGELOG.md) | 版本更新日志 |
| [兼容性说明](docs/compatibility.md) | JDK / Spring Boot / HttpClient5 版本兼容、CVE 与自行升级 |
| [HTTP_CONFIG.md](HTTP_CONFIG.md) | HTTP 客户端配置详解（连接池、超时、调优） |
| [扩展自定义模型](docs/custom-model.md) | 三种扩展方式完整说明 |
| [升级到 1.1.0-beta3](docs/migration/upgrade-to-1.1.0.md) | 从旧版本迁移指南 |
| [Claude AI 集成示例](examples/claude-integration/README.md) | 自定义模型扩展完整示例 |
| [多模态视觉示例](examples/multimodal-vision/README.md) | 图片 / 视频分析 8 个场景 |

**版本说明文档**：
[v1.1.0-beta3](docs/versions/v1.1.0-beta3.md)（当前）|
[v1.1.0-beta2](docs/versions/v1.1.0-beta2.md)（已废弃）|
[v1.1.0-beta](docs/versions/v1.1.0-beta.md)（已废弃）|
[v1.1.0](docs/versions/v1.1.0.md) + [补丁方案](docs/versions/v1.1.0-hotfix.md) |
[v1.0.9](docs/versions/v1.0.9.md)

**官方 API 文档**：
[通义千问](https://help.aliyun.com/zh/dashscope/) |
[OpenAI](https://platform.openai.com/docs/api-reference) |
[DeepSeek](https://api-docs.deepseek.com/zh-cn/) |
[讯飞星火](https://www.xfyun.cn/doc/spark/) |
[豆包](https://www.volcengine.com/docs/82379/1494384)

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request。

## 👤 作者

**dongxueyong**

- Website: [jeesoul.com](http://jeesoul.com)
- Email: 3248838607@qq.com
- GitHub: [@jeesoul](https://github.com/jeesoul/jeesoul-ai-model)

## 📄 许可证

本项目采用 [MIT](https://opensource.org/licenses/MIT) 许可证。

---

⭐ 如果这个项目对你有帮助，欢迎 Star！

