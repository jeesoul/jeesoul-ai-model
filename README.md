# jeesoul-ai-model

[![Maven Central](https://img.shields.io/maven-central/v/com.jeesoul/jeesoul-ai-model)](https://search.maven.org/artifact/com.jeesoul/jeesoul-ai-model)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-8+-blue)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.17-brightgreen)](https://spring.io/projects/spring-boot)

一个基于 Spring Boot 的 AI 大模型服务集成框架，支持多种大模型服务的统一接入，包括通义千问、ChatGPT、讯飞星火、DeepSeek 等。

## ✨ 核心特性

- 🎯 **统一接口** - 提供一致的 API，屏蔽不同模型的差异
- 🔌 **即插即用** - Spring Boot 自动配置，零代码集成
- 🌊 **流式支持** - 基于 WebFlux 的响应式流式对话
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
    <version>1.0.8</version>
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
  chat-gpt:
    api-key: your-chatgpt-api-key
  spark:
    api-key: your-spark-api-key
  deep-seek:
    api-key: your-deepseek-api-key
  qwen-vl:  # 多模态模型
    api-key: your-qwen-vl-api-key
```

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

| 模型名称 | 模型标识 | 特性 | 支持功能 |
|---------|---------|------|---------|
| 通义千问 | `qWen` | 阿里云大模型 | 思考模式、System Prompt、多轮对话 |
| ChatGPT | `chatgpt` | OpenAI 大模型 | System Prompt、多轮对话 |
| 讯飞星火 | `spark` | 科大讯飞大模型 | System Prompt、多轮对话 |
| DeepSeek | `deepSeek` | 深度求索大模型 | System Prompt、多轮对话 |
| 千问视觉 | `qwenVL` | 多模态模型 | 图片、视频分析、System Prompt、多轮对话 |

## 📚 使用指南

### 基础对话

```java
ModelRequestVO request = new ModelRequestVO()
    .setModelName("qWen")
    .setModel("qwen-turbo")
    .setPrompt("解释一下什么是微服务")
    .setTemperature(0.7)
    .setMaxTokens(2000);

AiService aiService = FactoryModelService.create(request.getModelName());
ModelResponseVO response = aiService.httpChat(request);
```

### 系统提示词

```java
// 所有模型都支持 systemPrompt
ModelRequestVO request = new ModelRequestVO()
    .setModelName("qWen")
    .setModel("qwen-turbo")
    .setSystemPrompt("你是一个专业的Java工程师")
    .setPrompt("如何优化Spring Boot性能？");
```

### 思考模式

```java
// QWen 支持思考模式
ModelRequestVO request = new ModelRequestVO()
    .setModelName("qWen")
    .setModel("qwen-turbo")
    .setEnableThinking(true)
    .setPrompt("计算 123 * 456");

ModelResponseVO response = aiService.httpChat(request);
System.out.println("答案: " + response.getResult());
System.out.println("思考过程: " + response.getThinking());
```

### 流式对话

```java
// 方式1: 获取完整响应对象
Flux<ModelResponseVO> responseFlux = aiService.streamChat(request);
responseFlux.subscribe(response -> {
    System.out.print(response.getResult());
    if (response.getThinking() != null) {
        System.out.println("\n思考: " + response.getThinking());
    }
});

// 方式2: 只获取文本内容（推荐）
Flux<String> textFlux = aiService.streamChatStr(request);
textFlux.subscribe(System.out::print);

// 方式3: 获取原始响应数据（JSON字符串，用于调试或自定义处理）
Flux<String> rawFlux = aiService.streamChatRaw(request);
rawFlux.subscribe(json -> {
    System.out.println("原始响应: " + json);
    // 可以自定义解析和处理
});
```

### 聊天上下文（多轮对话）

```java
// 使用 messages 列表支持多轮对话
List<ModelRequestVO.Message> messages = Arrays.asList(
    new ModelRequestVO.Message()
        .setRole("user")
        .setContent("你好"),
    new ModelRequestVO.Message()
        .setRole("assistant")
        .setContent("你好！有什么可以帮助你的吗？"),
    new ModelRequestVO.Message()
        .setRole("user")
        .setContent("请介绍一下Java")
);

ModelRequestVO request = new ModelRequestVO()
    .setModelName("qWen")
    .setModel("qwen-turbo")
    .setMessages(messages);

// 注意：如果设置了 messages，prompt 字段会被忽略
// systemPrompt 会自动添加到消息列表的最前面
```

### 参数透传

```java
// 传递模型特定的参数
Map<String, Object> params = new HashMap<>();
params.put("presence_penalty", 0.5);
params.put("frequency_penalty", 0.3);

ModelRequestVO request = new ModelRequestVO()
    .setModelName("chatgpt")
    .setModel("gpt-3.5-turbo")
    .setPrompt("写一首诗")
    .setParams(params);
```

### 获取原始响应数据

```java
// 同步获取原始响应（JSON字符串）
String rawResponse = aiService.httpChatRaw(request);
System.out.println("原始响应: " + rawResponse);
// 可以自定义解析和处理，获取完整的响应信息

// 流式获取原始响应（JSON字符串流）
Flux<String> rawStream = aiService.streamChatRaw(request);
rawStream.subscribe(json -> {
    System.out.println("原始响应片段: " + json);
    // 每个片段都是完整的JSON对象
});
```

## 🎨 多模态使用

### 图片分析

```java
AiService qwenVL = FactoryModelService.create("qwenVL");

ModelRequestVO request = new ModelRequestVO()
    .setModelName("qwenVL")
    .setModel("qwen-vl-plus")
    .setContents(Arrays.asList(
        MessageContent.imageUrl("https://example.com/image.jpg"),
        MessageContent.text("这张图片里有什么？")
    ));

ModelResponseVO response = qwenVL.httpChat(request);
```

### 多张图片对比

```java
ModelRequestVO request = new ModelRequestVO()
    .setModelName("qwenVL")
    .setModel("qwen-vl-plus")
    .setContents(Arrays.asList(
        MessageContent.text("比较这两张图片的区别："),
        MessageContent.imageUrl("https://example.com/image1.jpg"),
        MessageContent.imageUrl("https://example.com/image2.jpg")
    ));
```

### 视频分析

```java
ModelRequestVO request = new ModelRequestVO()
    .setModelName("qwenVL")
    .setModel("qwen3-vl-plus")
    .setContents(Arrays.asList(
        MessageContent.videoUrl("https://example.com/video.mp4"),
        MessageContent.text("总结视频的主要内容")
    ));
```

### 支持的内容类型

| 方法 | 说明 | 示例 |
|------|------|------|
| `MessageContent.text()` | 文本内容 | `text("你好")` |
| `MessageContent.imageUrl()` | 图片URL | `imageUrl("https://...")` |
| `MessageContent.imageUrl(url, detail)` | 图片URL(指定精度) | `imageUrl("https://...", ImageDetail.HIGH)` |
| `MessageContent.imageBase64()` | Base64图片 | `imageBase64("data:image/...")` |
| `MessageContent.videoUrl()` | 视频URL | `videoUrl("https://...")` |
| `MessageContent.audioUrl()` | 音频URL | `audioUrl("https://...")` |
| `MessageContent.fileUrl()` | 文件URL | `fileUrl("https://...")` |

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

```java
@Slf4j
@AiModelService(
    modelName = "claude",
    serviceName = "claudeService",
    description = "Anthropic Claude AI 模型"
)
public class ClaudeService extends AbstractAiService {
    
    public ClaudeService(AiProperties aiProperties, 
                        HttpUtils aiHttpUtils, 
                        StreamHttpUtils streamHttpUtils) {
        super(aiProperties, aiHttpUtils, streamHttpUtils);
    }

    @Override
    protected String getModelName() {
        return "claude";
    }

    @Override
    protected boolean supportSystemPrompt() {
        return true;
    }

    @Override
    protected boolean supportThinking() {
        return false;
    }

    @Override
    public ModelResponseVO httpChat(ModelRequestVO request) throws AiException {
        validateRequest(request);
        warnUnsupportedFeatures(request);
        
        // 实现你的调用逻辑
        // ...
    }

    @Override
    public Flux<ModelResponseVO> streamChat(ModelRequestVO request) {
        // 实现流式对话
        // ...
    }

    @Override
    public Flux<String> streamChatStr(ModelRequestVO request) {
        // 实现流式文本
        // ...
    }
}
```

### 方式二：手动注册

```java
@Component
public class CustomModelRegistrar {
    
    @PostConstruct
    public void registerModels() {
        AiStrategyContext.registerModel("claude", "claudeService");
        AiStrategyContext.registerModel("gemini", "geminiService");
        log.info("自定义模型注册完成");
    }
}
```

### 使用自定义模型

```java
// 直接使用模型名称创建服务
AiService claudeService = FactoryModelService.create("claude");

ModelRequestVO request = new ModelRequestVO()
    .setModelName("claude")
    .setModel("claude-3-opus-20240229")
    .setPrompt("Hello Claude!");

ModelResponseVO response = claudeService.httpChat(request);
```

## 📋 参数说明

### ModelRequestVO

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `modelName` | String | 是 | 模型名称(qWen/chatgpt/spark/deepSeek/qwenVL) |
| `model` | String | 是 | 具体模型版本(如 qwen-turbo, gpt-3.5-turbo) |
| `prompt` | String | 是* | 用户提示词(*多模态时可选，如果设置了messages则会被忽略) |
| `contents` | List<MessageContent> | 否 | 多模态内容列表(优先级高于prompt) |
| `messages` | List<Message> | 否 | 消息列表(用于多轮对话，优先级高于prompt) |
| `systemPrompt` | String | 否 | 系统提示词(所有模型都支持，会自动添加到消息列表最前面) |
| `enableThinking` | Boolean | 否 | 是否开启思考模式(仅QWen支持) |
| `temperature` | Double | 否 | 采样温度(0-2，默认0.7，可在yml中配置) |
| `topP` | Double | 否 | 核采样参数(0-1，默认0.9，可在yml中配置) |
| `maxTokens` | Integer | 否 | 最大生成Token数(默认2000，可在yml中配置) |
| `params` | Map | 否 | 自定义参数(模型特定参数) |

**Message 对象结构：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `role` | String | 角色(user/assistant/system) |
| `content` | String | 消息内容 |

### ModelResponseVO

| 字段 | 类型 | 说明 |
|------|------|------|
| `result` | String | 模型返回的文本内容 |
| `thinking` | String | 思考过程(如果启用) |
| `model` | String | 使用的模型名称 |

### AiService 接口方法

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `httpChat(ModelRequestVO)` | `ModelResponseVO` | 同步对话，返回解析后的响应对象 |
| `httpChatRaw(ModelRequestVO)` | `String` | 同步对话，返回原始JSON响应字符串 |
| `streamChat(ModelRequestVO)` | `Flux<ModelResponseVO>` | 流式对话，返回响应对象流 |
| `streamChatStr(ModelRequestVO)` | `Flux<String>` | 流式对话，返回文本内容流 |
| `streamChatRaw(ModelRequestVO)` | `Flux<String>` | 流式对话，返回原始JSON响应字符串流 |

## 💡 最佳实践

### 错误处理

```java
try {
    ModelResponseVO response = aiService.httpChat(request);
    return response.getResult();
} catch (IllegalArgumentException e) {
    // 参数错误
    log.error("参数校验失败: {}", e.getMessage());
    return "参数错误，请检查输入";
} catch (AiException e) {
    // AI服务调用失败
    log.error("AI调用失败: {}", e.getMessage(), e);
    return "服务暂时不可用，请稍后重试";
}
```

### 重试机制

```java
@Retryable(
    value = {AiException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2)
)
public ModelResponseVO chatWithRetry(ModelRequestVO request) {
    return aiService.httpChat(request);
}
```

### 异步处理

```java
@Async
public CompletableFuture<String> chatAsync(String prompt) {
    ModelRequestVO request = new ModelRequestVO()
        .setModelName("qWen")
        .setModel("qwen-turbo")
        .setPrompt(prompt);
    
    ModelResponseVO response = aiService.httpChat(request);
    return CompletableFuture.completedFuture(response.getResult());
}
```

### 缓存优化

```java
@Cacheable(value = "ai-responses", key = "#request.prompt")
public ModelResponseVO chatWithCache(ModelRequestVO request) {
    return aiService.httpChat(request);
}
```

## ⚙️ 配置选项

### 完整配置示例

```yaml
ai:
  qwen:
    api-key: sk-xxx
    endpoint: https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation
    temperature: 0.7      # 采样温度，默认0.7
    top-p: 0.9           # 核采样参数，默认0.9
    max-tokens: 2000     # 最大token数，默认2000
    model: qwen-turbo    # 默认模型名称
  
  chat-gpt:
    api-key: sk-xxx
    endpoint: https://api.openai.com/v1/chat/completions
    temperature: 0.7
    top-p: 0.9
    max-tokens: 2000
    model: gpt-3.5-turbo
  
  spark:
    api-key: xxx
    endpoint: https://spark-api-open.xf-yun.com/v2/chat/completions
    temperature: 0.7
    top-p: 0.9
    max-tokens: 2000
    model: spark-v3.5
  
  deep-seek:
    api-key: sk-xxx
    endpoint: https://api.deepseek.com/v1/chat/completions
    temperature: 0.7
    top-p: 0.9
    max-tokens: 2000
    model: deepseek-chat
  
  qwen-vl:
    api-key: sk-xxx
    endpoint: https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation
    temperature: 0.7
    top-p: 0.9
    max-tokens: 2000
    model: qwen-vl-plus

# 日志配置
logging:
  level:
    com.jeesoul.ai.model: DEBUG  # 开发环境，会打印请求参数
    # com.jeesoul.ai.model: INFO  # 生产环境
```

### 配置说明

- **参数优先级**：请求参数 > YML配置 > 代码默认值
- **temperature/topP/maxTokens**：可以在YML中配置默认值，也可以在请求中覆盖
- **model**：可以在YML中配置默认模型，也可以在请求中指定其他模型
- **调试模式**：设置日志级别为DEBUG时，会自动打印发送给模型的原始请求参数

## 🔍 检查模型状态

```java
@Component
public class ModelHealthCheck {
    
    @PostConstruct
    public void checkModels() {
        if (AiStrategyContext.isModelRegistered("qWen")) {
            log.info("✅ QWen 模型可用");
        }
        
        if (AiStrategyContext.isModelRegistered("claude")) {
            log.info("✅ Claude 自定义模型已注册");
        } else {
            log.warn("❌ Claude 模型未注册");
        }
    }
}
```

## 🚀 技术栈

| 技术/框架 | 版本 | 说明 |
|---------|------|------|
| Java | 8+ | 项目主语言 |
| Spring Boot | 2.7.17 | 应用框架 |
| Spring WebFlux | 2.7.17 | 响应式编程 |
| Lombok | Latest | 简化代码 |
| Hutool | 5.8.25 | 工具类库 |
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

## ⚠️ 注意事项

1. **API密钥安全** - 不要在代码中硬编码密钥，使用环境变量或配置中心
2. **成本控制** - 多模态和高分辨率分析成本较高，合理使用
3. **图片大小** - 建议图片 < 10MB，Base64编码 < 5MB
4. **响应时间** - 视频分析可能需要10-60秒，建议异步处理
5. **参数校验** - 框架会自动校验参数，确保合法性

## 🐛 故障排查

### 配置不生效

检查配置项名称：
```yaml
# ✅ 正确
ai:
  qwen:
    api-key: xxx

# ❌ 错误
ai:
  qwen:
    apiKey: xxx  # Spring Boot 2.x 推荐使用中划线
```

### 流式对话无响应

确保订阅了Flux：
```java
Flux<String> stream = aiService.streamChatStr(request);
stream.subscribe(System.out::print);  // ← 必须调用 subscribe()
```

### 模型不支持某功能

查看日志警告：
```
WARN: [QWen] 当前模型不支持 systemPrompt，该参数将被忽略
```

## 📝 更新日志

### v1.0.8 (最新版本)

**✨ 新增功能**
- 新增 `httpChatRaw()` 方法，支持获取原始HTTP响应数据（JSON字符串）
- 新增 `streamChatRaw()` 方法，支持获取原始流式响应数据（JSON字符串流）
- 新增 `messages` 字段支持，实现多轮对话上下文
- 新增配置参数支持：`temperature`、`topP`、`maxTokens`、`model` 可在YML中配置默认值
- 新增调试日志功能，DEBUG模式下自动打印请求参数

**🎉 重大改进**
- 所有模型统一支持 `systemPrompt` 参数（之前仅ChatGPT和Spark支持）
- `systemPrompt` 自动添加到消息列表最前面，确保优先级
- 参数优先级优化：请求参数 > YML配置 > 代码默认值
- 简化参数获取逻辑，提升代码可读性和性能

**🐛 缺陷修复**
- 修复流式原始响应返回格式问题（之前返回Map.toString()，现在返回JSON字符串）
- 修复 `postStreamText` 方法的响应格式问题

### v1.0.7

**✨ 新增功能**
- 新增多模态支持（图片、视频分析）
- 新增 `@AiModelService` 注解，支持动态注册模型
- 新增参数自动校验功能

**🎉 重大改进**
- 重构代码，消除重复，提升可维护性
- 统一流式返回格式
- 优化日志输出，避免敏感信息泄露

**🐛 缺陷修复**
- 修复流式响应格式不一致问题
- 修复参数透传Bug

### v1.0.6

- 支持通义千问、ChatGPT、讯飞星火、DeepSeek
- 支持同步和流式对话
- 基础参数透传功能

## 📚 学习资源

### 官方文档
- [快速开始](#-快速开始)
- [使用指南](#-使用指南)
- [多模态使用](#-多模态使用)
- [扩展自定义模型](#-扩展自定义模型)

### 完整示例
- [Claude AI 集成示例](examples/claude-integration/README.md) - 自定义模型集成
- [多模态视觉示例](examples/multimodal-vision/README.md) - 图片/视频分析

### 参考资料
- [通义千问官方文档](https://help.aliyun.com/zh/dashscope/)
- [OpenAI API 文档](https://platform.openai.com/docs/api-reference)
- [Claude API 文档](https://docs.anthropic.com/claude/reference/getting-started-with-the-api)

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
