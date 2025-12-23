package com.jeesoul.ai.model.service;

import com.jeesoul.ai.model.config.AiProperties;
import com.jeesoul.ai.model.config.ModelConfig;
import com.jeesoul.ai.model.constant.AiModel;
import com.jeesoul.ai.model.constant.AiRole;
import com.jeesoul.ai.model.entity.ResultContent;
import com.jeesoul.ai.model.exception.AiException;
import com.jeesoul.ai.model.request.HttpDouBaoChatRequest;
import com.jeesoul.ai.model.response.HttpDouBaoChatResponse;
import com.jeesoul.ai.model.response.StreamDouBaoResponse;
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
 * 豆包（火山方舟）大模型服务实现类
 * 实现与豆包AI的对接逻辑，包括HTTP和流式对话功能
 * 注意事项：
 * 1. 豆包API与OpenAI兼容
 * 2. 流式响应的usage字段始终为null，无法获取token统计
 * 3. 如需token统计，请使用同步接口httpChat()
 *
 * @author dxy
 * @date 2025-12-03
 */
@Slf4j
public class DouBaoService extends AbstractAiService {

    /**
     * 构造函数（推荐使用）
     *
     * @param modelConfig 模型配置
     */
    public DouBaoService(ModelConfig modelConfig) {
        super(modelConfig);
    }

    /**
     * 构造函数（向后兼容 - 支持 AiProperties + 工具类注入）
     * 请使用 {@link #DouBaoService(ModelConfig)} 替代
     * HttpUtils 和 StreamHttpUtils 已改为静态工具类，无需注入，工具类参数将被忽略
     */
    @Deprecated
    public DouBaoService(AiProperties aiProperties, HttpUtils aiHttpUtils, StreamHttpUtils streamHttpUtils) {
        super(aiProperties.getDouBao());
    }

    @Override
    protected String getModelName() {
        return AiModel.DOUBAO.getModelName();
    }

    @Override
    protected boolean supportSystemPrompt() {
        return true;
    }

    @Override
    protected boolean supportThinking() {
        // 豆包支持深度思考模式（如doubao-seed-1.6-thinking）
        return true;
    }

    @Override
    public ModelResponseVO httpChat(ModelRequestVO request) throws AiException {
        // 参数校验
        validateRequest(request);
        // 警告不支持的功能
        warnUnsupportedFeatures(request);

        try {
            HttpDouBaoChatRequest chatRequest = buildChatRequest(request, false);
            HttpDouBaoChatResponse response = sendHttpRequest(chatRequest);

            if (CollectionUtils.isEmpty(response.getChoices())) {
                return ModelResponseVO.of("", AiModel.DOUBAO.getModelName(),
                            chatRequest.getModel());
            }

            String result = response.getChoices().get(0).getMessage().getContent();

            // 提取usage信息
            TokenUsageVO usage = extractUsage(response);

            // 提取思考内容（如果有reasoning_tokens，说明使用了思考模式）
            String thinkingContent = extractThinkingContent(response);

            // 返回完整的响应信息
            return ModelResponseVO.of(
                        result,
                        thinkingContent,
                        AiModel.DOUBAO.getModelName(),
                        chatRequest.getModel(),
                        usage
            );
        } catch (Exception e) {
            log.error("[DouBao] 调用失败: {}", e.getMessage(), e);
            throw new AiException("DouBao调用失败", e);
        }
    }

    @Override
    public Flux<ModelResponseVO> streamChat(ModelRequestVO request) throws AiException {
        // 获取实际使用的模型版本
        String actualModel = getModel(request, modelConfig.getModel());

        return sendStreamRequest(request)
                    .filter(content -> {
                        // 过滤掉content为空的chunk
                        // 豆包不提供usage，所以只检查content
                        String text = content.getContent();
                        return text != null && !text.isEmpty();
                    })
                    .map(content -> {
                        // 构建完整的响应对象
                        // ⚠️ 注意：豆包的流式响应不提供usage统计（API限制）
                        // 如需token统计，请使用同步接口 httpChat()
                        return ModelResponseVO.of(
                                    content.getContent(),
                                    content.getThinkingContent(),
                                    AiModel.DOUBAO.getModelName(),
                                    actualModel,
                                    null
                        );
                    });
    }

