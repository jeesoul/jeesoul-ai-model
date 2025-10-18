package com.jeesoul.ai.model.service;

import com.jeesoul.ai.model.config.AiProperties;
import com.jeesoul.ai.model.constant.AiModel;
import com.jeesoul.ai.model.constant.AiRole;
import com.jeesoul.ai.model.exception.AiException;
import com.jeesoul.ai.model.request.HttpChatGPTChatRequest;
import com.jeesoul.ai.model.response.HttpChatGPTChatResponse;
import com.jeesoul.ai.model.response.StreamChatGPTResponse;
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
 * ChatGPT 大模型服务实现类。
 * 实现与 ChatGPT AI 的对接逻辑，包括HTTP和流式对话功能。
 *
 * @author dxy
 * @date 2025-06-10
 */
@Slf4j
public class ChatGPTService extends AbstractAiService {

    public ChatGPTService(AiProperties aiProperties, HttpUtils aiHttpUtils, StreamHttpUtils streamHttpUtils) {
        super(aiProperties, aiHttpUtils, streamHttpUtils);
    }

    @Override
    protected String getModelName() {
        return AiModel.CHATGPT.getModelName();
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
            HttpChatGPTChatRequest chatRequest = buildChatRequest(request, false);
            HttpChatGPTChatResponse response = sendHttpRequest(chatRequest);

            if (CollectionUtils.isEmpty(response.getChoices())) {
                return new ModelResponseVO("", AiModel.CHATGPT.getModelName());
            }
            String result = response.getChoices().get(0).getMessage().getContent();
            return new ModelResponseVO(result, AiModel.CHATGPT.getModelName());
        } catch (Exception e) {
            log.error("[ChatGPT] 调用失败: {}", e.getMessage(), e);
            throw new AiException("ChatGPT调用失败", e);
        }
    }

    @Override
    public Flux<ModelResponseVO> streamChat(ModelRequestVO request) throws AiException {
        return sendStreamRequest(request)
                .map(content -> new ModelResponseVO(content, AiModel.CHATGPT.getModelName()));
    }

    @Override
    public Flux<String> streamChatStr(ModelRequestVO request) throws AiException {
        return sendStreamRequest(request);
    }

    /**
     * 构建聊天请求对象
     *
     * @param request  请求参数
     * @param isStream 是否为流式请求
     * @return 构建好的请求对象
     */
    private HttpChatGPTChatRequest buildChatRequest(ModelRequestVO request, boolean isStream) {
        HttpChatGPTChatRequest chatRequest = new HttpChatGPTChatRequest();
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
    private List<HttpChatGPTChatRequest.Message> buildMessages(ModelRequestVO request) {
        List<HttpChatGPTChatRequest.Message> messages = new ArrayList<>();
        // 添加系统消息
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
            messages.add(createMessage(AiRole.SYSTEM, request.getSystemPrompt()));
        }
        // 添加用户消息
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
    private HttpChatGPTChatRequest.Message createMessage(AiRole role, String content) {
        HttpChatGPTChatRequest.Message message = new HttpChatGPTChatRequest.Message();
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
    private HttpChatGPTChatResponse sendHttpRequest(HttpChatGPTChatRequest chatRequest) throws IOException {
        HttpUtils.HttpConfig config = createHttpConfig();
        return aiHttpUtils.post(
                aiProperties.getChatGpt().getEndpoint(),
                new HashMap<>(),
                chatRequest,
                HttpChatGPTChatResponse.class,
                config
        );
    }

    /**
     * 发送流式请求
     *
     * @param request 请求参数
     * @return 流式响应
     */
    private Flux<String> sendStreamRequest(ModelRequestVO request) {
        HttpChatGPTChatRequest chatRequest = buildChatRequest(request, true);
        StreamHttpUtils.StreamHttpConfig<HttpChatGPTChatRequest, String> config = createStreamConfig();
        return streamHttpUtils.postStream(
                aiProperties.getChatGpt().getEndpoint(),
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
                .apiKey(aiProperties.getChatGpt().getApiKey())
                .requestInterceptor(r -> log.debug("[ChatGPT] 请求URL: {}", r.getUrl()))
                .responseInterceptor(response ->
                        log.debug("[ChatGPT] 响应状态: {}, 响应内容: {}", 
                                 response.getStatus(), truncateForLog(response.body(), 200)))
                .build();
    }

    /**
     * 创建流式配置
     *
     * @return 流式配置对象
     */
    private StreamHttpUtils.StreamHttpConfig<HttpChatGPTChatRequest, String> createStreamConfig() {
        return StreamHttpUtils.StreamHttpConfig.<HttpChatGPTChatRequest, String>builder()
                .apiKey(aiProperties.getChatGpt().getApiKey())
                .requestInterceptor(r -> r.header("X-Request-ID", UUID.randomUUID().toString()))
                .responseProcessor(new StreamChatGPTResponse())
                .build();
    }

}
