# jeesoul-ai-model

[![Maven Central](https://img.shields.io/maven-central/v/com.jeesoul/jeesoul-ai-model)](https://search.maven.org/artifact/com.jeesoul/jeesoul-ai-model)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-8+-blue)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.17-brightgreen)](https://spring.io/projects/spring-boot)

一个基于 Spring Boot 的 AI 大模型服务集成框架，支持多种大模型服务的统一接入，包括通义千问、ChatGPT、讯飞星火、DeepSeek、豆包等。提供统一的API接口，支持同步/流式对话、思考模式、多模态输入、Token统计等功能。

## ✨ 核心特性

- 🎯 **统一接口** - 提供一致的 API，屏蔽不同模型的差异
- 🔌 **即插即用** - Spring Boot 自动配置，零代码集成
- 🌊 **流式支持** - 基于 WebFlux 的响应式流式对话
- 🧠 **思考模式** - 支持深度推理模式，获取模型思考过程
- 📊 **Token统计** - 完整的Token使用统计，支持成本分析
- 🎨 **多模态** - 支持文本、图片、视频等多种输入类型
- 🔧 **易扩展** - 支持动态注册自定义 AI 模型
- 📝 **完善文档** - 详细的参数说明和使用示例
- ✅ **参数校验** - 自动校验参数，提前发现错误

## 📦 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.jeesoul</groupId>
    <artifactId>jeesoul-ai-model</artifactId>
    <version>1.1.0-beta</version>
</dependency>

<!-- 流式对话支持 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

### 2. 配置参数

```yaml
ai:
  qwen:
    api-key: your-qwen-api-key
    temperature: 0.7      # 可选，采样温度，默认0.7
    top-p: 0.9           # 可选，核采样参数，默认0.9
    max-tokens: 2000     # 可选，最大token数，默认2000
    model: qwen-turbo    # 可选，默认模型名称
  chat-gpt:
    api-key: your-chatgpt-api-key
    temperature: 0.7
    top-p: 0.9
    max-tokens: 2000
    model: gpt-3.5-turbo
  spark:
    api-key: your-spark-api-key
    temperature: 0.7
    top-p: 0.9
    max-tokens: 2000
    model: spark-v3.5
  deep-seek:
    api-key: your-deepseek-api-key
    temperature: 0.7
    top-p: 0.9
    max-tokens: 2000
    model: deepseek-chat
  doubao:  # 豆包（字节跳动）
    api-key: your-doubao-api-key
    endpoint: https://ark.cn-beijing.volces.com/api/v3/chat/completions
    temperature: 0.7
    top-p: 0.9
    max-tokens: 2000
    model: doubao-seed-code-preview-251028
  qwen-vl:  # 多模态模型
    api-key: your-qwen-vl-api-key
    temperature: 0.7
    top-p: 0.9
    max-tokens: 2000
    model: qwen-vl-plus
```

> 💡 **提示**：`temperature`、`top-p`、`max-tokens`、`model` 为可选配置项，如果不配置会使用默认值。这些参数也可以在请求中动态指定，请求参数优先级高于配置。

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
Flux<String> stream = aiService.streamChatStr(request);
stream.subscribe(System.out::print);
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

**说明：**
- **思考模式**：模型输出推理过程，提升答案准确性
- **Token统计**：提供详细的Token使用统计（豆包流式接口不支持）
- **多模态**：支持图片、视频等非文本输入

## 📚 使用指南

### 基础对话
```java
AiService aiService = FactoryModelService.create("qWen");
ModelRequestVO request = new ModelRequestVO()
    .setModelName("qWen")
    .setModel("qwen-turbo")
    .setPrompt("解释一下什么是微服务");
ModelResponseVO response = aiService.httpChat(request);
```

### 思考模式（深度推理）
```java
request.setEnableThinking(true);  // 启用思考模式
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

// 完整信息流（包含Token统计）
aiService.streamChat(request).subscribe(chunk -> {
    System.out.print(chunk.getResult());
    if (chunk.getUsage() != null) {
        System.out.println("\nToken: " + chunk.getUsage().getTotalTokens());
    }
});
```

### Token统计
```java
ModelResponseVO response = aiService.httpChat(request);
TokenUsageVO usage = response.getUsage();
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

> 更多完整示例见下方 [完整示例](#-完整示例) 章节。

## 📁 完整示例

本项目提供了两个完整的集成示例，包含可直接运行的代码：

### 1. Claude AI 集成示例

展示如何集成 Anthropic Claude 模型，包含完整的 Service 实现和 REST API 示例。

**查看示例：** [`examples/claude-integration/README.md`](examples/claude-integration/README.md)

**包含内容：**
- ✅ 完整的 `ClaudeService` 实现（300+ 行）
- ✅ REST API 控制器（4个接口）
- ✅ 配置文件示例
- ✅ curl 测试命令

### 2. 多模态视觉示例

展示如何使用 QWen-VL 进行图片、视频分析，包含 8 个实际应用场景。

**查看示例：** [`examples/multimodal-vision/README.md`](examples/multimodal-vision/README.md)

**包含内容：**
- ✅ 8 个完整的 API 示例
- ✅ 图片分析、OCR、视频理解
- ✅ 思考模式、流式分析
- ✅ 最佳实践指南

---

## 🔧 扩展自定义模型

### 方式一：使用 @AiModelService 注解（推荐）

扩展自定义模型无需修改 `AiProperties.java`，符合开闭原则。

#### 1. 创建配置类（实现 ModelConfig 接口）

```java
import com.jeesoul.ai.model.config.BaseModelConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 自定义模型配置类
 * 继承 BaseModelConfig，自动实现 ModelConfig 接口
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "ai.myai")
public class MyModelProperties extends BaseModelConfig {
    // 可选：添加额外配置
    // private String region;
    // private String version;
}
```

#### 2. 创建 Service 类（完全复用现有实现）

```java
import com.jeesoul.ai.model.annotation.AiModelService;
import com.jeesoul.ai.model.config.ModelConfig;
import com.jeesoul.ai.model.service.SparkService;  // 复用 SparkService
import org.springframework.stereotype.Service;

/**
 * 自定义模型服务
 * 完全复用 SparkService 的实现，只需提供自己的配置
 */
@Service
@AiModelService(
    modelName = "myai",
    serviceName = "myService",
    description = "自定义模型（复用SparkService实现）"
)
public class MyService extends SparkService {
    
    /**
     * 构造函数（推荐使用）
     * 只需传入 ModelConfig，无需注入工具类
     */
    public MyService(MyModelProperties modelConfig) {
        super(modelConfig);  // 工具类已静态化，无需注入
    }
}
```

#### 3. 配置文件

```yaml
ai:
  myai:
    api-key: your-api-key
    endpoint: https://your-endpoint.com/v2/chat/completions
    temperature: 0.7
    top-p: 0.9
    max-tokens: 2000
    model: your-model-name
```

#### 4. 使用自定义模型

```java
// 直接使用，无需额外配置
AiService myService = FactoryModelService.create("myai");

ModelRequestVO request = new ModelRequestVO()
    .setModelName("myai")
    .setModel("your-model-name")
    .setPrompt("Hello!");

ModelResponseVO response = myService.httpChat(request);
```

**优势：**
- ✅ **零侵入**：无需修改框架核心代码
- ✅ **易扩展**：继承现有 Service 即可复用所有功能
- ✅ **独立配置**：每个模型有独立的配置类
- ✅ **自动注册**：使用 `@AiModelService` 注解自动注册

### 方式二：完全自定义实现

如果需要完全自定义实现，可以继承 `AbstractAiService`。详细示例请参考：📖 [Claude AI 集成示例](examples/claude-integration/README.md)

### 方式三：手动注册

```java
@Component
public class CustomModelRegistrar {
    @PostConstruct
    public void registerModels() {
        AiStrategyContext.registerModel("claude", "claudeService");
    }
}
```

## 📋 API 文档

### 核心接口

**AiService** - 统一的服务接口
- `httpChat()` - 同步对话
- `streamChat()` - 流式对话（完整信息）
- `streamChatStr()` - 流式对话（纯文本）
- `httpChatRaw()` / `streamChatRaw()` - 原始响应

**ModelRequestVO** - 请求参数
- `modelName` - 模型标识（必填）
- `model` - 模型版本（必填）
- `prompt` - 用户提示词
- `systemPrompt` - 系统提示词
- `enableThinking` - 思考模式开关
- `temperature` / `topP` / `maxTokens` - 模型参数
- `contents` - 多模态内容
- `messages` - 多轮对话消息

**ModelResponseVO** - 响应结果
- `result` - 文本内容
- `thinking` / `thinkingContent` - 思考模式信息
- `modelProvider` / `modelName` - 模型信息
- `usage` - Token 使用统计

## 💡 最佳实践

- ✅ 错误处理：使用 try-catch 捕获 `AiException` 和 `IllegalArgumentException`
- ✅ 重试机制：使用 Spring Retry 或自定义重试逻辑
- ✅ 异步处理：使用 `@Async` 或 `CompletableFuture` 处理长时间任务
- ✅ 缓存优化：对相同请求进行缓存，减少 API 调用
- ✅ 成本控制：监控 Token 使用，合理使用思考模式

## ⚙️ 配置说明

**配置优先级**：请求参数 > YML配置 > 代码默认值

### 基础配置

```yaml
ai:
  qwen:
    api-key: your-api-key
    endpoint: https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation
    temperature: 0.7      # 可选，默认0.7
    top-p: 0.9           # 可选，默认0.9
    max-tokens: 2000     # 可选，默认2000
    model: qwen-turbo    # 可选，默认模型
```

### HTTP 客户端配置（v1.1.0+）

从 1.1.0 版本开始，支持通过 YML 配置 HTTP 客户端参数（连接池、超时等）：

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
      socket: 10000               # 响应超时（毫秒），即等待服务器返回数据的时间，默认10000
      connection-request: 5000    # 从连接池获取连接的超时（毫秒），默认5000
    keep-alive:
      duration: 20000             # 连接保活时间（毫秒），默认20000
      enable-retry: false         # 是否启用自动重试，默认false（LLM场景不建议开启）
```

**常见场景配置**：

```yaml
# 高并发场景
ai:
  http:
    pool:
      max-total: 500
      max-per-route: 500
    timeout:
      socket: 30000

# 网络不稳定场景
ai:
  http:
    timeout:
      connect: 15000
      socket: 60000
```

> 💡 **提示**：所有参数均为可选，不配置时使用默认值。详细配置说明见 📖 [HTTP_CONFIG.md](HTTP_CONFIG.md)

## 🔍 检查模型状态

```java
// 检查模型是否已注册
if (AiStrategyContext.isModelRegistered("qWen")) {
    log.info("✅ QWen 模型可用");
}
```

## 🚀 技术栈

| 技术/框架 | 版本 | 说明 |
|---------|------|------|
| Java | 8+ | 项目主语言 |
| Spring Boot | 2.7.17 | 应用框架 |
| Spring WebFlux | 2.7.17 | 响应式编程 |
| Lombok | Latest | 简化代码 |
| Apache HttpClient | 5.5.1 | 同步 HTTP 客户端（内置封装） |
| SLF4J | 1.7.36 | 日志门面 |

## 📖 架构设计

### 核心模块

```
src/main/java/com/jeesoul/ai/model/
├── annotation/       # 注解定义
├── config/          # 配置类
├── constant/        # 常量和枚举
├── entity/          # 实体类
├── exception/       # 异常定义
├── factory/         # 工厂类
├── http/            # 内置 HTTP 封装（基于 Apache HttpClient 5.x）
├── request/         # 请求对象
├── response/        # 响应对象
├── service/         # 服务实现
├── strategy/        # 策略模式
├── util/            # 工具类
└── vo/              # 视图对象
```

### 设计模式

- **工厂模式** - `FactoryModelService` 统一创建服务实例
- **策略模式** - `AiStrategyContext` 动态选择具体实现
- **模板方法模式** - `AbstractAiService` 定义通用流程
- **建造者模式** - `ModelRequestVO` 支持链式调用

## 🔄 版本历史

**当前版本：1.1.0-beta**

> ⚠️ **重要**：1.1.0 版本 pom 依赖声明有缺陷，新接入请使用 1.1.0-beta。已在使用 1.1.0 的项目无需换版本，按 [1.1.0 补丁方案](docs/versions/v1.1.0-hotfix.md) 在自身 pom 补三个依赖即可（已实测验证）。

### v1.1.0-beta 紧急修复（当前版本）
- 🔥 修复 1.1.0 版本的 HttpClient5 依赖缺失问题
- 🔥 显式声明所有 HttpClient5 依赖，确保通用组件的独立性
- 🔥 解决运行时 `ClassNotFoundException` 错误

### v1.1.0 主要更新 ⚠️ 需打补丁
- ✅ 彻底移除 Hutool 依赖，自研 HTTP 封装和 Spring 工具类
- ✅ 新增 HTTP 客户端配置支持（连接池、超时、保活参数可通过 YML 配置）
- ✅ 完全向后兼容，对外 API 零变化
- ⚠️ **已知问题**：pom 未传递 httpcore5 且 httpclient5 版本线不匹配，按 [补丁方案](docs/versions/v1.1.0-hotfix.md) 补三个依赖后可正常使用

详细更新日志：📖 [CHANGELOG.md](CHANGELOG.md)

### 历史版本文档
- [v1.1.0-beta 详细说明](docs/versions/v1.1.0-beta.md)
- [v1.1.0 补丁方案](docs/versions/v1.1.0-hotfix.md)
- [v1.1.0 详细说明](docs/versions/v1.1.0.md)
- [v1.0.9 详细说明](docs/versions/v1.0.9.md)
- [升级到 1.1.0 指南](docs/migration/upgrade-to-1.1.0.md)

## ⚠️ 注意事项

- 🔐 **API密钥安全** - 使用环境变量或配置中心，不要硬编码
- 💰 **成本控制** - 思考模式消耗更多Token，合理使用
- 📊 **Token统计** - 豆包流式接口不支持Token统计，使用同步接口
- 🖼️ **图片大小** - 建议 < 10MB，Base64 < 5MB
- ⏱️ **响应时间** - 视频分析和思考模式建议异步处理

## ❓ 常见问题

- **配置项**：使用中划线 `api-key` 而非 `apiKey`
- **流式调用**：必须调用 `subscribe()`
- **模型选择**：代码生成→DeepSeek，通用对话→QWen/ChatGPT，图片分析→QWenVL
- **思考模式**：`thinking`(Boolean) 标识是否思考，`thinkingContent`(String) 存储思考内容
- **Token统计**：豆包流式接口不支持，使用同步接口

## 📚 文档导航

### 核心文档
- 📖 [快速开始](#-快速开始) - 5分钟上手
- 📖 [使用指南](#-使用指南) - 基础对话、思考模式、流式对话
- 📖 [扩展自定义模型](#-扩展自定义模型) - 零侵入扩展

### 版本文档
- 📖 [CHANGELOG.md](CHANGELOG.md) - 版本更新日志
- 📖 [v1.1.0 详细说明](docs/versions/v1.1.0.md)
- 📖 [v1.0.9 详细说明](docs/versions/v1.0.9.md)
- 📖 [升级到 1.1.0 指南](docs/migration/upgrade-to-1.1.0.md)

### 配置文档
- 📖 [HTTP_CONFIG.md](HTTP_CONFIG.md) - HTTP 客户端配置详解（v1.1.0+）

### 示例文档
- 📖 [Claude AI 集成示例](examples/claude-integration/README.md) - 自定义模型扩展完整示例
- 📖 [多模态视觉示例](examples/multimodal-vision/README.md) - 图片/视频分析

### 官方 API 文档
- [通义千问](https://help.aliyun.com/zh/dashscope/) | [OpenAI](https://platform.openai.com/docs/api-reference) | [DeepSeek](https://api-docs.deepseek.com/zh-cn/) | [讯飞星火](https://www.xfyun.cn/doc/spark/) | [豆包](https://www.volcengine.com/docs/82379/1494384)

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 👤 作者

**dongxueyong**

- Website: [jeesoul.com](http://jeesoul.com)
- Email: 3248838607@qq.com
- GitHub: [@jeesoul](https://github.com/jeesoul/jeesoul-ai-model)

## 📄 许可证

本项目采用 [MIT](https://opensource.org/licenses/MIT) 许可证。

---

⭐ 如果这个项目对你有帮助，欢迎 Star！
