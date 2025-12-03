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
    <version>1.0.9</version>
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

### 思考模式（深度推理）

支持思考模式的模型：QWen、Spark、DeepSeek、DouBao

```java
// 启用思考模式
ModelRequestVO request = new ModelRequestVO()
    .setModelName("spark")
    .setModel("x1")
    .setEnableThinking(true)  // 启用思考模式
    .setPrompt("9.11和9.8哪个更大？");

ModelResponseVO response = aiService.httpChat(request);
System.out.println("是否使用思考: " + response.getThinking());  // true
System.out.println("思考过程: " + response.getThinkingContent());  // 模型的推理过程
System.out.println("最终答案: " + response.getResult());  // 最终答案

// 禁用思考模式
request.setEnableThinking(false);  // 显式禁用
ModelResponseVO response2 = aiService.httpChat(request);
System.out.println("是否使用思考: " + response2.getThinking());  // false
```

**流式思考模式：**

```java
// streamChat() - 获取完整信息
aiService.streamChat(request).subscribe(chunk -> {
    if (chunk.getThinking()) {
        // 思考过程的chunk
        System.out.print("[思考] " + chunk.getResult());
    } else {
        // 最终答案的chunk
        System.out.print(chunk.getResult());
    }
});

// streamChatStr() - 获取纯文本（思考+答案）
aiService.streamChatStr(request).subscribe(System.out::print);
```

### 流式对话

```java
// 方式1: 获取完整响应对象（包含Token统计、模型信息等）
Flux<ModelResponseVO> responseFlux = aiService.streamChat(request);
responseFlux.subscribe(chunk -> {
    System.out.print(chunk.getResult());  // 输出文本内容
    
    // 获取Token统计（最后一个chunk包含）
    if (chunk.getUsage() != null) {
        System.out.println("\nToken使用: " + chunk.getUsage());
    }
    
    // 获取模型信息
    System.out.println("模型: " + chunk.getModelName());
});

// 方式2: 只获取文本内容（推荐，最简单）
Flux<String> textFlux = aiService.streamChatStr(request);
textFlux.subscribe(System.out::print);

// 方式3: 获取原始响应数据（JSON字符串，用于调试或自定义处理）
Flux<String> rawFlux = aiService.streamChatRaw(request);
rawFlux.subscribe(json -> {
    System.out.println("原始响应: " + json);
    // 可以自定义解析和处理
});
```

### Token统计与成本分析

所有模型都支持Token使用统计，方便进行成本分析和优化：

```java
// 同步调用 - 获取Token统计
ModelResponseVO response = aiService.httpChat(request);
TokenUsageVO usage = response.getUsage();
System.out.println("输入Token: " + usage.getPromptTokens());
System.out.println("输出Token: " + usage.getCompletionTokens());
System.out.println("总Token: " + usage.getTotalTokens());
System.out.println("实际模型: " + response.getModelName());

// 流式调用 - 在最后一个chunk获取Token统计
aiService.streamChat(request).subscribe(chunk -> {
    System.out.print(chunk.getResult());
    
    // 最后一个chunk包含Token统计
    if (chunk.getUsage() != null) {
        System.out.println("\n总计使用: " + chunk.getUsage().getTotalTokens() + " tokens");
    }
});
```

**注意事项：**
- ✅ 所有模型的同步接口都支持Token统计
- ✅ QWen、Spark、DeepSeek、ChatGPT的流式接口支持Token统计（在最后一个chunk）
- ⚠️ DouBao的流式接口不提供Token统计（API限制），如需统计请使用同步接口

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

### 思考模式详细说明

**支持的模型：**
- ✅ QWen (通义千问)
- ✅ Spark (讯飞星火)
- ✅ DeepSeek (深度求索)
- ✅ DouBao (豆包)
- ❌ ChatGPT (不支持)

**启用方式：**

