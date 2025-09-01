# AI大模型服务集成框架

一个基于Spring Boot的AI大模型服务集成框架，支持多种大模型服务的统一接入，包括讯飞星火、通义千问、ChatGPT、和DeepSeek等。

## 技术栈

| 技术/框架                | 版本         | 说明                         |
|--------------------------|--------------|------------------------------|
| Java                     | 8            | 项目主语言                   |
| Spring Boot              | 2.7.17       | 应用框架，简化开发与配置     |
| Spring Web/WebFlux       | 2.7.17       | 支持同步与响应式 Web 服务    |
| Lombok                   | 最新         | 简化 Java 代码（如 getter/setter）|
| Hutool                   | 5.8.25       | Java 工具类库                |
| SLF4J                    | 1.7.36       | 日志门面                     |
| Apache Commons Lang3     | 3.12.0       | 常用工具类                   |
| Maven                    | -            | 项目构建与依赖管理           |
| JUnit（Spring Boot Test）| 2.7.17       | 单元测试                     |

## 功能特性

- 支持多种大模型服务统一接入
- 提供同步和流式两种对话模式
- 支持参数透传和自定义配置
- 统一的异常处理和日志记录
- 支持系统提示词和思考模式
- 基于Spring Boot的自动配置

## 项目结构

本项目采用模块化设计，主要包含以下模块：

- **FactoryModelService**: 提供统一的模型服务工厂，支持根据枚举值切换不同的模型服务。
- **AiService**: 定义了大模型服务的统一接口，包括同步和流式对话方法。
- **ModelRequestVO/ModelResponseVO**: 请求和响应的数据模型，支持参数透传和自定义配置。

### 模块结构

```
src/main/java/com/jeesoul/ai/model/
├── factory/          # 工厂模块 - 服务实例创建
├── service/          # 服务模块 - 核心业务逻辑
├── strategy/         # 策略模块 - 动态服务选择
├── vo/              # 视图对象 - 数据传输
├── request/         # 请求对象 - API请求封装
├── response/        # 响应对象 - API响应封装
├── entity/          # 实体对象 - 数据模型
├── config/          # 配置模块 - 参数管理
├── constant/        # 常量定义 - 枚举和常量
├── util/            # 工具模块 - 通用工具类
└── exception/       # 异常处理 - 自定义异常
```

## 架构设计

### 整体架构

本框架采用**多层架构 + 策略模式 + 工厂模式**的设计，核心设计理念：

1. **统一接口抽象**：通过`AiService`接口统一不同AI服务商的调用方式
2. **策略模式解耦**：使用策略模式动态选择不同的AI服务实现
3. **工厂模式创建**：通过工厂类统一管理服务实例的创建
4. **参数透传机制**：支持灵活的参数传递，适配不同模型的特殊需求
5. **响应式编程**：支持同步和流式两种调用方式

![整体架构图](img/overall-architecture.md)

### 设计模式应用

框架中应用了多种经典设计模式：

- **工厂模式**：`FactoryModelService`统一创建服务实例
- **策略模式**：`AiStrategyContext`动态选择具体实现
- **模板方法模式**：`AiService`接口定义统一的调用规范
- **建造者模式**：`ModelRequestVO`支持链式调用构建请求

![设计模式图](img/design-patterns.md)

### 数据流架构

系统的数据流程包括服务创建、请求处理和响应返回三个主要阶段：

![数据流图](img/data-flow.md)

### 技术架构分层

采用经典的分层架构设计，各层职责清晰：

![分层架构图](img/layered-architecture.md)

### 架构优势

1. **高内聚低耦合**
   - 每个模块职责单一，相互之间依赖关系清晰
   - 通过接口抽象实现了服务层与具体实现的解耦

2. **可扩展性强**
   - 新增AI服务商只需实现`AiService`接口
   - 在枚举中添加新的模型配置即可
   - 策略模式支持运行时动态切换

3. **统一的调用方式**
   - 不同AI服务商提供统一的调用接口
   - 支持同步和异步两种调用模式
   - 参数透传机制适配不同模型的特殊需求

4. **响应式编程支持**
   - 基于Spring WebFlux提供流式处理能力
   - 支持大文本生成的实时响应
   - 提供了灵活的数据流处理方式

5. **配置化管理**
   - 通过Spring Boot配置管理不同服务商的参数
   - 支持环境隔离和动态配置

## 快速开始

### 1. 添加依赖

```xml
<!--建议引入中央仓库地址-->
<repositories>
    <repository>
        <id>central</id>
        <url>https://repo.maven.apache.org/maven2/</url>
    </repository>
</repositories>

<dependencies>
<!--已发布到 maven 中央仓库 目前最新版: 1.0.6-->
<dependency>
    <groupId>com.jeesoul</groupId>
    <artifactId>jeesoul-ai-model</artifactId>
    <version>1.0.6</version>
</dependency>

<!--集成响应式编程-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
</dependencies>
```

### 2. 配置参数

在`application.yml`中添加配置：

