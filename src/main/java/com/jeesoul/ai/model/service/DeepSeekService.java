package com.jeesoul.ai.model.service;

import com.jeesoul.ai.model.config.AiProperties;
import com.jeesoul.ai.model.constant.AiModel;
import com.jeesoul.ai.model.constant.AiRole;
import com.jeesoul.ai.model.entity.ResultContent;
import com.jeesoul.ai.model.exception.AiException;
import com.jeesoul.ai.model.request.HttpDeepSeekChatRequest;
import com.jeesoul.ai.model.response.HttpDeepSeekChatResponse;
import com.jeesoul.ai.model.response.StreamDeepSeekResponse;
import com.jeesoul.ai.model.util.HttpUtils;
import com.jeesoul.ai.model.util.StreamHttpUtils;
import com.jeesoul.ai.model.vo.ModelRequestVO;
import com.jeesoul.ai.model.vo.ModelResponseVO;
import com.jeesoul.ai.model.vo.TokenUsageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * DeepSeek 大模型服务实现类。
 * 实现与 DeepSeek AI 的对接逻辑，包括HTTP和流式对话功能。
 *
 * @author dxy
 * @date 2025-06-10
 */
@Slf4j
public class DeepSeekService extends AbstractAiService {

    public DeepSeekService(AiProperties aiProperties, HttpUtils aiHttpUtils, StreamHttpUtils streamHttpUtils) {
        super(aiProperties, aiHttpUtils, streamHttpUtils);
    }

    @Override
    protected String getModelName() {
        return AiModel.DEEP_SEEK.getModelName();
    }

    @Override
    protected boolean supportSystemPrompt() {
        return true;
    }

    @Override
    protected boolean supportThinking() {
        return true;  // DeepSeek支持思考模式
    }

    @Override
    public ModelResponseVO httpChat(ModelRequestVO request) throws AiException {
        // 参数校验
        validateRequest(request);
        // 警告不支持的功能
        warnUnsupportedFeatures(request);

        try {
            HttpDeepSeekChatRequest chatRequest = buildChatRequest(request, false);
            HttpDeepSeekChatResponse response = sendHttpRequest(chatRequest);

            if (CollectionUtils.isEmpty(response.getChoices())) {
                return ModelResponseVO.of("", AiModel.DEEP_SEEK.getModelName(), 
                        chatRequest.getModel());
            }
            
            String result = response.getChoices().get(0).getMessage().getContent();
            
            // 提取usage信息
            TokenUsageVO usage = extractUsage(response);
            
            // 提取思考内容（reasoning_content）
            String thinkingContent = response.getChoices().get(0).getMessage().getReasoningContent();
            
            // 返回完整的响应信息
            return ModelResponseVO.of(
                    result,
                    thinkingContent,
                    AiModel.DEEP_SEEK.getModelName(),
                    chatRequest.getModel(),
                    usage
            );
        } catch (Exception e) {
            log.error("[DeepSeek] 调用失败: {}", e.getMessage(), e);
            throw new AiException("DeepSeek调用失败", e);
        }
    }

    @Override
    public Flux<ModelResponseVO> streamChat(ModelRequestVO request) throws AiException {
        // 获取实际使用的模型版本
        String actualModel = getModel(request, aiProperties.getDeepSeek().getModel());
        
        return sendStreamRequest(request)
                .filter(content -> {
                    // 过滤掉content为空的chunk（但保留只有usage的chunk）
                    String text = content.getContent();
                    return (text != null && !text.isEmpty()) || content.getUsage() != null;
                })
                .map(content -> {
                    // 构建完整的响应对象，包含usage信息（如果有）
                    return ModelResponseVO.of(
                            content.getContent(),  // content字段已包含thinking或正常内容
                            content.getThinkingContent(),
                            AiModel.DEEP_SEEK.getModelName(),
                            actualModel,
                            content.getUsage()  // 只有最后一个chunk有usage
                    );
                });
    }