```java
// 方式1：在请求中启用
ModelRequestVO request = new ModelRequestVO()
    .setModelName("deepSeek")
    .setModel("deepseek-chat")
    .setEnableThinking(true)  // 启用思考模式
    .setPrompt("解释量子纠缠");

// 方式2：显式禁用（某些模型默认启用）
request.setEnableThinking(false);  // 禁用思考模式
```

**获取思考内容：**

```java
// 同步调用
ModelResponseVO response = aiService.httpChat(request);
if (response.getThinking()) {
    System.out.println("=== 思考过程 ===");
    System.out.println(response.getThinkingContent());
    System.out.println("\n=== 最终答案 ===");
    System.out.println(response.getResult());
}

// 流式调用 - 区分思考和答案
aiService.streamChat(request).subscribe(chunk -> {
    if (chunk.getThinking()) {
        System.out.print("[思考] " + chunk.getResult());
    } else {
        System.out.print(chunk.getResult());
    }
});

// 流式调用 - 完整输出（思考+答案）
aiService.streamChatStr(request).subscribe(System.out::print);
```

**各模型的思考配置：**

| 模型 | 配置方式 | 默认值 | 支持模式 |
|------|---------|--------|---------|
| QWen | `enable_thinking: true/false` | false | enabled/disabled |
| Spark | `thinking: {type: "enabled"}` | enabled | enabled/disabled/auto |
| DeepSeek | `thinking: {type: "enabled"}` | disabled | enabled/disabled |
| DouBao | `thinking: {type: "enabled"}` | enabled | enabled/disabled/auto |

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

### ModelRequestVO（请求参数）

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `modelName` | String | 是 | 模型名称(qWen/chatgpt/spark/deepSeek/douBao/qwenVL) |
| `model` | String | 是 | 具体模型版本(如 qwen-turbo, gpt-3.5-turbo) |
| `prompt` | String | 是* | 用户提示词(*多模态时可选，如果设置了messages则会被忽略) |
| `contents` | List<MessageContent> | 否 | 多模态内容列表(优先级高于prompt) |
| `messages` | List<Message> | 否 | 消息列表(用于多轮对话，优先级高于prompt) |
| `systemPrompt` | String | 否 | 系统提示词(所有模型都支持，会自动添加到消息列表最前面) |
| `enableThinking` | Boolean | 否 | 是否开启思考模式(QWen/Spark/DeepSeek/DouBao支持) |
| `temperature` | Double | 否 | 采样温度(0-2，默认0.7，可在yml中配置) |
| `topP` | Double | 否 | 核采样参数(0-1，请求中默认null，yml中默认0.9) |
| `maxTokens` | Integer | 否 | 最大生成Token数(请求中默认null，yml中默认2000) |
| `params` | Map | 否 | 自定义参数(模型特定参数) |

**Message 对象结构：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `role` | String | 角色(user/assistant/system) |
| `content` | String | 消息内容 |

### ModelResponseVO（响应结果）

| 字段 | 类型 | 说明 |
|------|------|------|
| `result` | String | 模型返回的文本内容 |
| `thinking` | Boolean | 是否使用了思考模式(true/false) |
| `thinkingContent` | String | 思考过程内容(仅启用思考模式时有值) |
| `modelProvider` | String | 模型提供商(如 qWen, spark, deepSeek) |
| `modelName` | String | 实际使用的模型版本(如 qwen-turbo, x1) |
| `usage` | TokenUsageVO | Token使用统计 |

**TokenUsageVO 对象结构：**
| 字段 | 类型 | 说明 |
|------|------|------|
| `promptTokens` | Integer | 输入Token数 |
| `completionTokens` | Integer | 输出Token数 |
| `totalTokens` | Integer | 总Token数 |
| `reasoningTokens` | Integer | 推理Token数(DouBao思考模式) |
| `cachedTokens` | Integer | 缓存Token数(DouBao) |

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
  
  doubao:
    api-key: xxx
    endpoint: https://ark.cn-beijing.volces.com/api/v3/chat/completions
    temperature: 0.7
    top-p: 0.9
    max-tokens: 2000
    model: doubao-seed-code-preview-251028
  
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