```yaml
ai:
  qwen:
    apiKey: your-qwen-api-key
    endpoint: https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation
  spark:
    apiKey: your-spark-api-key
    endpoint: https://spark-api-open.xf-yun.com/v2/chat/completions
  deep-seek:
    apiKey: your-deepseek-api-key
    endpoint: https://api.deepseek.com/v1/chat/completions
  chat-gpt:
    apiKey: your-chatgpt-api-key
    endpoint: https://api.openai.com/v1/chat/completions
```

### 3. 使用示例

#### 通义千问 (QWen) 示例

```java
public void qwenChat() {
    // 创建请求对象（使用链式调用）

    ModelRequestVO request = new ModelRequestVO()
        .setModelName("qWen")
        .setModel("qwen-turbo")
        .setSystemPrompt("你是一个专业的AI助手")
        .setPrompt("你好，请介绍一下自己")
        .setEnableThinking(true)  // 开启思考模式
        .setParams(params);
    request.setTemperature(0.7);
    request.setTopP(0.8);
    request.setMaxTokens(2000);
    // 获取服务实例并调用
    AiService aiService = FactoryModelService.create(request.getModelName());
    
    // 同步对话
    ModelResponseVO response = aiService.httpChat(request);
    System.out.println("回答: " + response.getResult());
    
    // 流式对话
    Flux<ModelResponseVO> responseFlux = aiService.streamChat(request);
    responseFlux.subscribe(response -> {
        System.out.print(response.getResult());
        if (response.getThinking() != null) {
            System.out.println("\n思考过程：" + response.getThinking());
        }
    });
}
```

#### 讯飞星火 (Spark) 示例

```java
public void sparkChat() {
    ModelRequestVO request = new ModelRequestVO()
        .setModelName("spark")
        .setModel("x1")
        .setSystemPrompt("你是一个富有创造力的诗人")
        .setPrompt("请写一首关于春天的诗");
    
    AiService aiService = FactoryModelService.create(request.getModelName());
    
    // 同步对话
    ModelResponseVO response = aiService.httpChat(request);
    System.out.println("星火回答: " + response.getResult());
    
    // 流式对话 - 获取原始文本流
    Flux<String> textFlux = aiService.streamChatStr(request);
    textFlux.subscribe(text -> {
        System.out.print(text);
        // 可以实时显示生成的内容
    });
}
```

#### ChatGPT 示例

```java
public void chatGPTChat() {

    ModelRequestVO request = new ModelRequestVO()
        .setModelName("chatgpt")
        .setModel("gpt-3.5-turbo")
        .setSystemPrompt("你是一个技术专家，擅长解释复杂概念")
        .setPrompt("请解释什么是微服务架构")
        .setParams(params);
    request.setTemperature(0.7);
    request.setTopP(0.8);
    request.setMaxTokens(2000);
    AiService aiService = FactoryModelService.create(request.getModelName());
    
    // 同步对话
    ModelResponseVO response = aiService.httpChat(request);
    System.out.println("ChatGPT回答: " + response.getResult());
    
    // 流式对话
    Flux<ModelResponseVO> responseFlux = aiService.streamChat(request);
    responseFlux.subscribe(response -> {
        System.out.print(response.getResult());
    });
}
```

#### DeepSeek 示例

```java
public void deepSeekChat() {
    ModelRequestVO request = new ModelRequestVO()
        .setModelName("deepSeek")
        .setModel("deepseek-chat")
        .setSystemPrompt("你是一个数学老师，擅长解题")
        .setPrompt("请帮我解这个方程：2x + 5 = 13");
    
    AiService aiService = FactoryModelService.create(request.getModelName());
    
    // 同步对话
    ModelResponseVO response = aiService.httpChat(request);
    System.out.println("DeepSeek回答: " + response.getResult());
    
    // 流式对话
    Flux<ModelResponseVO> responseFlux = aiService.streamChat(request);
    responseFlux.subscribe(response -> {
        System.out.print(response.getResult());
    });
}
```

#### 通用流式对话处理

```java
public void handleStreamChat(String modelName, String prompt) {
    ModelRequestVO request = new ModelRequestVO()
        .setModelName(modelName)
        .setModel(getDefaultModel(modelName))
        .setPrompt(prompt);
    
    AiService aiService = FactoryModelService.create(modelName);
    
    // 获取流式响应
    Flux<ModelResponseVO> responseFlux = aiService.streamChat(request);
    
    // 处理流式响应
    responseFlux.subscribe(
        response -> {
            // 处理每个响应片段
            System.out.print(response.getResult());
        },
        error -> {
            // 错误处理
            System.err.println("对话出错: " + error.getMessage());
        },
        () -> {
            // 完成处理
            System.out.println("\n对话完成");
        }
    );
}

private String getDefaultModel(String modelName) {
    switch (modelName.toLowerCase()) {
        case "qwen": return "qwen-turbo";
        case "spark": return "x1";
        case "chatgpt": return "gpt-3.5-turbo";
        case "deepseek": return "deepseek-chat";
        default: return "qwen-turbo";
    }
}
```

