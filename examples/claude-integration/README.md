# Claude AI 集成示例

本示例展示如何使用 jeesoul-ai-model 框架集成 Anthropic Claude AI 模型。

> **v1.0.9-GA 更新**：扩展自定义模型现在更简单！无需修改 `AiProperties.java`，只需实现 `ModelConfig` 接口即可。

## 📋 文件说明

- **`ClaudeService.java`** - Claude AI 服务实现（完整代码）
- **`ClaudeController.java`** - REST API 控制器示例（包含8个接口）
- **`application.yml`** - 配置文件示例

## 🎯 示例功能

1. ✅ 同步对话 (`/api/claude/chat`)
2. ✅ 流式对话 (`/api/claude/stream-chat`)
3. ✅ 多模型对比 (`/api/claude/compare`)
4. ✅ 健康检查 (`/api/claude/health`)
5. ✅ System Prompt 支持
6. ✅ 参数自动校验
7. ✅ 完整的异常处理
8. ✅ 详细的日志输出

## 🚀 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.jeesoul</groupId>
    <artifactId>jeesoul-ai-model</artifactId>
    <version>1.0.9</version>  <!-- 或 1.0.9-GA -->
</dependency>
```

### 2. 创建配置类（v1.0.9-GA 推荐方式）

**v1.0.9-GA 新特性**：无需修改 `AiProperties.java`，创建独立的配置类即可！

```java
import com.jeesoul.ai.model.config.BaseModelConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Claude 模型配置类
 * 继承 BaseModelConfig，自动实现 ModelConfig 接口
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "ai.claude")
public class ClaudeModelProperties extends BaseModelConfig {
    // 可选：添加额外配置
    // private String region;
    // private String version;
}
```

### 3. 创建 Service（v1.0.9-GA 推荐方式）

将 `ClaudeService.java` 复制到你的项目中，并使用新的构造函数：

```java
@Slf4j
@AiModelService(
    modelName = "claude",
    serviceName = "claudeService",
    description = "Anthropic Claude AI 模型服务"
)
public class ClaudeService extends AbstractAiService {
    
    /**
     * 构造函数（v1.0.9-GA 推荐方式）
     * 只需传入 ModelConfig，工具类已静态化，无需注入
     */
    public ClaudeService(ClaudeModelProperties modelConfig) {
        super(modelConfig);
    }
    
    // ... 其他方法保持不变
}
```

**注意**：代码中使用工具类时，改为静态调用：
```java
// v1.0.9-GA：使用静态方法
HttpUtils.post(url, headers, body, responseType, config);
StreamHttpUtils.postStream(url, body, config);
```

### 4. 配置API密钥

```yaml
ai:
  claude:
    api-key: sk-ant-xxxxx
    endpoint: https://api.anthropic.com/v1/messages
    temperature: 0.7
    top-p: 0.9
    max-tokens: 2000
    model: claude-3-opus-20240229
```

**其他配置方式：**
- 环境变量：`export CLAUDE_API_KEY=sk-ant-xxxxx`
- 系统属性：`java -Dai.claude.api-key=sk-ant-xxxxx -jar your-app.jar`

### 5. 使用Claude模型

```java
@RestController
public class MyController {
    