## 🔄 从v1.0.8升级到v1.0.9

### 向后兼容性

v1.0.9 **完全向后兼容** v1.0.8，现有代码无需修改即可使用。

### 新增字段说明

**ModelResponseVO 新增字段：**
```java
// 旧版本（v1.0.8）
response.getResult();   // ✅ 仍然可用
response.getModel();    // ✅ 仍然可用（已标记@Deprecated）

// 新版本（v1.0.9）推荐使用
response.getResult();           // 文本内容
response.getThinking();         // 是否思考（Boolean）
response.getThinkingContent();  // 思考内容（String）
response.getModelProvider();    // 模型提供商
response.getModelName();        // 具体模型版本
response.getUsage();            // Token统计
```

### 迁移建议

**1. 更新依赖版本**
```xml
<dependency>
    <groupId>com.jeesoul</groupId>
    <artifactId>jeesoul-ai-model</artifactId>
    <version>1.0.9</version>
</dependency>
```

**2. 使用新字段（可选）**
```java
// 旧代码（仍然可用）
String result = response.getResult();
String model = response.getModel();

// 新代码（推荐）
String result = response.getResult();
String modelProvider = response.getModelProvider();
String modelVersion = response.getModelName();
TokenUsageVO usage = response.getUsage();
```

**3. 思考模式使用（新功能）**
```java
// 启用思考模式
request.setEnableThinking(true);

// 获取思考内容
if (response.getThinking()) {
    System.out.println("思考: " + response.getThinkingContent());
}
```

## ⚠️ 注意事项

1. **API密钥安全** - 不要在代码中硬编码密钥，使用环境变量或配置中心
2. **成本控制** - 思考模式会消耗更多Token，合理使用
3. **Token统计** - 豆包的流式接口不提供Token统计，如需统计请使用同步接口
4. **图片大小** - 建议图片 < 10MB，Base64编码 < 5MB
5. **响应时间** - 视频分析和思考模式可能需要更长时间，建议异步处理
6. **参数校验** - 框架会自动校验参数，确保合法性

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
WARN: [ChatGPT] 当前模型不支持 enableThinking，该参数将被忽略
```

### 思考模式不生效

确保：
1. 模型支持思考模式（QWen/Spark/DeepSeek/DouBao）
2. 显式设置 `setEnableThinking(true)` 或 `setEnableThinking(false)`
3. 检查日志中的请求参数，确认thinking参数已发送

### 流式输出空白行

已在v1.0.9中修复，如果仍有问题：
1. 确保使用最新版本
2. 检查是否正确过滤了空chunk

## ❓ 常见问题

### Q1: 如何选择合适的模型？

**根据场景选择：**
- **代码生成/技术问答** → DeepSeek (性价比高)
- **通用对话** → QWen、ChatGPT
- **需要深度推理** → Spark X1、DeepSeek (思考模式)
- **图片/视频分析** → QWenVL
- **中文优化** → QWen、DouBao、Spark

### Q2: thinking字段和thinkingContent字段的区别？

- `thinking` (Boolean) - 标识是否使用了思考模式
- `thinkingContent` (String) - 存储完整的思考过程文本

```java
if (response.getThinking()) {  // 判断是否思考
    System.out.println(response.getThinkingContent());  // 获取思考内容
}
```

### Q3: 为什么豆包的流式接口没有Token统计？

这是豆包API的限制，流式响应中不包含usage字段。如需Token统计，请使用同步接口 `httpChat()`。

### Q4: 如何处理思考模式的流式输出？

```java
// 方式1：区分思考和答案
aiService.streamChat(request).subscribe(chunk -> {
    if (chunk.getThinking()) {
        System.out.print("[思考] ");
    }
    System.out.print(chunk.getResult());
});