## 支持的模型

| 模型名称 | 枚举值 | 默认模型 | 说明 |
|---------|--------|----------|------|
| 通义千问 | qWen | qwen-turbo | 阿里云大模型，支持思考模式 |
| 讯飞星火 | spark | x1 | 科大讯飞大模型，支持流式对话 |
| ChatGPT | chatgpt | gpt-3.5-turbo | OpenAI大模型，功能全面 |
| DeepSeek | deepSeek | deepseek-chat | 深度求索大模型，擅长推理 |

## 参数说明

### ModelRequestVO

| 参数名 | 类型 | 是否必填 | 说明                                |
|--------|------|----------|-----------------------------------|
| modelName | String | 是 | 模型名称（qWen/spark/chatgpt/deepSeek） |
| model | String | 是 | 具体模型版本                            |
| systemPrompt | String | 否 | 系统提示词                             |
| prompt | String | 是 | 用户提示词                             |
| enableThinking | boolean | 否 | 是否开启思考模式 (支持QWen系列快、慢思考)          |
| params | Map<String,Object> | 否 | 自定义参数                             |

### ModelResponseVO

| 参数名 | 类型 | 说明 |
|--------|------|------|
| result | String | 返回结果 |
| thinking | Boolean | 思考过程（如果启用） |
| model | String | 模型名称 |

## 最佳实践

### 1. 错误处理

```java
try {
    ModelResponseVO response = aiService.httpChat(request);
    // 处理成功响应
} catch (AiException e) {
    log.error("AI服务调用失败: {}", e.getMessage(), e);
    // 根据错误类型进行相应处理
    if (e.getMessage().contains("API密钥")) {
        // 处理认证错误
    } else if (e.getMessage().contains("配额")) {
        // 处理配额限制
    }
}
```

### 2. 重试机制

```java
public ModelResponseVO chatWithRetry(ModelRequestVO request, int maxRetries) {
    for (int i = 0; i < maxRetries; i++) {
        try {
            return aiService.httpChat(request);
        } catch (AiException e) {
            if (i == maxRetries - 1) {
                throw e;
            }
            log.warn("第{}次调用失败，准备重试: {}", i + 1, e.getMessage());
            try {
                Thread.sleep(1000 * (i + 1)); // 指数退避
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new AiException("重试被中断", ie);
            }
        }
    }
    throw new AiException("重试次数已达上限");
}
```

### 3. 流式对话优化

```java
public void optimizedStreamChat(ModelRequestVO request) {
    AiService aiService = FactoryModelService.create(request.getModelName());
    
    // 使用背压控制，避免内存溢出
    Flux<ModelResponseVO> responseFlux = aiService.streamChat(request)
        .onBackpressureBuffer(1000) // 限制缓冲区大小
        .doOnNext(response -> {
            // 实时处理每个响应片段
            processResponse(response);
        })
        .doOnError(error -> {
            // 错误处理
            log.error("流式对话出错: {}", error.getMessage());
        })
        .doOnComplete(() -> {
            // 完成处理
            log.info("流式对话完成");
        });
    
    responseFlux.subscribe();
}
```

## 注意事项

1. **配置参数名称**：确保使用 `apiKey` 而不是 `api-key`，这是Spring Boot的属性绑定规则
2. **API密钥安全**：不要在代码中硬编码API密钥，使用环境变量或配置中心
3. **流式对话处理**：建议使用响应式编程方式处理，避免阻塞主线程
4. **错误处理**：在生产环境中添加适当的错误处理和重试机制
5. **参数验证**：在调用前验证必要参数，避免运行时错误
6. **资源管理**：及时释放流式连接，避免资源泄漏

## 常见问题

### Q: 配置不生效怎么办？
A: 检查配置参数名称是否正确，确保使用 `apiKey` 而不是 `api-key`

### Q: 流式对话没有响应？
A: 检查是否正确订阅了Flux流，确保调用了 `subscribe()` 方法

### Q: 如何切换不同的模型？
A: 使用 `FactoryModelService.create(modelName)` 方法，传入对应的模型名称

### Q: 支持哪些自定义参数？
A: 支持各模型的标准参数，如temperature、top_p、max_tokens等

## 贡献指南

欢迎提交Issue和Pull Request来帮助改进这个项目。更多信息请访问 [贡献指南](https://github.com/jeesoul/jeesoul-ai-model/blob/main/CONTRIBUTING.md)。

## 作者介绍

本项目由 [dongxueyong](http://jeesoul.com) 开发和维护。如有任何问题或建议，欢迎通过 [邮箱](mailto:3248838607@qq.com) 联系。

## 项目介绍

本项目旨在提供一个统一的接口，方便开发者快速集成多种AI大模型服务。通过简单的配置和调用，开发者可以轻松实现与大模型的交互，无需关注底层实现细节。更多信息请访问 [项目主页](https://github.com/jeesoul/jeesoul-ai-model)。

## 许可证

本项目采用 MIT 许可证。详情请查看 [LICENSE](https://opensource.org/licenses/MIT) 文件。