    @Override
    public Flux<String> streamChatStr(ModelRequestVO request) throws AiException {
        return sendStreamRequest(request)
                .filter(content -> {
                    String text = content.getContent();
                    return text != null && !text.isEmpty();  // 过滤掉null和空字符串
                })
                .map(ResultContent::getContent);
    }

    @Override
    public String httpChatRaw(ModelRequestVO request) throws AiException {
        // 参数校验
        validateRequest(request);
        // 警告不支持的功能
        warnUnsupportedFeatures(request);

        try {
            HttpDeepSeekChatRequest chatRequest = buildChatRequest(request, false);
            logRequestParams(chatRequest);
            HttpUtils.HttpConfig config = createHttpConfig();
            return aiHttpUtils.postRaw(
                    aiProperties.getDeepSeek().getEndpoint(),
                    new HashMap<>(),
                    chatRequest,
                    config
            );
        } catch (Exception e) {
            log.error("[DeepSeek] 调用失败: {}", e.getMessage(), e);
            throw new AiException("DeepSeek调用失败", e);
        }
    }

    @Override
    public Flux<String> streamChatRaw(ModelRequestVO request) throws AiException {
        HttpDeepSeekChatRequest chatRequest = buildChatRequest(request, true);
        logRequestParams(chatRequest);
        StreamHttpUtils.StreamHttpConfig<HttpDeepSeekChatRequest, String> config = StreamHttpUtils.StreamHttpConfig
                .<HttpDeepSeekChatRequest, String>builder()
                .apiKey(aiProperties.getDeepSeek().getApiKey())
                .requestInterceptor(r -> r.header("X-Request-ID", UUID.randomUUID().toString()))
                .build();
        return streamHttpUtils.postStreamRaw(
                aiProperties.getDeepSeek().getEndpoint(),
                chatRequest,
                config
        );
    }

    /**
     * 构建聊天请求对象
     *
     * @param request  请求参数
     * @param isStream 是否为流式请求
     * @return 构建好的请求对象
     */
    private HttpDeepSeekChatRequest buildChatRequest(ModelRequestVO request, boolean isStream) {
        HttpDeepSeekChatRequest chatRequest = new HttpDeepSeekChatRequest();
        chatRequest.setModel(getModel(request, aiProperties.getDeepSeek().getModel()));
        chatRequest.setMessages(buildMessages(request));
        chatRequest.setStream(isStream);
        chatRequest.setTemperature(getTemperature(request, aiProperties.getDeepSeek().getTemperature()));
        chatRequest.setTopP(getTopP(request, aiProperties.getDeepSeek().getTopP()));
        chatRequest.setMaxTokens(getMaxTokens(request, aiProperties.getDeepSeek().getMaxTokens()));
        
        // 设置思考模式（DeepSeek支持）
        // 文档：https://api-docs.deepseek.com/zh-cn/guides/thinking_mode
        if (request.isEnableThinking()) {
            chatRequest.setThinking(HttpDeepSeekChatRequest.ThinkingConfig.enabled());
        } else {
            chatRequest.setThinking(HttpDeepSeekChatRequest.ThinkingConfig.disabled());
        }
        
        // 使用基类的参数合并方法
        mergeParamsToRequest(chatRequest, request.getParams());
        return chatRequest;
    }

    /**
     * 构建消息列表
     *
     * @param request 请求参数
     * @return 消息列表
     */
    private List<HttpDeepSeekChatRequest.Message> buildMessages(ModelRequestVO request) {
        List<HttpDeepSeekChatRequest.Message> messages = new ArrayList<>();
        
        // 如果设置了 systemPrompt，系统消息放在最前面
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
            messages.add(createMessage(AiRole.SYSTEM, request.getSystemPrompt()));
        }
        
