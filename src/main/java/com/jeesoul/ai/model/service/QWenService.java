package com.jeesoul.ai.model.service;

import com.jeesoul.ai.model.config.AiProperties;
import com.jeesoul.ai.model.constant.AiModel;
import com.jeesoul.ai.model.constant.AiRole;
import com.jeesoul.ai.model.entity.ResultContent;
import com.jeesoul.ai.model.exception.AiException;
import com.jeesoul.ai.model.request.HttpQWenChatRequest;
import com.jeesoul.ai.model.response.HttpQWenChatResponse;
import com.jeesoul.ai.model.response.StreamQWenResponse;
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
 * @author dxy
 * @date 2025-06-10
 */
@Slf4j
public class QWenService extends AbstractAiService {

    public QWenService(AiProperties aiProperties, HttpUtils aiHttpUtils, StreamHttpUtils streamHttpUtils) {
        super(aiProperties, aiHttpUtils, streamHttpUtils);
    }

    @Override
    protected String getModelName() {
        return AiModel.Q_WEN.getModelName();
    }

    @Override
    protected boolean supportSystemPrompt() {
        return false;
    }

    @Override
    protected boolean supportThinking() {
        return true;
    }

    @Override
    public ModelResponseVO httpChat(ModelRequestVO request) throws AiException {
        // 参数校验
        validateRequest(request);
        // 警告不支持的功能
        warnUnsupportedFeatures(request);
        
        try {
            HttpQWenChatRequest chatRequest = buildChatRequest(request, false);
            HttpQWenChatResponse response = sendHttpRequest(chatRequest);

            if (CollectionUtils.isEmpty(response.getChoices())) {
                return new ModelResponseVO("", AiModel.Q_WEN.getModelName());
            }
            String result = response.getChoices().get(0).getMessage().getContent();
            return new ModelResponseVO(result, AiModel.Q_WEN.getModelName());
        } catch (Exception e) {
            log.error("[QWen] 调用失败: {}", e.getMessage(), e);
            throw new AiException("QWen调用失败", e);
        }
    }

    @Override
    public Flux<ModelResponseVO> streamChat(ModelRequestVO request) throws AiException {
        return sendStreamRequest(request)
                .map(content -> new ModelResponseVO(content.getContent(), content.getThinking(),
                        AiModel.Q_WEN.getModelName()));
    }

    @Override
    public Flux<String> streamChatStr(ModelRequestVO request) throws AiException {
        // 统一返回 content 字符串，而不是JSON
        return sendStreamRequest(request).map(ResultContent::getContent);
    }

    /**
     * 构建聊天请求对象
     *
     * @param request  请求参数
     * @param isStream 是否为流式请求
     * @return 构建好的请求对象
     */
    private HttpQWenChatRequest buildChatRequest(ModelRequestVO request, boolean isStream) {
        HttpQWenChatRequest chatRequest = new HttpQWenChatRequest();
        // 添加思考提示（如果启用）
        if (request.isEnableThinking()) {
            chatRequest.setEnableThinking(Boolean.TRUE);
            HttpQWenChatRequest.ChatThink chatThink = new HttpQWenChatRequest.ChatThink();
            chatThink.setEnableThinking(Boolean.TRUE);
            chatRequest.setChatTemplateKwargs(chatThink);
        }
        chatRequest.setModel(request.getModel());
        chatRequest.setMessages(buildMessages(request));
        chatRequest.setStream(isStream);
        chatRequest.setTemperature(request.getTemperature());
        chatRequest.setTopP(request.getTopP());
        chatRequest.setMaxTokens(request.getMaxTokens());
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
    private List<HttpQWenChatRequest.Message> buildMessages(ModelRequestVO request) {
        List<HttpQWenChatRequest.Message> messages = new ArrayList<>();
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
    private HttpQWenChatRequest.Message createMessage(AiRole role, String content) {
        HttpQWenChatRequest.Message message = new HttpQWenChatRequest.Message();
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
    private HttpQWenChatResponse sendHttpRequest(HttpQWenChatRequest chatRequest) throws IOException {
        HttpUtils.HttpConfig config = createHttpConfig();
        return aiHttpUtils.post(
                aiProperties.getQwen().getEndpoint(),
                new HashMap<>(),
                chatRequest,
                HttpQWenChatResponse.class,
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
        HttpQWenChatRequest chatRequest = buildChatRequest(request, true);
        StreamHttpUtils.StreamHttpConfig<HttpQWenChatRequest, ResultContent> config = createStreamConfig();
        return streamHttpUtils.postStream(
                aiProperties.getQwen().getEndpoint(),
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
                .apiKey(aiProperties.getQwen().getApiKey())
                .requestInterceptor(r -> log.debug("[QWen] 请求URL: {}", r.getUrl()))
                .responseInterceptor(response ->
                        log.debug("[QWen] 响应状态: {}, 响应内容: {}", 
                                 response.getStatus(), truncateForLog(response.body(), 200)))
                .build();
    }

    /**
     * 创建流式配置
     *
     * @return 流式配置对象
     */
    private StreamHttpUtils.StreamHttpConfig<HttpQWenChatRequest, ResultContent> createStreamConfig() {
        return StreamHttpUtils.StreamHttpConfig.<HttpQWenChatRequest, ResultContent>builder()
                .apiKey(aiProperties.getQwen().getApiKey())
                .requestInterceptor(r -> r.header("X-Request-ID", UUID.randomUUID().toString()))
                .responseProcessor(new StreamQWenResponse())
                .build();
    }

}
