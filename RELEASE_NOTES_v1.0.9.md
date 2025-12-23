# Release Notes - v1.0.9-GA

> **发布日期**: 2025年1月  
> **版本类型**: 架构重构 + 功能增强  
> **升级版本**: v1.0.9 → v1.0.9-GA

---

## 📋 版本概述

v1.0.9-GA 是 v1.0.9 的稳定版本，主要聚焦于架构重构和扩展性改进。**完全向后兼容** v1.0.9，现有代码无需修改即可升级。

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
// 1. 配置类
@Data
@ConfigurationProperties(prefix = "ai.myai")
public class MyModelProperties extends BaseModelConfig {}

// 2. Service 类
@Service
@AiModelService(modelName = "myai", serviceName = "myService")
public class MyService extends SparkService {
    public MyService(MyModelProperties modelConfig) {
        super(modelConfig);
    }
}

// 3. 使用
AiService service = FactoryModelService.create("myai");
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
// v1.0.9 - 需要注入工具类
public MyService(AiProperties aiProperties, HttpUtils aiHttpUtils, StreamHttpUtils streamHttpUtils) {
    super(aiProperties.getSpark(), aiHttpUtils, streamHttpUtils);
    // 使用：aiHttpUtils.post(...)
}

// v1.0.9-GA - 只需配置
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

// 自定义模型（v1.0.9-GA 新增支持）
AiService myService = FactoryModelService.create("myai");
```

#### 4. 自定义模型扩展机制优化
- ✅ 完全复用现有 Service：继承即可，无需重写代码
- ✅ 独立配置管理：每个模型有独立的配置类
- ✅ 自动注册：使用 `@AiModelService` 注解自动注册
- ✅ 零侵入：无需修改框架核心代码

---

## 🎨 API 变更

### 架构相关 API 变更（v1.0.9-GA 新增）

#### AbstractAiService 构造函数变更
```java
// v1.0.9 - 需要注入工具类
public AbstractAiService(AiProperties aiProperties, HttpUtils aiHttpUtils, StreamHttpUtils streamHttpUtils)

// v1.0.9-GA - 只需配置（推荐）
public AbstractAiService(ModelConfig modelConfig)

// v1.0.9-GA - 向后兼容（已废弃）
@Deprecated
public AbstractAiService(AiProperties aiProperties, HttpUtils aiHttpUtils, StreamHttpUtils streamHttpUtils)
```

#### Service 构造函数变更
```java
// v1.0.9
public SparkService(AiProperties aiProperties, HttpUtils aiHttpUtils, StreamHttpUtils streamHttpUtils)

// v1.0.9-GA - 推荐使用
public SparkService(ModelConfig modelConfig)

// v1.0.9-GA - 向后兼容（已废弃）
@Deprecated
public SparkService(AiProperties aiProperties, HttpUtils aiHttpUtils, StreamHttpUtils streamHttpUtils)
```

#### HTTP 工具类方法变更
```java
// v1.0.9 - 实例方法
HttpUtils httpUtils = new HttpUtils();
httpUtils.post(url, headers, body, responseType, config);

// v1.0.9-GA - 静态方法（推荐）
HttpUtils.post(url, headers, body, responseType, config);
```

#### FactoryModelService 增强
```java
// v1.0.9 - 仅支持内置模型
AiService service = FactoryModelService.create("spark");  // ✅
AiService service = FactoryModelService.create("myai");   // ❌ 不支持