// 方式2：完整输出（推荐）
aiService.streamChatStr(request).subscribe(System.out::print);
```

### Q5: modelProvider和modelName的区别？

- `modelProvider` - 模型提供商（如 qWen, spark, deepSeek）
- `modelName` - 实际使用的模型版本（如 qwen-turbo, x1, deepseek-chat）

这两个字段方便业务方进行模型追踪和成本分析。

### Q6: 如何实现流式输出到前端？

```java
@RestController
public class ChatController {
    
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestParam String prompt) {
        ModelRequestVO request = new ModelRequestVO()
            .setModelName("qWen")
            .setModel("qwen-turbo")
            .setPrompt(prompt);
        
        AiService aiService = FactoryModelService.create("qWen");
        return aiService.streamChatStr(request);
    }
}
```

前端使用EventSource接收：
```javascript
const eventSource = new EventSource('/chat/stream?prompt=你好');
eventSource.onmessage = (event) => {
    console.log(event.data);
};
```

## 📝 更新日志

### v1.0.9 (最新版本)

**✨ 新增功能**
- 🎉 **新增豆包(DouBao)模型支持** - 集成字节跳动豆包大模型
- 📊 **Token统计功能** - 所有模型返回详细的Token使用统计（promptTokens、completionTokens、totalTokens）
- 🧠 **完整的思考模式支持** - QWen、Spark、DeepSeek、DouBao均支持深度推理模式
- 🏷️ **模型版本信息** - 响应中包含 `modelProvider` 和 `modelName` 字段，方便追踪实际使用的模型

**🎉 重大改进**
- **响应对象重构** - `ModelResponseVO` 新增多个字段：
  - `thinking` (Boolean) - 标识是否使用思考模式
  - `thinkingContent` (String) - 存储完整的思考过程内容
  - `modelProvider` (String) - 模型提供商名称
  - `modelName` (String) - 实际使用的模型版本
  - `usage` (TokenUsageVO) - Token使用统计
- **流式响应增强** - 流式接口支持返回Token统计和模型信息（最后一个chunk包含）
- **思考模式统一** - 所有支持思考的模型使用统一的API和行为
- **空内容过滤** - 自动过滤空chunk，避免输出空白内容

**🐛 缺陷修复**
- 修复流式响应中空字符串导致的输出问题
- 修复QWen/QWenVL的Usage类型转换错误
- 修复Spark/QWen在同时返回content和reasoning_content时的优先级问题
- 修复思考模式禁用不生效的问题（现在会显式设置disabled）
- 修复流式响应中NPE问题
- 修复DeepSeek思考模式支持（之前未启用）

**⚠️ 向后兼容性**
- 保留了所有旧字段和构造方法（标记为@Deprecated）
- 新增字段不影响现有业务代码
- 建议使用新的 `ModelResponseVO.of()` 工厂方法

### v1.0.8

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

## 💡 完整使用示例

### 示例1：基础对话（带Token统计）

```java
@Service
public class ChatService {
    
