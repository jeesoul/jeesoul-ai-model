# 扩展自定义模型

本库支持在**不修改框架源码**的前提下接入新模型。三种方式，按侵入性从低到高排列，
推荐第一种。

## 方式一：@AiModelService 注解（推荐）

无需修改 `AiProperties.java`，符合开闭原则。

### 1. 创建配置类

继承 `BaseModelConfig` 即自动实现 `ModelConfig` 接口：

```java
import com.jeesoul.ai.model.config.BaseModelConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "ai.myai")
public class MyModelProperties extends BaseModelConfig {
    // 可选：添加额外配置
    // private String region;
    // private String version;
}
```

### 2. 创建 Service 类

如果目标模型的接口协议与已有模型一致（例如同为 OpenAI 兼容格式），
直接继承对应的 Service 即可复用全部实现，只需提供自己的配置：

```java
import com.jeesoul.ai.model.annotation.AiModelService;
import com.jeesoul.ai.model.service.SparkService;
import org.springframework.stereotype.Service;

@Service
@AiModelService(
    modelName = "myai",
    serviceName = "myService",
    description = "自定义模型（复用SparkService实现）"
)
public class MyService extends SparkService {

    public MyService(MyModelProperties modelConfig) {
        super(modelConfig);  // 工具类已静态化，无需注入
    }
}
```

启动时注解会被自动扫描并注册到策略表。

### 3. 配置文件

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

### 4. 调用

与内置模型完全一致：

```java
AiService myService = FactoryModelService.create("myai");

ModelRequestVO request = new ModelRequestVO()
    .setModelName("myai")
    .setModel("your-model-name")
    .setPrompt("Hello!");

ModelResponseVO response = myService.httpChat(request);
```

### 这种方式的优势

- **零侵入**：无需修改框架核心代码
- **易扩展**：继承现有 Service 即可复用所有功能
- **独立配置**：每个模型有独立的配置类
- **自动注册**：`@AiModelService` 注解自动完成注册

## 方式二：完全自定义实现

目标模型的请求/响应格式与现有模型都不一样时，继承 `AbstractAiService`，
实现 `buildRequest`、响应解析等抽象方法即可。基类已抽出参数校验、参数兜底、
消息构建等公共逻辑，子类只处理差异部分。

完整可运行示例见 [Claude AI 集成示例](../examples/claude-integration/README.md)，
含 300+ 行的 `ClaudeService` 实现、REST 控制器、配置与 curl 测试命令。

## 方式三：手动注册

不想用注解扫描时，可以在启动后手动往策略表里注册：

```java
@Component
public class CustomModelRegistrar {
    @PostConstruct
    public void registerModels() {
        AiStrategyContext.registerModel("claude", "claudeService");
    }
}
```

第二个参数是 Spring 容器中的 Bean 名。

## 检查模型是否已注册

```java
if (AiStrategyContext.isModelRegistered("qWen")) {
    log.info("QWen 模型可用");
}
```

## 相关文档

- [README](../README.md) - 快速开始与统一 API
- [Claude AI 集成示例](../examples/claude-integration/README.md) - 完全自定义实现的完整范例
- [多模态视觉示例](../examples/multimodal-vision/README.md) - 图片/视频分析场景
