package com.example.ai.service;

import com.jeesoul.ai.model.annotation.AiModelService;
import com.jeesoul.ai.model.config.AiProperties;
import com.jeesoul.ai.model.constant.AiRole;
import com.jeesoul.ai.model.exception.AiException;
import com.jeesoul.ai.model.response.HttpBaseResponse;
import com.jeesoul.ai.model.service.AbstractAiService;
import com.jeesoul.ai.model.util.HttpUtils;
import com.jeesoul.ai.model.util.StreamHttpUtils;
import com.jeesoul.ai.model.vo.ModelRequestVO;
import com.jeesoul.ai.model.vo.ModelResponseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.*;

/**
 * Claude AI 模型服务实现
 * 
 * 使用说明：
 * 1. 将此文件复制到你的项目中
 * 2. 配置 API 密钥（环境变量或系统属性）
 * 3. 使用 FactoryModelService.create("claude") 创建服务
 * 
 * @author jeesoul
 * @date 2025-10-18
 */
@Slf4j
@AiModelService(
    modelName = "claude",
    serviceName = "claudeService",
    description = "Anthropic Claude AI 模型服务"
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
        return true;  // Claude 支持 system prompt
    }

    @Override
    protected boolean supportThinking() {
        return false;  // Claude 不支持 thinking 模式
    }

    @Override
    public ModelResponseVO httpChat(ModelRequestVO request) throws AiException {
        // 参数校验
        validateRequest(request);
        // 警告不支持的功能
        warnUnsupportedFeatures(request);
        
        try {
            // 1. 构建请求
            ClaudeChatRequest claudeRequest = buildClaudeRequest(request);
            
            if (log.isDebugEnabled()) {
                log.debug("[Claude] 请求URL: {}, 请求体: {}", 
                         getEndpoint(), truncateForLog(com.jeesoul.ai.model.util.JsonUtils.toJson(claudeRequest), 500));
            }
            
            // 2. 发送HTTP请求
            ClaudeChatResponse response = sendHttpRequest(claudeRequest);
            
            // 3. 解析响应
            if (response.getContent() == null || response.getContent().isEmpty()) {
                return new ModelResponseVO("", "claude");
            }
            
            String result = response.getContent().get(0).getText();
            return new ModelResponseVO(result, "claude");
        } catch (Exception e) {
            log.error("[Claude] 调用失败: {}", e.getMessage(), e);
            throw new AiException("Claude调用失败", e);
        }
    }

    @Override
    public Flux<ModelResponseVO> streamChat(ModelRequestVO request) throws AiException {
        // Claude 流式调用实现（示例返回空流）
        log.warn("[Claude] 流式调用暂未实现，返回同步结果");
        ModelResponseVO response = httpChat(request);
        return Flux.just(response);
    }

    @Override
    public Flux<String> streamChatStr(ModelRequestVO request) throws AiException {
        // Claude 流式文本调用实现
        log.warn("[Claude] 流式调用暂未实现，返回同步结果");
        ModelResponseVO response = httpChat(request);
        return Flux.just(response.getResult());
    }

    /**
     * 构建 Claude 请求对象
     */
    private ClaudeChatRequest buildClaudeRequest(ModelRequestVO request) {
        ClaudeChatRequest claudeRequest = new ClaudeChatRequest();
        claudeRequest.setModel(request.getModel());
        claudeRequest.setMessages(buildMessages(request));
        claudeRequest.setMaxTokens(request.getMaxTokens() != null ? request.getMaxTokens() : 4096);
        
        if (request.getTemperature() != null) {
            claudeRequest.setTemperature(request.getTemperature());
        }
        
        if (request.getTopP() != null) {
            claudeRequest.setTopP(request.getTopP());
        }
        
        // 设置 system prompt
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
            claudeRequest.setSystem(request.getSystemPrompt());
        }
        
        // 使用基类的参数合并方法
        mergeParamsToRequest(claudeRequest, request.getParams());
        
        return claudeRequest;
    }

    /**
     * 构建消息列表
     */
    private List<ClaudeMessage> buildMessages(ModelRequestVO request) {
        List<ClaudeMessage> messages = new ArrayList<>();
        
        // 添加用户消息
        ClaudeMessage userMsg = new ClaudeMessage();
        userMsg.setRole(AiRole.USER.getValue());
        userMsg.setContent(request.getPrompt());
        messages.add(userMsg);
        
        return messages;
    }

    /**
     * 发送 HTTP 请求
     */
    private ClaudeChatResponse sendHttpRequest(ClaudeChatRequest request) throws Exception {
        HttpUtils.HttpConfig config = HttpUtils.HttpConfig.builder()
                .apiKey(getApiKey())
                .requestInterceptor(r -> {
                    // Claude API 需要特殊的版本头
                    r.header("anthropic-version", "2023-06-01");
                })
                .responseInterceptor(response ->
                        log.debug("[Claude] 响应状态: {}, 响应内容: {}", 
                                 response.getStatus(), truncateForLog(response.body(), 200)))
                .build();
        
        return aiHttpUtils.post(
                getEndpoint(),
                new HashMap<>(),
                request,
                ClaudeChatResponse.class,
                config
        );
    }

    /**
     * 获取 API 密钥
     * 优先级: 环境变量 > 系统属性
     */
    private String getApiKey() {
        String apiKey = System.getenv("CLAUDE_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = System.getProperty("ai.claude.apiKey");
        }
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Claude API密钥未配置，请设置环境变量 CLAUDE_API_KEY 或系统属性 ai.claude.apiKey");
        }
        return apiKey;
    }

    /**
     * 获取 API 端点
     */
    private String getEndpoint() {
        String endpoint = System.getProperty("ai.claude.endpoint");
        return endpoint != null ? endpoint : "https://api.anthropic.com/v1/messages";
    }

    // ==================== Claude API 请求/响应对象 ====================

    /**
     * Claude 聊天请求
     */
    @Data
    public static class ClaudeChatRequest {
        private String model;
        private List<ClaudeMessage> messages;
        private Integer maxTokens;
        private String system;
        private Double temperature;
        private Double topP;
    }

    /**
     * Claude 消息对象
     */
    @Data
    public static class ClaudeMessage {
        private String role;
        private String content;
    }

    /**
     * Claude 聊天响应
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ClaudeChatResponse extends HttpBaseResponse {
        private String id;
        private String type;
        private String role;
        private List<ContentBlock> content;
        private String model;
        private String stopReason;
        private Usage usage;

        @Data
        public static class ContentBlock {
            private String type;
            private String text;
        }

        @Data
        public static class Usage {
            private Integer inputTokens;
            private Integer outputTokens;
        }
    }
}