    @Override
    public Flux<String> streamChatStr(ModelRequestVO request) throws AiException {
        return sendStreamRequest(request)
                    .filter(content -> {
                        String text = content.getContent();
                        // 过滤掉null和空字符串
                        return text != null && !text.isEmpty();
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
            HttpDouBaoChatRequest chatRequest = buildChatRequest(request, false);
            logRequestParams(chatRequest);
            HttpUtils.HttpConfig config = createHttpConfig();
            return HttpUtils.postRaw(
                        getEndpoint(),
                        new HashMap<>(),
                        chatRequest,
                        config
            );
        } catch (Exception e) {
            log.error("[DouBao] 调用失败: {}", e.getMessage(), e);
            throw new AiException("DouBao调用失败", e);
        }
    }

    @Override
    public Flux<String> streamChatRaw(ModelRequestVO request) throws AiException {
        HttpDouBaoChatRequest chatRequest = buildChatRequest(request, true);
        logRequestParams(chatRequest);
        StreamHttpUtils.StreamHttpConfig<HttpDouBaoChatRequest, String> config =
                    StreamHttpUtils.StreamHttpConfig.<HttpDouBaoChatRequest, String>builder()
                                .apiKey(getApiKey())
                                .requestInterceptor(r -> r.header("X-Request-ID", UUID.randomUUID().toString()))
                                .build();
        return StreamHttpUtils.postStreamRaw(
                    getEndpoint(),
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
    private HttpDouBaoChatRequest buildChatRequest(ModelRequestVO request, boolean isStream) {
        HttpDouBaoChatRequest chatRequest = new HttpDouBaoChatRequest();

        // 设置深度思考配置
        if (request.isEnableThinking()) {
            chatRequest.setThinking(HttpDouBaoChatRequest.ThinkingConfig.enabled());
            log.debug("[DouBao] 启用深度思考模式");
        } else {
            chatRequest.setThinking(HttpDouBaoChatRequest.ThinkingConfig.disabled());
            log.debug("[DouBao] 禁用深度思考模式");
        }

        chatRequest.setModel(getModel(request, modelConfig.getModel()));
        chatRequest.setMessages(buildMessages(request));
        chatRequest.setStream(isStream);
        chatRequest.setTemperature(getTemperature(request, modelConfig.getTemperature()));
        chatRequest.setTopP(getTopP(request, modelConfig.getTopP()));
        chatRequest.setMaxTokens(getMaxTokens(request, modelConfig.getMaxTokens()));
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
    private List<HttpDouBaoChatRequest.Message> buildMessages(ModelRequestVO request) {
        List<HttpDouBaoChatRequest.Message> messages = new ArrayList<>();

        // 如果设置了 systemPrompt，系统消息放在最前面
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
            messages.add(createMessage(AiRole.SYSTEM, request.getSystemPrompt()));
        }

        // 如果请求中提供了消息列表，添加消息列表
        if (!CollectionUtils.isEmpty(request.getMessages())) {
            for (ModelRequestVO.Message voMessage : request.getMessages()) {
                HttpDouBaoChatRequest.Message message = new HttpDouBaoChatRequest.Message();
                message.setRole(voMessage.getRole());
                // 直接使用字符串内容
                message.setContent(voMessage.getContent());
                messages.add(message);
            }
        } else {
            // 否则使用原来的逻辑：添加用户消息
            messages.add(createMessage(AiRole.USER, request.getPrompt()));
        }

        return messages;
    }

    /**
     * 创建消息对象
     *
     * @param role    角色
     * @param content 内容
     * @return 消息对象
     */
    private HttpDouBaoChatRequest.Message createMessage(AiRole role, String content) {
        HttpDouBaoChatRequest.Message message = new HttpDouBaoChatRequest.Message();
        message.setRole(role.getValue());
        // 豆包支持两种content格式：
        // 1. 直接字符串（简单文本）
        // 2. 数组格式（多模态内容）
        // 为了保持简单，这里使用字符串格式
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
    private HttpDouBaoChatResponse sendHttpRequest(HttpDouBaoChatRequest chatRequest) throws IOException {
        logRequestParams(chatRequest);
        HttpUtils.HttpConfig config = createHttpConfig();
        return HttpUtils.post(
                    getEndpoint(),
                    new HashMap<>(),
                    chatRequest,
                    HttpDouBaoChatResponse.class,
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
        HttpDouBaoChatRequest chatRequest = buildChatRequest(request, true);
        logRequestParams(chatRequest);
        StreamHttpUtils.StreamHttpConfig<HttpDouBaoChatRequest, ResultContent> config = createStreamConfig();
        return StreamHttpUtils.postStream(
                    getEndpoint(),
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
                    .apiKey(getApiKey())
                    .requestInterceptor(r -> log.debug("[DouBao] 请求URL: {}", r.getUrl()))
                    .responseInterceptor(response ->
                                log.debug("[DouBao] 响应状态: {}, 响应内容: {}",
                                            response.getStatus(), truncateForLog(response.body(), 200)))
                    .build();
    }

    /**
     * 创建流式配置
     *
     * @return 流式配置对象
     */
    private StreamHttpUtils.StreamHttpConfig<HttpDouBaoChatRequest, ResultContent> createStreamConfig() {
        return StreamHttpUtils.StreamHttpConfig.<HttpDouBaoChatRequest, ResultContent>builder()
                    .apiKey(getApiKey())
                    .requestInterceptor(r -> r.header("X-Request-ID", UUID.randomUUID().toString()))
                    .responseProcessor(new StreamDouBaoResponse())
                    .build();
    }

    /**
     * 从响应中提取Token使用统计
     *
     * @param response HTTP响应对象
     * @return TokenUsageVO对象，如果响应中没有usage信息则返回null
     */
    private TokenUsageVO extractUsage(HttpDouBaoChatResponse response) {
        if (response == null || response.getUsage() == null) {
            return null;
        }

        HttpDouBaoChatResponse.Usage usage = response.getUsage();

        // 豆包使用标准的promptTokens和completionTokens字段
        return TokenUsageVO.of(
                    usage.getPromptTokens(),
                    usage.getCompletionTokens(),
                    usage.getTotalTokens()
        );
    }

    /**
     * 从响应中提取思考内容
     * 豆包的思考内容在 reasoning_content 字段中
     *
     * @param response HTTP响应对象
     * @return 思考内容，如果没有则返回null
     */
    private String extractThinkingContent(HttpDouBaoChatResponse response) {
        if (response == null || CollectionUtils.isEmpty(response.getChoices())) {
            return null;
        }

        HttpDouBaoChatResponse.Choice.Message message = response.getChoices().get(0).getMessage();
        if (message == null) {
            return null;
        }

        // 豆包的思考内容在 reasoning_content 字段中
        String reasoningContent = message.getReasoningContent();
        if (reasoningContent != null && !reasoningContent.isEmpty()) {
            log.debug("[DouBao] 提取到思考内容，长度: {} 字符", reasoningContent.length());

            // 同时检查reasoning_tokens统计
            if (response.getUsage() != null
                        && response.getUsage().getCompletionTokensDetails() != null) {
                Integer reasoningTokens = response.getUsage().getCompletionTokensDetails().getReasoningTokens();
                if (reasoningTokens != null && reasoningTokens > 0) {
                    log.debug("[DouBao] 思考消耗tokens: {}", reasoningTokens);
                }
            }

            return reasoningContent;
        }

        return null;
    }
}