        // 如果请求中提供了消息列表，添加消息列表（跳过其中的 system 消息，因为已经在最前面添加了）
        if (!CollectionUtils.isEmpty(request.getMessages())) {
            for (ModelRequestVO.Message voMessage : request.getMessages()) {
                HttpDeepSeekChatRequest.Message message = new HttpDeepSeekChatRequest.Message();
                message.setRole(voMessage.getRole());
                message.setContent(voMessage.getContent());
                messages.add(message);
            }
        }
        
        // 否则使用原来的逻辑：添加用户消息
        messages.add(createMessage(AiRole.USER, request.getPrompt()));
        return messages;
    }

    /**
     * 创建消息对象
     *
     * @param role    角色
     * @param content 内容
     * @return 消息对象
     */
    private HttpDeepSeekChatRequest.Message createMessage(AiRole role, String content) {
        HttpDeepSeekChatRequest.Message message = new HttpDeepSeekChatRequest.Message();
        message.setRole(role.getValue());
        message.setContent(content);
        return message;
    }

    /**
     * 发送HTTP请求
     *
     * @param chatRequest 聊天请求对象
     * @return HTTP响应
     * @throws IOException 请求异常
     */
    private HttpDeepSeekChatResponse sendHttpRequest(HttpDeepSeekChatRequest chatRequest) throws IOException {
        logRequestParams(chatRequest);
        HttpUtils.HttpConfig config = createHttpConfig();
        return aiHttpUtils.post(
                aiProperties.getDeepSeek().getEndpoint(),
                new HashMap<>(),
                chatRequest,
                HttpDeepSeekChatResponse.class,
                config
        );
    }

    /**
     * 发送流式请求
     *
     * @param request 请求参数
     * @return 流式响应
     */
    private Flux<ResultContent> sendStreamRequest(ModelRequestVO request) {
        HttpDeepSeekChatRequest chatRequest = buildChatRequest(request, true);
        logRequestParams(chatRequest);
        StreamHttpUtils.StreamHttpConfig<HttpDeepSeekChatRequest, ResultContent> config = createStreamConfig();
        return streamHttpUtils.postStream(
                aiProperties.getDeepSeek().getEndpoint(),
                chatRequest,
                config
        );
    }

    /**
     * 创建HTTP配置
     *
     * @return HTTP配置对象
     */
    private HttpUtils.HttpConfig createHttpConfig() {
        return HttpUtils.HttpConfig.builder()
                .apiKey(aiProperties.getDeepSeek().getApiKey())
                .requestInterceptor(r -> log.debug("[DeepSeek] 请求URL: {}", r.getUrl()))
                .responseInterceptor(response ->
                        log.debug("[DeepSeek] 响应状态: {}, 响应内容: {}",
                                response.getStatus(), truncateForLog(response.body(), 200)))
                .build();
    }

    /**
     * 创建流式配置
     *
     * @return 流式配置对象
     */
    private StreamHttpUtils.StreamHttpConfig<HttpDeepSeekChatRequest, ResultContent> createStreamConfig() {
        return StreamHttpUtils.StreamHttpConfig.<HttpDeepSeekChatRequest, ResultContent>builder()
                .apiKey(aiProperties.getDeepSeek().getApiKey())
                .requestInterceptor(r -> r.header("X-Request-ID", UUID.randomUUID().toString()))
                .responseProcessor(new StreamDeepSeekResponse())
                .build();
    }

    /**
     * 从响应中提取Token使用统计
     *
     * @param response HTTP响应对象
     * @return TokenUsageVO对象，如果响应中没有usage信息则返回null
     */
    private TokenUsageVO extractUsage(HttpDeepSeekChatResponse response) {
        if (response == null || response.getUsage() == null) {
            return null;
        }
        
        HttpDeepSeekChatResponse.Usage usage = response.getUsage();
        
        // DeepSeek使用标准的promptTokens和completionTokens字段
        return TokenUsageVO.of(
                usage.getPromptTokens(),
                usage.getCompletionTokens(),
                usage.getTotalTokens()
        );
    }

}
