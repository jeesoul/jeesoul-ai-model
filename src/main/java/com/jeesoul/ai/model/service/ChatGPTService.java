package com.jeesoul.ai.model.service;

import com.jeesoul.ai.model.config.AiProperties;
import com.jeesoul.ai.model.config.ModelConfig;
import com.jeesoul.ai.model.constant.AiModel;
import com.jeesoul.ai.model.constant.AiRole;
import com.jeesoul.ai.model.constant.ContentType;
import com.jeesoul.ai.model.constant.ImageDetail;
import com.jeesoul.ai.model.entity.ResultContent;
import com.jeesoul.ai.model.exception.AiException;
import com.jeesoul.ai.model.request.HttpChatGPTChatRequest;
import com.jeesoul.ai.model.response.HttpChatGPTChatResponse;
import com.jeesoul.ai.model.response.StreamChatGPTResponse;
import com.jeesoul.ai.model.util.HttpUtils;
import com.jeesoul.ai.model.util.StreamHttpUtils;
import com.jeesoul.ai.model.vo.MessageContent;
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
import java.util.Locale;
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

    /**
     * 构造函数（推荐使用）
     *
     * @param modelConfig 模型配置
     */
    public ChatGPTService(ModelConfig modelConfig) {
        super(modelConfig);
    }

    /**
     * 构造函数（向后兼容 - 支持 AiProperties + 工具类注入）
     * 请使用 {@link #ChatGPTService(ModelConfig)} 替代
     * HttpUtils 和 StreamHttpUtils 已改为静态工具类，无需注入，工具类参数将被忽略
     */
    @Deprecated
    public ChatGPTService(AiProperties aiProperties, HttpUtils aiHttpUtils, StreamHttpUtils streamHttpUtils) {
        super(aiProperties.getChatGpt());
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
                return ModelResponseVO.of("", AiModel.CHATGPT.getModelName(),
                            chatRequest.getModel());
            }

            String result = response.getChoices().get(0).getMessage().getContent();

            // 提取usage信息
            TokenUsageVO usage = extractUsage(response);

            // 返回完整的响应信息
            return ModelResponseVO.of(
                        result,
                        null,
                        AiModel.CHATGPT.getModelName(),
                        chatRequest.getModel(),
                        usage
            );
        } catch (Exception e) {
            log.error("[ChatGPT] 调用失败: {}", e.getMessage(), e);
            throw new AiException("ChatGPT调用失败", e);
        }
    }

    @Override
    public Flux<ModelResponseVO> streamChat(ModelRequestVO request) throws AiException {
        // 获取实际使用的模型版本
        String actualModel = getModel(request, modelConfig.getModel());

        return sendStreamRequest(request)
                    .filter(content -> {
                        // 过滤掉content为空的chunk（但保留只有usage的chunk）
                        String text = content.getContent();
                        return (text != null && !text.isEmpty()) || content.getUsage() != null;
                    })
                    .map(content -> {
                        // 构建完整的响应对象，包含usage信息（如果有）
                        return ModelResponseVO.of(
                                    content.getContent(),
                                    content.getThinkingContent(),
                                    AiModel.CHATGPT.getModelName(),
                                    actualModel,
                                    content.getUsage()
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
            HttpChatGPTChatRequest chatRequest = buildChatRequest(request, false);
            logRequestParams(chatRequest);
            HttpUtils.HttpConfig config = createHttpConfig();
            return HttpUtils.postRaw(
                        getEndpoint(),
                        new HashMap<>(),
                        chatRequest,
                        config
            );
        } catch (Exception e) {
            log.error("[ChatGPT] 调用失败: {}", e.getMessage(), e);
            throw new AiException("ChatGPT调用失败", e);
        }
    }

    @Override
    public Flux<String> streamChatRaw(ModelRequestVO request) throws AiException {
        HttpChatGPTChatRequest chatRequest = buildChatRequest(request, true);
        logRequestParams(chatRequest);
        StreamHttpUtils.StreamHttpConfig<HttpChatGPTChatRequest, String> config =
                    StreamHttpUtils.StreamHttpConfig.<HttpChatGPTChatRequest, String>builder()
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
    private HttpChatGPTChatRequest buildChatRequest(ModelRequestVO request, boolean isStream) {
        HttpChatGPTChatRequest chatRequest = new HttpChatGPTChatRequest();
        String model = getModel(request, modelConfig.getModel());
        chatRequest.setModel(model);
        chatRequest.setMessages(buildMessages(request));
        chatRequest.setStream(isStream);
        chatRequest.setTemperature(getTemperature(request, modelConfig.getTemperature()));
        chatRequest.setTopP(getTopP(request, modelConfig.getTopP()));
        chatRequest.setMaxTokens(getMaxTokens(request, modelConfig.getMaxTokens()));
        // 使用基类的参数合并方法
        mergeParamsToRequest(chatRequest, request.getParams());
        normalizeMaxTokenParameter(chatRequest, model);
        return chatRequest;
    }

    /**
     * 按模型版本选择最大输出token参数
     *
     * @param chatRequest ChatGPT请求对象
     * @param model 模型名称
     */
    private void normalizeMaxTokenParameter(HttpChatGPTChatRequest chatRequest, String model) {
        if (!supportsMaxCompletionTokens(model)) {
            return;
        }
        if (chatRequest.getMaxCompletionTokens() == null) {
            chatRequest.setMaxCompletionTokens(chatRequest.getMaxTokens());
        }
        chatRequest.setMaxTokens(null);
    }

    /**
     * 判断模型是否使用新版最大输出token参数
     *
     * @param model 模型名称
     * @return true表示使用max_completion_tokens
     */
    private boolean supportsMaxCompletionTokens(String model) {
        if (model == null) {
            return false;
        }
        String normalized = model.trim().toLowerCase(Locale.ENGLISH);
        return normalized.startsWith("gpt-5") || normalized.startsWith("chatgpt-5")
                || normalized.startsWith("gpt-6") || normalized.startsWith("chatgpt-6");
    }

    /**
     * 构建消息列表
     *
     * @param request 请求参数
     * @return 消息列表
     */
    private List<HttpChatGPTChatRequest.Message> buildMessages(ModelRequestVO request) {
        List<HttpChatGPTChatRequest.Message> messages = new ArrayList<>();

        // 如果设置了 systemPrompt，系统消息放在最前面
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
            messages.add(createMessage(AiRole.SYSTEM, request.getSystemPrompt()));
        }

        // 如果请求中提供了消息列表，添加消息列表（跳过其中的 system 消息，因为已经在最前面添加了）
        if (!CollectionUtils.isEmpty(request.getMessages())) {
            for (ModelRequestVO.Message voMessage : request.getMessages()) {
                HttpChatGPTChatRequest.Message message = new HttpChatGPTChatRequest.Message();
                message.setRole(voMessage.getRole());
                message.setContent(voMessage.getContent());
                messages.add(message);
            }
        }

        // 优先使用contents构建OpenAI多模态用户消息
        if (!CollectionUtils.isEmpty(request.getContents())) {
            HttpChatGPTChatRequest.Message message = new HttpChatGPTChatRequest.Message();
            message.setRole(AiRole.USER.getValue());
            message.setContentParts(buildContentParts(request.getContents()));
            messages.add(message);
        } else {
            // 否则使用原来的逻辑：添加用户消息
            messages.add(createMessage(AiRole.USER, request.getPrompt()));
        }
        return messages;
    }

    /**
     * 将统一消息内容转换为OpenAI内容片段
     *
     * @param contents 统一消息内容列表
     * @return OpenAI内容片段列表
     */
    private List<HttpChatGPTChatRequest.ContentPart> buildContentParts(List<MessageContent> contents) {
        List<HttpChatGPTChatRequest.ContentPart> parts = new ArrayList<>();
        for (MessageContent content : contents) {
            if (content == null || content.getType() == null) {
                continue;
            }

            if (ContentType.TEXT == content.getType()) {
                if (content.getText() != null) {
                    HttpChatGPTChatRequest.ContentPart part = new HttpChatGPTChatRequest.ContentPart();
                    part.setType(ContentType.TEXT.getValue());
                    part.setText(content.getText());
                    parts.add(part);
                }
            } else if (ContentType.IMAGE_URL == content.getType()) {
                if (content.getImageUrl() != null && content.getImageUrl().getUrl() != null) {
                    parts.add(createImagePart(content.getImageUrl().getUrl(),
                            content.getImageUrl().getDetail()));
                }
            } else if (ContentType.IMAGE_BASE64 == content.getType()) {
                if (content.getBase64() != null) {
                    parts.add(createImagePart(toDataUrl(content), null));
                }
            } else {
                log.warn("[ChatGPT] 不支持的多模态内容类型: {}", content.getType());
            }
        }
        return parts;
    }

    /**
     * 创建OpenAI图片内容片段
     *
     * @param url 图片URL或Data URL
     * @param detail 图片详细度
     * @return 图片内容片段
     */
    private HttpChatGPTChatRequest.ContentPart createImagePart(String url, ImageDetail detail) {
        HttpChatGPTChatRequest.ContentPart part = new HttpChatGPTChatRequest.ContentPart();
        part.setType(ContentType.IMAGE_URL.getValue());
        HttpChatGPTChatRequest.ContentPart.ImageUrl imageUrl =
                new HttpChatGPTChatRequest.ContentPart.ImageUrl();
        imageUrl.setUrl(url);
        if (detail != null) {
            imageUrl.setDetail(detail.getValue());
        }
        part.setImageUrl(imageUrl);
        return part;
    }

    /**
     * 将Base64图片转换为OpenAI Data URL
     *
     * @param content Base64图片内容
     * @return OpenAI Data URL
     */
    private String toDataUrl(MessageContent content) {
        String base64 = content.getBase64().trim();
        if (base64.startsWith("data:")) {
            return base64;
        }
        String mimeType = content.getMimeType();
        if (mimeType == null || mimeType.trim().isEmpty()) {
            mimeType = "image/jpeg";
        }
        return "data:" + mimeType.trim() + ";base64," + base64;
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
        logRequestParams(chatRequest);
        HttpUtils.HttpConfig config = createHttpConfig();
        return HttpUtils.post(
                    getEndpoint(),
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
    private Flux<ResultContent> sendStreamRequest(ModelRequestVO request) {
        HttpChatGPTChatRequest chatRequest = buildChatRequest(request, true);
        logRequestParams(chatRequest);
        StreamHttpUtils.StreamHttpConfig<HttpChatGPTChatRequest, ResultContent> config = createStreamConfig();
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
    private StreamHttpUtils.StreamHttpConfig<HttpChatGPTChatRequest, ResultContent> createStreamConfig() {
        return StreamHttpUtils.StreamHttpConfig.<HttpChatGPTChatRequest, ResultContent>builder()
                    .apiKey(getApiKey())
                    .requestInterceptor(r -> r.header("X-Request-ID", UUID.randomUUID().toString()))
                    .responseProcessor(new StreamChatGPTResponse())
                    .build();
    }

    /**
     * 从响应中提取Token使用统计
     *
     * @param response HTTP响应对象
     * @return TokenUsageVO对象，如果响应中没有usage信息则返回null
     */
    private TokenUsageVO extractUsage(HttpChatGPTChatResponse response) {
        if (response == null || response.getUsage() == null) {
            return null;
        }

        HttpChatGPTChatResponse.Usage usage = response.getUsage();

        // ChatGPT使用标准的promptTokens和completionTokens字段
        return TokenUsageVO.of(
                    usage.getPromptTokens(),
                    usage.getCompletionTokens(),
                    usage.getTotalTokens()
        );
    }

}