    public void basicChat() {
        // 创建请求
        ModelRequestVO request = new ModelRequestVO()
            .setModelName("qWen")
            .setModel("qwen-turbo")
            .setPrompt("介绍一下Spring Boot");
        
        // 获取服务
        AiService aiService = FactoryModelService.create("qWen");
        
        // 同步调用
        ModelResponseVO response = aiService.httpChat(request);
        
        // 输出结果
        System.out.println("答案: " + response.getResult());
        System.out.println("模型: " + response.getModelName());
        System.out.println("Token使用: " + response.getUsage().getTotalTokens());
    }
}
```

### 示例2：思考模式（深度推理）

```java
public void thinkingMode() {
    ModelRequestVO request = new ModelRequestVO()
        .setModelName("deepSeek")
        .setModel("deepseek-chat")
        .setEnableThinking(true)  // 启用思考模式
        .setPrompt("为什么天空是蓝色的？");
    
    AiService aiService = FactoryModelService.create("deepSeek");
    ModelResponseVO response = aiService.httpChat(request);
    
    if (response.getThinking()) {
        System.out.println("=== 模型思考过程 ===");
        System.out.println(response.getThinkingContent());
        System.out.println("\n=== 最终答案 ===");
    }
    System.out.println(response.getResult());
}
```

### 示例3：流式对话（实时输出）

```java
public void streamChat() {
    ModelRequestVO request = new ModelRequestVO()
        .setModelName("spark")
        .setModel("x1")
        .setPrompt("写一首关于春天的诗");
    
    AiService aiService = FactoryModelService.create("spark");
    
    // 方式1：纯文本流（最简单）
    aiService.streamChatStr(request).subscribe(
        text -> System.out.print(text),  // 实时输出
        error -> System.err.println("错误: " + error),
        () -> System.out.println("\n完成")
    );
    
    // 方式2：完整信息流（包含Token统计）
    aiService.streamChat(request).subscribe(
        chunk -> {
            System.out.print(chunk.getResult());
            if (chunk.getUsage() != null) {
                System.out.println("\nToken: " + chunk.getUsage().getTotalTokens());
            }
        }
    );
}
```

### 示例4：多轮对话（上下文）

```java
public void multiTurnChat() {
    List<ModelRequestVO.Message> messages = new ArrayList<>();
    
    // 第一轮对话
    messages.add(new ModelRequestVO.Message()
        .setRole("user")
        .setContent("我叫张三"));
    
    ModelRequestVO request = new ModelRequestVO()
        .setModelName("chatgpt")
        .setModel("gpt-3.5-turbo")
        .setMessages(messages);
    
    AiService aiService = FactoryModelService.create("chatgpt");
    ModelResponseVO response1 = aiService.httpChat(request);
    
    // 添加助手回复到上下文
    messages.add(new ModelRequestVO.Message()
        .setRole("assistant")
        .setContent(response1.getResult()));
    
    // 第二轮对话
    messages.add(new ModelRequestVO.Message()
        .setRole("user")
        .setContent("我叫什么名字？"));
    
    request.setMessages(messages);
    ModelResponseVO response2 = aiService.httpChat(request);
    System.out.println(response2.getResult());  // 输出：您叫张三
}
```

### 示例5：多模态（图片分析）

```java
public void imageAnalysis() {
    ModelRequestVO request = new ModelRequestVO()
        .setModelName("qwenVL")
        .setModel("qwen-vl-plus")
        .setContents(Arrays.asList(
            MessageContent.imageUrl("https://example.com/product.jpg"),
            MessageContent.text("这个产品有什么特点？")
        ));
    
    AiService aiService = FactoryModelService.create("qwenVL");
    ModelResponseVO response = aiService.httpChat(request);
    System.out.println(response.getResult());
}
```

### 示例6：成本统计

```java
@Service
public class CostAnalysisService {
    
    public void analyzeCost() {
        ModelRequestVO request = new ModelRequestVO()
            .setModelName("douBao")
            .setModel("doubao-seed-code-preview-251028")
            .setEnableThinking(true)
            .setPrompt("分析微服务架构的优缺点");
        
        AiService aiService = FactoryModelService.create("douBao");
        ModelResponseVO response = aiService.httpChat(request);
        
        TokenUsageVO usage = response.getUsage();
        
        // 根据实际价格计算成本
        double inputCost = usage.getPromptTokens() * 0.0001;  // 假设价格
        double outputCost = usage.getCompletionTokens() * 0.0002;
        double totalCost = inputCost + outputCost;
        
        System.out.println("模型: " + response.getModelName());
        System.out.println("输入Token: " + usage.getPromptTokens());
        System.out.println("输出Token: " + usage.getCompletionTokens());
        System.out.println("总Token: " + usage.getTotalTokens());
        System.out.println("预估成本: ¥" + totalCost);
        
        // 思考模式的额外Token
        if (usage.getReasoningTokens() != null) {
            System.out.println("推理Token: " + usage.getReasoningTokens());
        }
    }
}
```

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
- [DeepSeek API 文档](https://api-docs.deepseek.com/zh-cn/)
- [讯飞星火官方文档](https://www.xfyun.cn/doc/spark/)
- [豆包官方文档](https://www.volcengine.com/docs/82379/1494384)

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
