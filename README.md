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
<!--已发布到 maven 中央仓库 目前最新版: 1.0.3-->
<dependency>
    <groupId>com.jeesoul</groupId>
    <artifactId>jeesoul-ai-model</artifactId>
    <version>1.0.3</version>
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
    api-key: your-api-key
    endpoint: https://api.qwen.com/v1/chat/completions
  spark:
    api-key: your-api-key
    endpoint: https://api.spark.com/v1/chat/completions
  deep-seek:
    api-key: your-api-key
    endpoint: https://api.deepseek.com/v1/chat/completions
```

### 3. 使用示例

#### 同步对话

```java

public void chat() {
    // 非必填参数 创建请求对象（使用链式调用）
    Map<String, Object> params = new HashMap<>();
    params.put("temperature", 0.7);
    params.put("top_p", 0.9);
    params.put("max_tokens", 2000);
    
    ModelRequestVO request = new ModelRequestVO()
        .setModelName("qWen")  // 或 "spark", "deepSeek"
        .setModel("qwen-turbo")  // 具体模型版本
        .setPrompt("你好，请介绍一下自己")
        .setParams(params);
    
    // 获取服务实例并调用
    AiService aiService = FactoryModelService.create(request.getModelName());
    ModelResponseVO response = aiService.httpChat(request);
    System.out.println(response.getResult());
}
```

#### 流式对话

```java

public void streamChat() {
    // 使用链式调用创建请求对象
    ModelRequestVO request = new ModelRequestVO()
        .setModelName("spark")
        .setModel("x1")
        .setPrompt("写一首诗");
    
    AiService aiService = FactoryModelService.create(request.getModelName());
    
    // 方式1：获取ModelResponseVO流
    Flux<ModelResponseVO> responseFlux = aiService.streamChat(request);
    responseFlux.subscribe(response -> {
        System.out.println(response.getResult());
        if (response.getThinking() != null) {
            System.out.println("思考过程：" + response.getThinking());
        }
    });
    
    // 方式2：获取原始文本流
    Flux<String> textFlux = aiService.streamChatStr(request);
    textFlux.subscribe(System.out::println);
}
```

## 支持的模型

| 模型名称 | 枚举值 |
|---------|--------|
| 讯飞星火 | spark |
| ChatGPT | chatgpt |
| 通义千问 | qWen |
| DeepSeek | deepSeek |

## 参数说明

### ModelRequestVO

| 参数名 | 类型 | 是否必填 | 说明                                |
|--------|------|----------|-----------------------------------|
| modelName | String | 是 | 模型名称（qWen/chatgpt/spark/deepSeek） |
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

## 注意事项

1. 使用前请确保已正确配置API密钥和端点地址
2. 流式对话建议使用响应式编程方式处理
3. 参数透传支持常见参数（temperature/top_p/max_tokens）和其他自定义参数
4. 建议在生产环境中添加适当的错误处理和重试机制

## 贡献指南

欢迎提交Issue和Pull Request来帮助改进这个项目。更多信息请访问 [贡献指南](https://github.com/jeesoul/jeesoul-ai-model/blob/main/CONTRIBUTING.md)。

## 作者介绍

本项目由 [dongxueyong](http://jeesoul.com) 开发和维护。如有任何问题或建议，欢迎通过 [邮箱](mailto:3248838607@qq.com) 联系。

## 项目介绍

本项目旨在提供一个统一的接口，方便开发者快速集成多种AI大模型服务。通过简单的配置和调用，开发者可以轻松实现与大模型的交互，无需关注底层实现细节。更多信息请访问 [项目主页](https://github.com/jeesoul/jeesoul-ai-model)。

## 许可证

本项目采用 MIT 许可证。详情请查看 [LICENSE](https://opensource.org/licenses/MIT) 文件。

