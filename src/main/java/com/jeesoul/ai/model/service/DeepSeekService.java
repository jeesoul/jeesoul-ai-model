package com.jeesoul.ai.model.service;

import com.jeesoul.ai.model.config.AiProperties;
import com.jeesoul.ai.model.constant.AiModel;
import com.jeesoul.ai.model.constant.AiRole;
import com.jeesoul.ai.model.exception.AiException;
import com.jeesoul.ai.model.request.HttpDeepSeekChatRequest;
import com.jeesoul.ai.model.response.HttpDeepSeekChatResponse;
import com.jeesoul.ai.model.response.StreamDeepSeekResponse;
import com.jeesoul.ai.model.util.HttpUtils;
import com.jeesoul.ai.model.util.StreamHttpUtils;
import com.jeesoul.ai.model.vo.ModelRequestVO;
import com.jeesoul.ai.model.vo.ModelResponseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DeepSeek 大模型服务实现类。
 * 实现与 DeepSeek AI 的对接逻辑，包括HTTP和流式对话功能。
 *
 * @author dxy
 * @date 2025-06-10
 */
@Slf4j
@RequiredArgsConstructor
public class DeepSeekService implements AiService {
    /**
     * AI配置信息
     */
    private final AiProperties aiProperties;
    /**
     * HTTP工具类实例
     */
    private final HttpUtils aiHttpUtils;
    /**
     * 流式HTTP工具类实例
     */
    private final StreamHttpUtils streamHttpUtils;

    @Override
    public ModelResponseVO httpChat(ModelRequestVO request) throws AiException {
        try {
            HttpDeepSeekChatRequest chatRequest = buildChatRequest(request, false);
            HttpDeepSeekChatResponse response = sendHttpRequest(chatRequest);

            if (CollectionUtils.isEmpty(response.getChoices())) {
                return new ModelResponseVO("", AiModel.DEEP_SEEK.getModelName());
            }
            String result = response.getChoices().get(0).getMessage().getContent();
            return new ModelResponseVO(result, AiModel.DEEP_SEEK.getModelName());
        } catch (Exception e) {
            log.error("[DeepSeek] 调用失败: {}", e.getMessage(), e);
            throw new AiException("DeepSeek调用失败", e);
        }
    }

    @Override
    public Flux<ModelResponseVO> streamChat(ModelRequestVO request) throws AiException {
        return sendStreamRequest(request)
                .map(content -> new ModelResponseVO(content, AiModel.DEEP_SEEK.getModelName()));
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
    private HttpDeepSeekChatRequest buildChatRequest(ModelRequestVO request, boolean isStream) {
        HttpDeepSeekChatRequest chatRequest = new HttpDeepSeekChatRequest();
        chatRequest.setModel(request.getModel());
        chatRequest.setMessages(buildMessages(request));
        chatRequest.setStream(isStream);
        chatRequest.setTemperature(request.getTemperature());
        chatRequest.setTopP(request.getTopP());
        chatRequest.setMaxTokens(request.getMaxTokens());
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
    private Flux<String> sendStreamRequest(ModelRequestVO request) {
        HttpDeepSeekChatRequest chatRequest = buildChatRequest(request, true);
        StreamHttpUtils.StreamHttpConfig<HttpDeepSeekChatRequest, String> config = createStreamConfig();
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
                .requestInterceptor(r -> log.info("[DeepSeek] 请求URL: {}", r.getUrl()))
                .responseInterceptor(response ->
                        log.info("[DeepSeek] 响应状态: {}, 响应内容: {}", response.getStatus(), response.body()))
                .build();
    }

    /**
     * 创建流式配置
     *
     * @return 流式配置对象
     */
    private StreamHttpUtils.StreamHttpConfig<HttpDeepSeekChatRequest, String> createStreamConfig() {
        return StreamHttpUtils.StreamHttpConfig.<HttpDeepSeekChatRequest, String>builder()
                .apiKey(aiProperties.getDeepSeek().getApiKey())
                .requestInterceptor(r -> r.header("X-Request-ID", UUID.randomUUID().toString()))
                .responseProcessor(new StreamDeepSeekResponse())
                .build();
    }

    /**
     * 将params参数合并到请求体
     *
     * @param chatRequest 请求体
     * @param params      扩展字段
     */
    private void mergeParamsToRequest(HttpDeepSeekChatRequest chatRequest, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return;
        }
        // 处理通用参数
//        setCommonParam(chatRequest, params, "temperature", Double.class);
//        setCommonParam(chatRequest, params, "top_p", Double.class);
//        setCommonParam(chatRequest, params, "max_tokens", Integer.class);
        // 处理其他参数
        setOtherParams(chatRequest, params);
    }

    /**
     * 设置通用参数
     *
     * @param chatRequest 请求体
     * @param params      参数映射
     * @param paramName   参数名
     * @param paramType   参数类型
     */
    private void setCommonParam(HttpDeepSeekChatRequest chatRequest, Map<String, Object> params,
                                String paramName, Class<?> paramType) {
        if (!params.containsKey(paramName)) {
            return;
        }
        try {
            String setterName = "set" + Character.toUpperCase(paramName.charAt(0)) + paramName.substring(1);
            chatRequest.getClass().getMethod(setterName, paramType)
                    .invoke(chatRequest, ((Number) params.get(paramName)).doubleValue());
        } catch (Exception e) {
            log.debug("[DeepSeek] 参数透传失败 - {}: {}", paramName, e.getMessage());
        }
    }

    /**
     * 设置其他参数
     *
     * @param chatRequest 请求体
     * @param params      参数映射
     */
    private void setOtherParams(HttpDeepSeekChatRequest chatRequest, Map<String, Object> params) {
        params.forEach((k, v) -> {
            String setter = "set" + Character.toUpperCase(k.charAt(0)) + k.substring(1);
            try {
                for (java.lang.reflect.Method m : chatRequest.getClass().getMethods()) {
                    if (m.getName().equals(setter) && m.getParameterCount() == 1) {
                        m.invoke(chatRequest, v);
                        break;
                    }
                }
            } catch (Exception e) {
                log.debug("[DeepSeek] 参数透传失败 - {}: {}", k, e.getMessage());
            }
        });
    }
}
