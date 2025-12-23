# Release Notes - v1.0.9-GA

> **发布日期**: 2025年1月
> **版本类型**: 重要功能更新 + 架构重构

---

## 🎉 主要更新

### 🏗️ 架构重构（重大改进）

#### 1. 模型配置抽象化 - ModelConfig 接口
- ✅ 引入 `ModelConfig` 接口，解耦配置与实现
- ✅ 新增 `BaseModelConfig` 抽象类，提供通用配置属性
- ✅ 所有内置模型的 `*Properties` 类实现 `ModelConfig` 接口
- ✅ **解决核心问题**：扩展自定义模型不再需要修改 `AiProperties.java`
- ✅ 符合**开闭原则**：对扩展开放，对修改关闭

**使用示例：**
```java
// 自定义模型配置
@Data
@ConfigurationProperties(prefix = "ai.myai")
public class MyModelProperties extends BaseModelConfig {
    // 可选：添加额外配置
}

// 自定义模型服务
@Service
@AiModelService(modelName = "myai", serviceName = "myService")
public class MyService extends SparkService {
    public MyService(MyModelProperties modelConfig) {
        super(modelConfig);  // 只需传入配置，无需修改框架代码
    }
}
```

#### 2. HTTP 工具类静态化
- ✅ `HttpUtils` 和 `StreamHttpUtils` 改为静态工具类
- ✅ 添加私有构造函数，防止实例化
- ✅ 所有 Service 改为使用静态方法调用
- ✅ 简化 AutoConfiguration，移除工具类注入依赖
- ✅ 提升性能：减少对象创建和内存占用
- ✅ 简化扩展：自定义模型无需注入工具类

**改进对比：**
```java
// 旧方式（v1.0.8）
public MyService(AiProperties aiProperties, HttpUtils aiHttpUtils, StreamHttpUtils streamHttpUtils) {
    super(aiProperties.getSpark(), aiHttpUtils, streamHttpUtils);
    // 使用：aiHttpUtils.post(...)
}

// 新方式（v1.0.9-GA）
public MyService(MyModelProperties modelConfig) {
    super(modelConfig);
    // 使用：HttpUtils.post(...) - 静态调用
}
```

#### 3. FactoryModelService 支持自定义模型
- ✅ 修改 `FactoryModelService.create()` 方法，支持自定义模型
- ✅ 不再仅检查枚举，同时检查策略上下文中的注册模型
- ✅ 自定义模型可通过 `@AiModelService` 注解自动注册

**使用示例：**
```java
// 内置模型
AiService sparkService = FactoryModelService.create("spark");

// 自定义模型（现在完全支持！）
AiService myService = FactoryModelService.create("myai");
```

#### 4. 自定义模型扩展机制优化
- ✅ 完全复用现有 Service：继承即可，无需重写代码
- ✅ 独立配置管理：每个模型有独立的配置类
- ✅ 自动注册：使用 `@AiModelService` 注解自动注册
- ✅ 零侵入：无需修改框架核心代码

**完整示例：**
```java
// 1. 配置类
@Data
@ConfigurationProperties(prefix = "ai.myai")
public class MyModelProperties extends BaseModelConfig {
    // 继承所有通用配置
}

// 2. Service 类
@Service
@AiModelService(modelName = "myai", serviceName = "myService")
public class MyService extends SparkService {
    public MyService(MyModelProperties modelConfig) {
        super(modelConfig);
    }
}

// 3. 配置文件
ai:
  myai:
    api-key: your-api-key
    endpoint: https://your-endpoint.com
    model: your-model-name

// 4. 使用
AiService service = FactoryModelService.create("myai");
```

---

### ✨ 新增功能

#### 1. 豆包（DouBao）模型集成
- 完整支持字节跳动豆包大模型
- 支持同步和流式对话
- 支持思考模式（深度推理）
- 配置示例：
```yaml
ai:
  doubao:
    api-key: your-api-key
    endpoint: https://ark.cn-beijing.volces.com/api/v3/chat/completions
    model: doubao-seed-code-preview-251028
```

#### 2. Token 统计功能
- 所有模型的同步接口均返回详细的 Token 使用统计
- 流式接口支持在最后一个 chunk 返回 Token 统计（豆包除外）
- 新增 `TokenUsageVO` 对象，包含以下字段：
  - `promptTokens` - 输入 Token 数
  - `completionTokens` - 输出 Token 数
  - `totalTokens` - 总 Token 数
  - `reasoningTokens` - 推理 Token 数（思考模式）
  - `cachedTokens` - 缓存 Token 数（部分模型支持）
  - `inputTokens` / `outputTokens` - QWen 特有字段