// v1.0.9-GA - 支持自定义模型
AiService service = FactoryModelService.create("spark");  // ✅
AiService service = FactoryModelService.create("myai");   // ✅ 支持！
```

### ModelResponseVO 字段说明

v1.0.9-GA 中 `ModelResponseVO` 字段保持不变，与 v1.0.9 完全兼容。

---

## 🔧 重大改进

### 架构设计优化

**依赖注入简化：**
- v1.0.9：Service 需要注入 `HttpUtils` 和 `StreamHttpUtils`
- v1.0.9-GA：直接使用静态方法，无需注入
- 优势：减少依赖，简化构造函数，提升性能

**配置管理解耦：**
- v1.0.9：扩展模型需要修改 `AiProperties.java`
- v1.0.9-GA：实现 `ModelConfig` 接口或继承 `BaseModelConfig`
- 优势：符合开闭原则，易于扩展

---

## 🐛 Bug 修复

### 1. 自定义模型扩展问题（v1.0.9-GA 修复）
- ✅ 修复扩展自定义模型需要修改 `AiProperties.java` 的问题
- ✅ 修复 `FactoryModelService.create()` 不支持自定义模型的问题
- ✅ 修复自定义模型无法使用工具类的问题（工具类已静态化）

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
v1.0.9-GA **完全向后兼容** v1.0.9，现有代码无需修改即可升级。

### 废弃的 API

#### 1. 废弃字段
以下字段已标记为 `@Deprecated`，但仍可使用：
- `ModelResponseVO.model` → 使用 `modelProvider` 和 `modelName` 替代

#### 2. 废弃的构造函数
以下构造函数已标记为 `@Deprecated`，但仍可使用：
```java
// v1.0.9 方式（仍然可用，但不推荐）
public Service(AiProperties aiProperties, HttpUtils aiHttpUtils, StreamHttpUtils streamHttpUtils) {
    super(aiProperties.getXxx(), aiHttpUtils, streamHttpUtils);
}

// v1.0.9-GA 方式（推荐）
public Service(ModelConfig modelConfig) {
    super(modelConfig);
}
```

#### 3. 废弃的配置类
- `HttpConfig` → `HttpUtils` 和 `StreamHttpUtils` 已改为静态工具类，无需 Bean 配置

### 推荐迁移

详见下方 [🔄 迁移指南](#-迁移指南) 章节。

---

## 📚 文档更新

### 主文档
- ✅ README.md - 精简优化，移除重复内容
- ✅ 更新版本号到 v1.0.9-GA
- ✅ 优化自定义模型扩展说明

### 示例文档
- ✅ examples/claude-integration - 更新 v1.0.9-GA 新特性说明
- ✅ examples/multimodal-vision - 更新架构改进说明

---

## 🚀 升级步骤

### 1. 更新依赖
```xml
<dependency>
    <groupId>com.jeesoul</groupId>
    <artifactId>jeesoul-ai-model</artifactId>
    <version>1.0.9-GA</version>
</dependency>
```

### 2. 无需修改代码
v1.0.9-GA 完全向后兼容 v1.0.9，现有代码无需修改即可升级。

### 3. 可选：使用新特性（扩展自定义模型）
详见下方 [🔄 迁移指南](#-迁移指南) 章节。

---

## 🔄 迁移指南

### 从 v1.0.9 迁移到 v1.0.9-GA

#### 无需修改（完全兼容）
- ✅ 现有 Service 调用代码
- ✅ 现有配置方式
- ✅ 现有 API 使用

#### 可选优化（推荐）

**构造函数迁移：**
```java
// v1.0.9 代码（仍然可用）
public MyService(AiProperties aiProperties, HttpUtils aiHttpUtils, StreamHttpUtils streamHttpUtils) {
    super(aiProperties.getSpark(), aiHttpUtils, streamHttpUtils);
}

// v1.0.9-GA 代码（推荐）
public MyService(MyModelProperties modelConfig) {
    super(modelConfig);
}
```

**工具类使用迁移：**
```java
// v1.0.9 代码（仍然可用）
aiHttpUtils.post(url, headers, body, responseType, config);
streamHttpUtils.postStream(url, body, config);

// v1.0.9-GA 代码（推荐）
HttpUtils.post(url, headers, body, responseType, config);
StreamHttpUtils.postStream(url, body, config);
```

**扩展自定义模型：**
```java
// 1. 创建配置类
@Data
@ConfigurationProperties(prefix = "ai.myai")
public class MyModelProperties extends BaseModelConfig {}

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

详细示例参考：📖 [`examples/claude-integration/README.md`](../examples/claude-integration/README.md)

---

## 📧 反馈与支持

如有问题或建议，欢迎联系：
- **Email**: 3248838607@qq.com
- **GitHub**: https://github.com/jeesoul/jeesoul-ai-model
- **Issues**: https://github.com/jeesoul/jeesoul-ai-model/issues

---

**Happy Coding! 🎉**

