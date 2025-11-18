package com.jeesoul.ai.model.service;

import com.jeesoul.ai.model.config.AiProperties;
import com.jeesoul.ai.model.constant.AiModel;
import com.jeesoul.ai.model.constant.AiRole;
import com.jeesoul.ai.model.entity.ResultContent;
import com.jeesoul.ai.model.exception.AiException;
import com.jeesoul.ai.model.request.HttpSparkChatRequest;
import com.jeesoul.ai.model.response.HttpSparkChatResponse;
import com.jeesoul.ai.model.response.StreamSparkResponse;
import com.jeesoul.ai.model.util.HttpUtils;
import com.jeesoul.ai.model.util.StreamHttpUtils;
import com.jeesoul.ai.model.vo.ModelRequestVO;
import com.jeesoul.ai.model.vo.ModelResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * 讯飞星火大模型服务实现类
 * 实现与讯飞星火AI的对接逻辑，包括HTTP和流式对话功能
 *
 * @author dxy
 * @date 2025-06-10
 */
@Slf4j
public class SparkService extends AbstractAiService {

    public SparkService(AiProperties aiProperties, HttpUtils aiHttpUtils, StreamHttpUtils streamHttpUtils) {
        super(aiProperties, aiHttpUtils, streamHttpUtils);
    }

    @Override
    protected String getModelName() {
        return AiModel.SPARK.getModelName();
    }

    @Override
    protected boolean supportSystemPrompt() {
        return true;
    }

    @Override
    protected boolean supportThinking() {
        return false;
    }

    @Override
    public ModelResponseVO httpChat(ModelRequestVO request) throws AiException {
        // 参数校验
        validateRequest(request);
        // 警告不支持的功能
        warnUnsupportedFeatures(request);
        
        try {
            HttpSparkChatRequest chatRequest = buildChatRequest(request, false);
            HttpSparkChatResponse response = sendHttpRequest(chatRequest);

            if (CollectionUtils.isEmpty(response.getChoices())) {
                return new ModelResponseVO("", AiModel.SPARK.getModelName());
            }
            String result = response.getChoices().get(0).getMessage().getContent();
            return new ModelResponseVO(result, AiModel.SPARK.getModelName());
        } catch (Exception e) {
            log.error("[Spark] 调用失败: {}", e.getMessage(), e);
            throw new AiException("Spark调用失败", e);
        }
    }

    @Override
    public Flux<ModelResponseVO> streamChat(ModelRequestVO request) throws AiException {
        return sendStreamRequest(request)
                .map(content -> new ModelResponseVO(content.getContent(), content.getThinking(),
                        AiModel.SPARK.getModelName()));
    }

    @Override
    public Flux<String> streamChatStr(ModelRequestVO request) throws AiException {
        // 统一返回 content 字符串，而不是JSON
        return sendStreamRequest(request).map(ResultContent::getContent);
    }

    @Override
    public String httpChatRaw(ModelRequestVO request) throws AiException {
        // 参数校验
        validateRequest(request);
        // 警告不支持的功能
        warnUnsupportedFeatures(request);

        try {
            HttpSparkChatRequest chatRequest = buildChatRequest(request, false);
            logRequestParams(chatRequest);
            HttpUtils.HttpConfig config = createHttpConfig();
            return aiHttpUtils.postRaw(
                    aiProperties.getSpark().getEndpoint(),
                    new HashMap<>(),
                    chatRequest,
                    config
            );
        } catch (Exception e) {
            log.error("[Spark] 调用失败: {}", e.getMessage(), e);
            throw new AiException("Spark调用失败", e);
        }
    }

    @Override
    public Flux<String> streamChatRaw(ModelRequestVO request) throws AiException {
        HttpSparkChatRequest chatRequest = buildChatRequest(request, true);
        logRequestParams(chatRequest);
        StreamHttpUtils.StreamHttpConfig<HttpSparkChatRequest, String> config = StreamHttpUtils.StreamHttpConfig
                .<HttpSparkChatRequest, String>builder()
                .apiKey(aiProperties.getSpark().getApiKey())
                .requestInterceptor(r -> r.header("X-Request-ID", UUID.randomUUID().toString()))
                .build();
        return streamHttpUtils.postStreamRaw(
                aiProperties.getSpark().getEndpoint(),
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
    private HttpSparkChatRequest buildChatRequest(ModelRequestVO request, boolean isStream) {
        HttpSparkChatRequest chatRequest = new HttpSparkChatRequest();
        chatRequest.setModel(getModel(request, aiProperties.getSpark().getModel()));
        chatRequest.setMessages(buildMessages(request));
        chatRequest.setStream(isStream);
        chatRequest.setTemperature(getTemperature(request, aiProperties.getSpark().getTemperature()));
        chatRequest.setTopP(getTopP(request, aiProperties.getSpark().getTopP()));
        chatRequest.setMaxTokens(getMaxTokens(request, aiProperties.getSpark().getMaxTokens()));
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
    private List<HttpSparkChatRequest.Message> buildMessages(ModelRequestVO request) {
        List<HttpSparkChatRequest.Message> messages = new ArrayList<>();
        
        // 如果设置了 systemPrompt，系统消息放在最前面
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
            messages.add(createMessage(AiRole.SYSTEM, request.getSystemPrompt()));
        }
        
        // 如果请求中提供了消息列表，添加消息列表（跳过其中的 system 消息，因为已经在最前面添加了）
        if (!CollectionUtils.isEmpty(request.getMessages())) {
            for (ModelRequestVO.Message voMessage : request.getMessages()) {
                HttpSparkChatRequest.Message message = new HttpSparkChatRequest.Message();
                message.setRole(voMessage.getRole());
                message.setContent(voMessage.getContent());
                messages.add(message);
            }
            return messages;
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
    private HttpSparkChatRequest.Message createMessage(AiRole role, String content) {
        HttpSparkChatRequest.Message message = new HttpSparkChatRequest.Message();
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
    private HttpSparkChatResponse sendHttpRequest(HttpSparkChatRequest chatRequest) throws IOException {
        logRequestParams(chatRequest);
        HttpUtils.HttpConfig config = createHttpConfig();
        return aiHttpUtils.post(
                aiProperties.getSpark().getEndpoint(),
                new HashMap<>(),
                chatRequest,
                HttpSparkChatResponse.class,
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
        HttpSparkChatRequest chatRequest = buildChatRequest(request, true);
        logRequestParams(chatRequest);
        StreamHttpUtils.StreamHttpConfig<HttpSparkChatRequest, ResultContent> config = createStreamConfig();
        return streamHttpUtils.postStream(
                aiProperties.getSpark().getEndpoint(),
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
                .apiKey(aiProperties.getSpark().getApiKey())
                .requestInterceptor(r -> log.debug("[Spark] 请求URL: {}", r.getUrl()))
                .responseInterceptor(response ->
                        log.debug("[Spark] 响应状态: {}, 响应内容: {}", 
                                 response.getStatus(), truncateForLog(response.body(), 200)))
                .build();
    }

    /**
     * 创建流式配置
     *
     * @return 流式配置对象
     */
    private StreamHttpUtils.StreamHttpConfig<HttpSparkChatRequest, ResultContent> createStreamConfig() {
        return StreamHttpUtils.StreamHttpConfig.<HttpSparkChatRequest, ResultContent>builder()
                .apiKey(aiProperties.getSpark().getApiKey())
                .requestInterceptor(r -> r.header("X-Request-ID", UUID.randomUUID().toString()))
                .responseProcessor(new StreamSparkResponse())
                .build();
    }

}