#### 3. 完整的思考模式支持
支持思考模式的模型扩展到：
- ✅ **QWen** (通义千问)
- ✅ **Spark** (讯飞星火)
- ✅ **DeepSeek** (深度求索) - 新增
- ✅ **DouBao** (豆包) - 新增

#### 4. 模型信息追踪
- 响应中新增 `modelProvider` 字段 - 模型提供商（如 qWen, spark）
- 响应中新增 `modelName` 字段 - 实际使用的模型版本（如 qwen-turbo, x1）
- 方便业务方进行模型追踪和成本分析

---

## 🎨 API 变更

### 架构相关 API 变更（v1.0.9-GA 新增）

#### AbstractAiService 构造函数变更
```java
// v1.0.8 - 需要注入工具类
public AbstractAiService(AiProperties aiProperties, HttpUtils aiHttpUtils, StreamHttpUtils streamHttpUtils)

// v1.0.9-GA - 只需配置（推荐）
public AbstractAiService(ModelConfig modelConfig)

// v1.0.9-GA - 向后兼容（已废弃）
@Deprecated
public AbstractAiService(AiProperties aiProperties, HttpUtils aiHttpUtils, StreamHttpUtils streamHttpUtils)
```

#### Service 构造函数变更
```java
// v1.0.8
public SparkService(AiProperties aiProperties, HttpUtils aiHttpUtils, StreamHttpUtils streamHttpUtils)

// v1.0.9-GA - 推荐使用
public SparkService(ModelConfig modelConfig)

// v1.0.9-GA - 向后兼容（已废弃）
@Deprecated
public SparkService(AiProperties aiProperties, HttpUtils aiHttpUtils, StreamHttpUtils streamHttpUtils)
```

#### HTTP 工具类方法变更
```java
// v1.0.8 - 实例方法
HttpUtils httpUtils = new HttpUtils();
httpUtils.post(url, headers, body, responseType, config);

// v1.0.9-GA - 静态方法（推荐）
HttpUtils.post(url, headers, body, responseType, config);
```

#### FactoryModelService 增强
```java
// v1.0.8 - 仅支持内置模型
AiService service = FactoryModelService.create("spark");  // ✅
AiService service = FactoryModelService.create("myai");  // ❌ 不支持

// v1.0.9-GA - 支持自定义模型
AiService service = FactoryModelService.create("spark");  // ✅
AiService service = FactoryModelService.create("myai");   // ✅ 支持！
```

### ModelResponseVO 新增字段

```java
public class ModelResponseVO {
    // 新增字段
    private Boolean thinking;           // 是否使用思考模式
    private String thinkingContent;     // 思考过程内容
    private String modelProvider;       // 模型提供商
    private String modelName;           // 具体模型版本
    private TokenUsageVO usage;         // Token 使用统计
    
    // 已废弃字段（保留向后兼容）
    @Deprecated
    private String model;               // 使用 modelProvider 替代
}
```

### 使用示例

```java
// 同步调用
ModelResponseVO response = aiService.httpChat(request);
System.out.println("答案: " + response.getResult());
System.out.println("模型: " + response.getModelName());
System.out.println("Token: " + response.getUsage().getTotalTokens());

// 思考模式
if (response.getThinking()) {
    System.out.println("思考过程: " + response.getThinkingContent());
}

// 流式调用
aiService.streamChat(request).subscribe(chunk -> {
    System.out.print(chunk.getResult());
    if (chunk.getUsage() != null) {
        System.out.println("\nToken: " + chunk.getUsage().getTotalTokens());
    }
});
```

---

## 🔧 重大改进

### 1. 架构设计优化（v1.0.9-GA 新增）

#### 依赖注入简化
- **旧方式**：Service 需要注入 `HttpUtils` 和 `StreamHttpUtils`
- **新方式**：直接使用静态方法，无需注入
- **优势**：减少依赖，简化构造函数，提升性能

#### 配置管理解耦
- **旧方式**：扩展模型需要修改 `AiProperties.java`
- **新方式**：实现 `ModelConfig` 接口或继承 `BaseModelConfig`
- **优势**：符合开闭原则，易于扩展

#### 代码简化对比
```java
// v1.0.8 - 需要注入工具类
@RequiredArgsConstructor
public class SparkAutoConfiguration {
    private final HttpUtils aiHttpUtils;
    private final StreamHttpUtils streamHttpUtils;
    
    @Bean
    public AiService sparkService(AiProperties properties) {
        return new SparkService(properties.getSpark(), aiHttpUtils, streamHttpUtils);
    }
}

// v1.0.9-GA - 简化配置
@Configuration
public class SparkAutoConfiguration {
    @Bean
    public AiService sparkService(AiProperties properties) {
        return new SparkService(properties.getSpark());  // 只需配置
    }
}
```