    @PostMapping("/chat")
    public ModelResponseVO chat(@RequestBody String prompt) {
        // 创建Claude服务（v1.0.9-GA：支持自定义模型）
        AiService claudeService = FactoryModelService.create("claude");
        
        // 构建请求
        ModelRequestVO request = new ModelRequestVO()
                .setModelName("claude")
                .setModel("claude-3-opus-20240229")
                .setPrompt(prompt)
                .setSystemPrompt("你是一个专业的AI助手");
        
        // 调用Claude
        ModelResponseVO response = claudeService.httpChat(request);
        
        // v1.0.9-GA：获取Token统计和模型信息
        System.out.println("模型: " + response.getModelProvider() + " - " + response.getModelName());
        if (response.getUsage() != null) {
            System.out.println("Token使用: " + response.getUsage().getTotalTokens());
        }
        
        return response;
    }
}
```

## 🏗️ v1.0.9-GA 架构改进

### 新方式 vs 旧方式对比

**旧方式（v1.0.8）：**
```java
// 需要修改 AiProperties.java
public ClaudeService(AiProperties aiProperties, 
                    HttpUtils aiHttpUtils, 
                    StreamHttpUtils streamHttpUtils) {
    super(aiProperties, aiHttpUtils, streamHttpUtils);
}
```

**新方式（v1.0.9-GA 推荐）：**
```java
// 无需修改框架代码，只需创建配置类
public ClaudeService(ClaudeModelProperties modelConfig) {
    super(modelConfig);  // 工具类已静态化，无需注入
}
```

**优势：**
- ✅ **零侵入**：无需修改 `AiProperties.java`
- ✅ **易扩展**：每个模型有独立的配置类
- ✅ **更简洁**：无需注入工具类
- ✅ **符合开闭原则**：对扩展开放，对修改关闭

## 📝 支持的Claude模型

- `claude-3-opus-20240229` - 最强大的模型
- `claude-3-sonnet-20240229` - 平衡性能和成本
- `claude-3-haiku-20240307` - 最快速和经济的模型
- `claude-2.1` - 上一代模型
- `claude-2.0` - 上一代模型

## 🔧 API说明

### 同步对话

```bash
curl -X POST http://localhost:8080/api/claude/chat \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "解释一下量子计算的基本原理",
    "systemPrompt": "你是一个物理学教授"
  }'
```

### 流式对话

```bash
curl -X POST http://localhost:8080/api/claude/stream-chat \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "写一篇关于人工智能的文章"
  }'
```

### 多模型对比

```bash
curl -X POST http://localhost:8080/api/claude/compare \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "如何学习编程？"
  }'
```

## 🎯 特性

- ✅ 支持 Claude 3 全系列模型
- ✅ 支持 system prompt
- ✅ 完整的 Token 使用统计（v1.0.9+）
- ✅ 返回模型提供商和版本信息（v1.0.9+）
- ✅ 自动参数校验
- ✅ 统一的异常处理
- ✅ 详细的日志输出
- ✅ 符合框架规范

## 📊 v1.0.9-GA 新增功能

### Token 统计
```java
ModelResponseVO response = claudeService.httpChat(request);
TokenUsageVO usage = response.getUsage();
System.out.println("输入Token: " + usage.getPromptTokens());
System.out.println("输出Token: " + usage.getCompletionTokens());
System.out.println("总Token: " + usage.getTotalTokens());
```

### 模型信息
```java
System.out.println("提供商: " + response.getModelProvider());  // claude
System.out.println("模型版本: " + response.getModelName());     // claude-3-opus-20240229
```

### 架构改进
- ✅ **ModelConfig 接口**：解耦配置与实现
- ✅ **工具类静态化**：`HttpUtils` 和 `StreamHttpUtils` 改为静态方法
- ✅ **自定义模型扩展**：无需修改框架核心代码

## 📚 Claude API 文档

- [Claude API 官方文档](https://docs.anthropic.com/claude/reference/getting-started-with-the-api)
- [Claude 3 模型对比](https://www.anthropic.com/claude)

## 💡 注意事项

1. **API密钥安全**：不要在代码中硬编码API密钥，使用环境变量或配置中心
2. **速率限制**：Claude API有速率限制，注意控制请求频率
3. **Token计费**：Claude按Token计费，注意控制maxTokens参数
4. **模型选择**：根据需求选择合适的模型，Opus最强但最贵，Haiku最快最便宜
5. **v1.0.9-GA**：推荐使用新的 `ModelConfig` 方式扩展，无需修改框架代码

## 🔍 故障排查

### 问题1：Claude服务未注册

**错误信息：**
```
IllegalArgumentException: 未注册的模型: claude
```

**解决方案：**
1. 确保 `ClaudeService` 类上有 `@AiModelService` 注解
2. 确保被Spring扫描到（在启动类或配置类所在包下）
3. v1.0.9-GA：`FactoryModelService.create()` 现在支持自定义模型，检查模型名称是否正确

### 问题2：API密钥无效

**错误信息：**
```
AiException: Claude调用失败
```

**解决方案：**
1. 检查API密钥是否正确
2. 检查API密钥是否有效
3. 检查网络连接

### 问题3：响应为空

**可能原因：**
- prompt为空
- maxTokens设置太小
- API返回错误

**解决方案：**
查看日志输出，检查具体错误信息。

## 📧 联系支持

如有问题，请联系：
- Email: 3248838607@qq.com
- GitHub: https://github.com/jeesoul/jeesoul-ai-model
