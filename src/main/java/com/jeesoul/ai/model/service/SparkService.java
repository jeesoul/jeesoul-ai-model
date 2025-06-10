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
import com.jeesoul.ai.model.util.JsonUtils;
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
 * @author dxy
 * @date 2025-06-10
 */
@Slf4j
@RequiredArgsConstructor
public class SparkService implements AiService {
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
        return sendStreamRequest(request).map(JsonUtils::toJson);
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
        chatRequest.setModel(request.getModel());
        chatRequest.setMessages(buildMessages(request));
        chatRequest.setStream(isStream);
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
                .requestInterceptor(r -> log.info("[Spark] 请求URL: {}", r.getUrl()))
                .responseInterceptor(response ->
                        log.info("[Spark] 响应状态: {}, 响应内容: {}", response.getStatus(), response.body()))
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

    /**
     * 将params参数合并到请求体
     *
     * @param chatRequest 请求体
     * @param params      扩展字段
     */
    private void mergeParamsToRequest(HttpSparkChatRequest chatRequest, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return;
        }
        // 处理通用参数
        setCommonParam(chatRequest, params, "temperature", Double.class);
        setCommonParam(chatRequest, params, "top_p", Double.class);
        setCommonParam(chatRequest, params, "max_tokens", Integer.class);
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
    private void setCommonParam(HttpSparkChatRequest chatRequest, Map<String, Object> params,
                                String paramName, Class<?> paramType) {
        if (!params.containsKey(paramName)) {
            return;
        }
        try {
            String setterName = "set" + Character.toUpperCase(paramName.charAt(0)) + paramName.substring(1);
            chatRequest.getClass().getMethod(setterName, paramType)
                    .invoke(chatRequest, ((Number) params.get(paramName)).doubleValue());
        } catch (Exception e) {
            log.debug("[Spark] 参数透传失败 - {}: {}", paramName, e.getMessage());
        }
    }

    /**
     * 设置其他参数
     *
     * @param chatRequest 请求体
     * @param params      参数映射
     */
    private void setOtherParams(HttpSparkChatRequest chatRequest, Map<String, Object> params) {
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
                log.debug("[Spark] 参数透传失败 - {}: {}", k, e.getMessage());
            }
        });
    }
}