### 2. 响应对象重构
- `thinking` 字段从 String 改为 Boolean，表示是否使用思考模式
- 新增 `thinkingContent` 字段存储实际的思考内容
- 保持向后兼容，旧字段标记为 @Deprecated

### 3. 流式响应增强
- 流式接口统一返回 `ResultContent` 对象
- 支持在流中携带 Token 统计和模型信息
- 自动过滤空 chunk，提升输出质量

### 4. 思考模式统一
- 所有支持思考的模型使用统一的 API
- 支持显式禁用思考模式（`setEnableThinking(false)`）
- 流式输出支持区分思考过程和最终答案

### 5. 空内容过滤
- 自动过滤流式响应中的空字符串
- 保留包含 Token 统计的最后一个 chunk
- 避免输出空白行

---

## 🐛 Bug 修复

### 1. 自定义模型扩展问题（v1.0.9-GA 修复）
- ✅ 修复扩展自定义模型需要修改 `AiProperties.java` 的问题
- ✅ 修复 `FactoryModelService.create()` 不支持自定义模型的问题
- ✅ 修复自定义模型无法使用工具类的问题（工具类已静态化）

### 2. 流式响应问题
- ✅ 修复流式响应输出空白行的问题
- ✅ 修复 `streamChatStr()` 中的 NPE 问题
- ✅ 修复空 chunk 导致的 Flux 错误

### 3. 类型转换问题
- ✅ 修复 QWen/QWenVL 的 Usage 类型转换错误
- ✅ 修复 Jackson 反序列化继承类的问题

### 4. 思考模式问题
- ✅ 修复 Spark/QWen 的 reasoning_content 提取错误
- ✅ 修复思考模式禁用不生效的问题
- ✅ 修复 DeepSeek 思考模式未启用的问题

### 5. 内容优先级问题
- ✅ 修复同时返回 content 和 reasoning_content 时的处理逻辑
- ✅ 确保 reasoning_content 优先级高于 content

---

## 📊 支持的模型对比

| 模型名称 | 模型标识 | 思考模式 | Token统计 | 多模态 | 流式Token |
|---------|---------|---------|----------|--------|----------|
| 通义千问 | qWen | ✅ | ✅ | ❌ | ✅ |
| ChatGPT | chatgpt | ❌ | ✅ | ❌ | ✅ |
| 讯飞星火 | spark | ✅ | ✅ | ❌ | ✅ |
| DeepSeek | deepSeek | ✅ | ✅ | ❌ | ✅ |
| 豆包 | douBao | ✅ | ✅ 同步 | ❌ | ❌ |
| 千问视觉 | qwenVL | ✅ | ✅ | ✅ | ✅ |

---

## ⚠️ 向后兼容性

### 完全兼容
v1.0.9-GA **完全向后兼容** v1.0.8，现有代码无需修改即可升级。

### 废弃的 API

#### 1. 废弃字段
以下字段已标记为 `@Deprecated`，但仍可使用：
- `ModelResponseVO.model` → 使用 `modelProvider` 和 `modelName` 替代

#### 2. 废弃的构造函数
以下构造函数已标记为 `@Deprecated`，但仍可使用：
```java
// 旧方式（仍然可用，但不推荐）
public Service(AiProperties aiProperties, HttpUtils aiHttpUtils, StreamHttpUtils streamHttpUtils) {
    super(aiProperties.getXxx(), aiHttpUtils, streamHttpUtils);
}

// 新方式（推荐）
public Service(ModelConfig modelConfig) {
    super(modelConfig);
}
```

#### 3. 废弃的配置类
- `HttpConfig` → `HttpUtils` 和 `StreamHttpUtils` 已改为静态工具类，无需 Bean 配置

### 推荐迁移

#### 字段迁移
```java
// 旧代码（仍然可用）
String model = response.getModel();

// 新代码（推荐）
String provider = response.getModelProvider();
String modelVersion = response.getModelName();
TokenUsageVO usage = response.getUsage();
```

#### 构造函数迁移
```java
// 旧代码（仍然可用）
public MyService(AiProperties aiProperties, HttpUtils aiHttpUtils, StreamHttpUtils streamHttpUtils) {
    super(aiProperties.getSpark(), aiHttpUtils, streamHttpUtils);
}

// 新代码（推荐）
public MyService(MyModelProperties modelConfig) {
    super(modelConfig);
}
```

