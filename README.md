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



## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.jeesoul</groupId>
    <artifactId>jeesoul-ai-model</artifactId>
    <version>${latest.version}</version>
</dependency>

<!--集成响应式编程-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

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
| enableThinking | boolean | 否 | 是否开启思考模式 (后期加入)                   |
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

欢迎提交Issue和Pull Request来帮助改进这个项目。

## 许可证

本项目采用 MIT 许可证。

