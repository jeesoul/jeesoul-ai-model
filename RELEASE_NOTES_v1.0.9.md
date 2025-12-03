# Release Notes - v1.0.9

> **发布日期**: 2024年12月
> **版本类型**: 重要功能更新

---

## 🎉 主要更新

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

### 1. 响应对象重构
- `thinking` 字段从 String 改为 Boolean，表示是否使用思考模式
- 新增 `thinkingContent` 字段存储实际的思考内容
- 保持向后兼容，旧字段标记为 @Deprecated

### 2. 流式响应增强
- 流式接口统一返回 `ResultContent` 对象
- 支持在流中携带 Token 统计和模型信息
- 自动过滤空 chunk，提升输出质量

### 3. 思考模式统一
- 所有支持思考的模型使用统一的 API
- 支持显式禁用思考模式（`setEnableThinking(false)`）
- 流式输出支持区分思考过程和最终答案

### 4. 空内容过滤
- 自动过滤流式响应中的空字符串
- 保留包含 Token 统计的最后一个 chunk
- 避免输出空白行

---

## 🐛 Bug 修复

### 1. 流式响应问题
- ✅ 修复流式响应输出空白行的问题
- ✅ 修复 `streamChatStr()` 中的 NPE 问题
- ✅ 修复空 chunk 导致的 Flux 错误

### 2. 类型转换问题
- ✅ 修复 QWen/QWenVL 的 Usage 类型转换错误
- ✅ 修复 Jackson 反序列化继承类的问题

### 3. 思考模式问题
- ✅ 修复 Spark/QWen 的 reasoning_content 提取错误
- ✅ 修复思考模式禁用不生效的问题
- ✅ 修复 DeepSeek 思考模式未启用的问题

### 4. 内容优先级问题
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
v1.0.9 **完全向后兼容** v1.0.8，现有代码无需修改即可升级。

### 废弃字段
以下字段已标记为 @Deprecated，但仍可使用：
- `ModelResponseVO.model` → 使用 `modelProvider` 和 `modelName` 替代

### 推荐迁移
虽然不强制，但建议逐步迁移到新字段：
```java
// 旧代码（仍然可用）
String model = response.getModel();

// 新代码（推荐）
String provider = response.getModelProvider();
String modelVersion = response.getModelName();
TokenUsageVO usage = response.getUsage();
```

---

## 📚 文档更新

### 主文档
- ✅ README.md - 完整更新，新增 6 个使用示例
- ✅ 新增常见问题 FAQ 章节
- ✅ 新增升级指南章节
- ✅ 更新参数说明和 API 文档

### 示例文档
- ✅ examples/claude-integration - 更新版本号和新特性说明
- ✅ examples/multimodal-vision - 新增 Token 统计示例

### 架构文档
- ✅ img/layered-architecture.md - 新增 DouBao 和 QWenVL
- ✅ img/overall-architecture.md - 完整架构图更新

---

## 🚀 升级步骤

### 1. 更新依赖
```xml
<dependency>
    <groupId>com.jeesoul</groupId>
    <artifactId>jeesoul-ai-model</artifactId>
    <version>1.0.9</version>
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

---

## 📧 反馈与支持

如有问题或建议，欢迎联系：
- **Email**: 3248838607@qq.com
- **GitHub**: https://github.com/jeesoul/jeesoul-ai-model
- **Issues**: https://github.com/jeesoul/jeesoul-ai-model/issues

---

## 🙏 致谢

感谢所有使用和支持本项目的开发者！

本版本重点解决了多个用户反馈的问题，并新增了豆包模型支持和完整的 Token 统计功能。期待大家的持续反馈和贡献！

---

**Happy Coding! 🎉**

