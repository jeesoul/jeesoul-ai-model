# Claude AI 集成示例

本示例展示如何使用 jeesoul-ai-model 框架集成 Anthropic Claude AI 模型。

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
    <version>1.0.7</version>
</dependency>
```

### 2. 复制代码

将 `ClaudeService.java` 复制到你的项目中，例如：
```
src/main/java/com/yourcompany/ai/service/ClaudeService.java
```

### 3. 配置API密钥

方式一：环境变量
```bash
export CLAUDE_API_KEY=sk-ant-xxxxx
```

方式二：系统属性
```bash
java -Dai.claude.apiKey=sk-ant-xxxxx -jar your-app.jar
```

方式三：配置文件（需要扩展AiProperties）
```yaml
ai:
  claude:
    apiKey: sk-ant-xxxxx
```

### 4. 使用Claude模型

```java
@RestController
public class MyController {
    
    @PostMapping("/chat")
    public ModelResponseVO chat(@RequestBody String prompt) {
        // 创建Claude服务
        AiService claudeService = FactoryModelService.create("claude");
        
        // 构建请求
        ModelRequestVO request = new ModelRequestVO()
                .setModelName("claude")
                .setModel("claude-3-opus-20240229")
                .setPrompt(prompt)
                .setSystemPrompt("你是一个专业的AI助手");
        
        // 调用Claude
        return claudeService.httpChat(request);
    }
}
```

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
- ✅ 自动参数校验
- ✅ 统一的异常处理
- ✅ 详细的日志输出
- ✅ 符合框架规范

## 📚 Claude API 文档

- [Claude API 官方文档](https://docs.anthropic.com/claude/reference/getting-started-with-the-api)
- [Claude 3 模型对比](https://www.anthropic.com/claude)

## 💡 注意事项

1. **API密钥安全**：不要在代码中硬编码API密钥
2. **速率限制**：Claude API有速率限制，注意控制请求频率
3. **Token计费**：Claude按Token计费，注意控制maxTokens参数
4. **模型选择**：根据需求选择合适的模型，Opus最强但最贵，Haiku最快最便宜

## 🔍 故障排查

### 问题1：Claude服务未注册

**错误信息：**
```
IllegalArgumentException: 未注册的模型: claude
```

**解决方案：**
确保 `ClaudeService` 类上有 `@AiModelService` 注解，并且被Spring扫描到。

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