#### 工具类使用迁移
```java
// 旧代码（仍然可用）
aiHttpUtils.post(url, headers, body, responseType, config);
streamHttpUtils.postStream(url, body, config);

// 新代码（推荐）
HttpUtils.post(url, headers, body, responseType, config);
StreamHttpUtils.postStream(url, body, config);
```

---

## 📚 文档更新

### 主文档
- ✅ README.md - 完整更新，新增 6 个使用示例
- ✅ 新增常见问题 FAQ 章节
- ✅ 新增升级指南章节
- ✅ 更新参数说明和 API 文档
- ✅ 新增自定义模型扩展指南

### 示例文档
- ✅ examples/claude-integration - 更新版本号和新特性说明
- ✅ examples/multimodal-vision - 新增 Token 统计示例
- ✅ examples/custom-model-extension - 新增自定义模型扩展示例（v1.0.9-GA）

### 架构文档
- ✅ img/layered-architecture.md - 新增 DouBao 和 QWenVL
- ✅ img/overall-architecture.md - 完整架构图更新
- ✅ 新增 ModelConfig 接口设计说明
- ✅ 新增静态工具类设计说明

---

## 🚀 升级步骤

### 1. 更新依赖
```xml
<dependency>
    <groupId>com.jeesoul</groupId>
    <artifactId>jeesoul-ai-model</artifactId>
    <version>1.0.9</version>  <!-- 或 1.0.9-GA -->
</dependency>
```

### 2. 可选配置（豆包）
```yaml
ai:
  doubao:
    api-key: your-api-key
    endpoint: https://ark.cn-beijing.volces.com/api/v3/chat/completions
    model: doubao-seed-code-preview-251028
```

### 3. 使用新特性（可选）
```java
// Token 统计
if (response.getUsage() != null) {
    System.out.println("Token: " + response.getUsage().getTotalTokens());
}

// 思考模式
request.setEnableThinking(true);
if (response.getThinking()) {
    System.out.println("思考: " + response.getThinkingContent());
}
```

### 4. 扩展自定义模型（v1.0.9-GA 新增）
```java
// 1. 创建配置类
@Data
@ConfigurationProperties(prefix = "ai.myai")
public class MyModelProperties extends BaseModelConfig {
    // 可选：添加额外配置
}

// 2. 创建 Service（完全复用 SparkService）
@Service
@AiModelService(modelName = "myai", serviceName = "myService")
public class MyService extends SparkService {
    public MyService(MyModelProperties modelConfig) {
        super(modelConfig);
    }
}

// 3. 配置文件
ai:
  myai:
    api-key: your-api-key
    endpoint: https://your-endpoint.com
    model: your-model-name

// 4. 使用
AiService service = FactoryModelService.create("myai");
```

---

## 📧 反馈与支持

如有问题或建议，欢迎联系：
- **Email**: 3248838607@qq.com
- **GitHub**: https://github.com/jeesoul/jeesoul-ai-model
- **Issues**: https://github.com/jeesoul/jeesoul-ai-model/issues

---

## 🎯 版本亮点总结

### v1.0.9-GA 核心改进

1. **架构重构**
   - ✅ ModelConfig 接口解耦配置
   - ✅ HTTP 工具类静态化
   - ✅ 自定义模型扩展零侵入

2. **功能增强**
   - ✅ 豆包模型完整支持
   - ✅ Token 统计功能
   - ✅ 思考模式统一支持

3. **性能优化**
   - ✅ 减少对象创建
   - ✅ 简化依赖注入
   - ✅ 提升扩展性能

4. **开发体验**
   - ✅ 扩展模型更简单
   - ✅ 代码更简洁
   - ✅ 文档更完善

## 🔄 迁移指南

### 从 v1.0.8 迁移到 v1.0.9-GA

#### 无需修改（完全兼容）
- ✅ 现有 Service 调用代码
- ✅ 现有配置方式
- ✅ 现有 API 使用

#### 可选优化（推荐）
- 🔄 使用 `ModelConfig` 接口扩展自定义模型
- 🔄 使用静态方法调用工具类
- 🔄 使用新的构造函数签名

#### 详细迁移步骤
参考 `examples/custom-model-extension/README.md` 获取完整的迁移示例。

## 🙏 致谢

感谢所有使用和支持本项目的开发者！

**v1.0.9-GA 版本重点：**
- 🏗️ 架构重构：解决扩展性问题，符合开闭原则
- 🚀 性能优化：工具类静态化，减少内存占用
- 🎨 开发体验：自定义模型扩展零侵入
- ✨ 功能增强：豆包模型、Token 统计、思考模式

期待大家的持续反馈和贡献！

---

**Happy Coding! 🎉**

