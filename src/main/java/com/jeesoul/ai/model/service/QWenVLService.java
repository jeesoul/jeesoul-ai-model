package com.jeesoul.ai.model.service;

import com.jeesoul.ai.model.config.AiProperties;
import com.jeesoul.ai.model.constant.AiModel;
import com.jeesoul.ai.model.constant.AiRole;
import com.jeesoul.ai.model.constant.ContentType;
import com.jeesoul.ai.model.entity.ResultContent;
import com.jeesoul.ai.model.exception.AiException;
import com.jeesoul.ai.model.request.HttpQWenVLChatRequest;
import com.jeesoul.ai.model.response.HttpQWenChatResponse;
import com.jeesoul.ai.model.response.StreamQWenResponse;
import com.jeesoul.ai.model.util.HttpUtils;
import com.jeesoul.ai.model.util.StreamHttpUtils;
import com.jeesoul.ai.model.vo.MessageContent;
import com.jeesoul.ai.model.vo.ModelRequestVO;
import com.jeesoul.ai.model.vo.ModelResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.*;

/**
 * 千问视觉理解模型服务实现
 * 支持图片、视频等多模态输入
 *
 * @author dxy
 * @date 2025-10-18
 */
@Slf4j
public class QWenVLService extends AbstractAiService {

    public QWenVLService(AiProperties aiProperties, HttpUtils aiHttpUtils, StreamHttpUtils streamHttpUtils) {
        super(aiProperties, aiHttpUtils, streamHttpUtils);
    }

    @Override
    protected String getModelName() {
        return AiModel.QWEN_VL.getModelName();
    }

    @Override
    protected boolean supportSystemPrompt() {
        return true;
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
            HttpQWenVLChatRequest chatRequest = buildChatRequest(request, false);
            HttpQWenChatResponse response = sendHttpRequest(chatRequest);

            if (CollectionUtils.isEmpty(response.getChoices())) {
                return new ModelResponseVO("", AiModel.QWEN_VL.getModelName());
            }
            String result = response.getChoices().get(0).getMessage().getContent();
            return new ModelResponseVO(result, AiModel.QWEN_VL.getModelName());
        } catch (Exception e) {
            log.error("[QWenVL] 调用失败: {}", e.getMessage(), e);
            throw new AiException("QWenVL调用失败", e);
        }
    }

    @Override
    public Flux<ModelResponseVO> streamChat(ModelRequestVO request) throws AiException {
        return sendStreamRequest(request)
                .map(content -> new ModelResponseVO(content.getContent(), content.getThinking(),
                        AiModel.QWEN_VL.getModelName()));
    }

    @Override
    public Flux<String> streamChatStr(ModelRequestVO request) throws AiException {
        // 统一返回 content 字符串
        return sendStreamRequest(request).map(ResultContent::getContent);
    }

    @Override
    public String httpChatRaw(ModelRequestVO request) throws AiException {
        // 参数校验
        validateRequest(request);
        // 警告不支持的功能
        warnUnsupportedFeatures(request);

        try {
            HttpQWenVLChatRequest chatRequest = buildChatRequest(request, false);
            logRequestParams(chatRequest);
            HttpUtils.HttpConfig config = createHttpConfig();
            return aiHttpUtils.postRaw(
                    aiProperties.getQwenVL().getEndpoint(),
                    new HashMap<>(),
                    chatRequest,
                    config
            );
        } catch (Exception e) {
            log.error("[QWenVL] 调用失败: {}", e.getMessage(), e);
            throw new AiException("QWenVL调用失败", e);
        }
    }

    @Override
    public Flux<String> streamChatRaw(ModelRequestVO request) throws AiException {
        // 参数校验
        validateRequest(request);
        // 警告不支持的功能
        warnUnsupportedFeatures(request);

        HttpQWenVLChatRequest chatRequest = buildChatRequest(request, true);
        logRequestParams(chatRequest);
        StreamHttpUtils.StreamHttpConfig<HttpQWenVLChatRequest, String> config = StreamHttpUtils.StreamHttpConfig
                .<HttpQWenVLChatRequest, String>builder()
                .apiKey(aiProperties.getQwenVL().getApiKey())
                .requestInterceptor(r -> r.header("X-Request-ID", UUID.randomUUID().toString()))
                .build();
        return streamHttpUtils.postStreamRaw(
                aiProperties.getQwenVL().getEndpoint(),
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
    private HttpQWenVLChatRequest buildChatRequest(ModelRequestVO request, boolean isStream) {
        HttpQWenVLChatRequest chatRequest = new HttpQWenVLChatRequest();
        
        // 添加思考提示（如果启用）
        if (request.isEnableThinking()) {
            chatRequest.setEnableThinking(Boolean.TRUE);
            HttpQWenVLChatRequest.ChatThink chatThink = new HttpQWenVLChatRequest.ChatThink();
            chatThink.setEnableThinking(Boolean.TRUE);
            chatRequest.setChatTemplateKwargs(chatThink);
        }
        
        chatRequest.setModel(getModel(request, aiProperties.getQwenVL().getModel()));
        chatRequest.setMessages(buildMultiModalMessages(request));
        chatRequest.setStream(isStream);
        chatRequest.setTemperature(getTemperature(request, aiProperties.getQwenVL().getTemperature()));
        chatRequest.setTopP(getTopP(request, aiProperties.getQwenVL().getTopP()));
        chatRequest.setMaxTokens(getMaxTokens(request, aiProperties.getQwenVL().getMaxTokens()));
        
        // 使用基类的参数合并方法
        mergeParamsToRequest(chatRequest, request.getParams());
        return chatRequest;
    }

    /**
     * 构建多模态消息列表
     *
     * @param request 请求参数
     * @return 消息列表
     */
    private List<HttpQWenVLChatRequest.Message> buildMultiModalMessages(ModelRequestVO request) {
        List<HttpQWenVLChatRequest.Message> messages = new ArrayList<>();
        
        // 如果设置了 systemPrompt，系统消息放在最前面
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().isEmpty()) {
            HttpQWenVLChatRequest.Message systemMessage = new HttpQWenVLChatRequest.Message();
            systemMessage.setRole(AiRole.SYSTEM.getValue());
            systemMessage.setContent(request.getSystemPrompt());
            messages.add(systemMessage);
        }
        
        // 如果请求中提供了消息列表，添加消息列表（跳过其中的 system 消息，因为已经在最前面添加了）
        if (!CollectionUtils.isEmpty(request.getMessages())) {
            for (ModelRequestVO.Message voMessage : request.getMessages()) {
                HttpQWenVLChatRequest.Message message = new HttpQWenVLChatRequest.Message();
                message.setRole(voMessage.getRole());
                // QWenVL 的 content 是 Object 类型，直接使用字符串
                message.setContent(voMessage.getContent());
                messages.add(message);
            }
            return messages;
        }
        
        // 否则使用原来的逻辑：contents 或 prompt
        HttpQWenVLChatRequest.Message message = new HttpQWenVLChatRequest.Message();
        message.setRole(AiRole.USER.getValue());

        // 优先使用 contents 字段（多模态）
        if (!CollectionUtils.isEmpty(request.getContents())) {
            List<Map<String, Object>> contentList = new ArrayList<>();
            for (MessageContent content : request.getContents()) {
                Map<String, Object> contentMap = convertToQWenContent(content);
                if (contentMap != null) {
                    contentList.add(contentMap);
                }
            }
            message.setContent(contentList);
        } else {
            // 降级到纯文本
            message.setContent(request.getPrompt());
        }

        messages.add(message);
        return messages;
    }

    /**
     * 将 MessageContent 转换为千问 VL API 的格式
     *
     * @param content 消息内容
     * @return 千问格式的内容对象
     */
    private Map<String, Object> convertToQWenContent(MessageContent content) {
        if (content == null || content.getType() == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        ContentType type = content.getType();

        switch (type) {
            case TEXT:
                if (content.getText() != null) {
                    result.put("type", "text");
                    result.put("text", content.getText());
                }
                break;
            case IMAGE_URL:
                if (content.getImageUrl() != null && content.getImageUrl().getUrl() != null) {
                    result.put("type", "image_url");
                    result.put("image_url", content.getImageUrl().getUrl());
                    // 千问VL不支持detail参数，忽略
                }
                break;
            case VIDEO_URL:
                if (content.getVideoUrl() != null) {
                    result.put("type", "video_url");
                    result.put("video_url", content.getVideoUrl());
                }
                break;
            case AUDIO_URL:
                if (content.getAudioUrl() != null) {
                    result.put("type", "audio_url");
                    result.put("audio_url", content.getAudioUrl());
                }
                break;
            case FILE_URL:
                if (content.getFileUrl() != null) {
                    result.put("type", "file_url");
                    result.put("file_url", content.getFileUrl());
                }
                break;
            case IMAGE_BASE64:
                log.warn("[QWenVL] 暂不支持 base64 图片格式，请使用图片URL");
                return null;
            default:
                log.warn("[QWenVL] 不支持的内容类型: {}", type);
                return null;
        }

        return result.isEmpty() ? null : result;
    }

    /**
     * 发送HTTP请求
     *
     * @param chatRequest 聊天请求对象
     * @return HTTP响应
     * @throws IOException 请求异常
     */
    private HttpQWenChatResponse sendHttpRequest(HttpQWenVLChatRequest chatRequest) throws IOException {
        logRequestParams(chatRequest);
        HttpUtils.HttpConfig config = createHttpConfig();
        return aiHttpUtils.post(
                aiProperties.getQwenVL().getEndpoint(),
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
        // 参数校验
        validateRequest(request);
        // 警告不支持的功能
        warnUnsupportedFeatures(request);

        HttpQWenVLChatRequest chatRequest = buildChatRequest(request, true);
        logRequestParams(chatRequest);
        StreamHttpUtils.StreamHttpConfig<HttpQWenVLChatRequest, ResultContent> config = createStreamConfig();

        return streamHttpUtils.postStream(
                aiProperties.getQwenVL().getEndpoint(),
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
                .apiKey(aiProperties.getQwenVL().getApiKey())
                .responseInterceptor(response ->
                        log.debug("[QWenVL] 响应状态: {}, 响应内容: {}", 
                                 response.getStatus(), truncateForLog(response.body(), 200)))
                .build();
    }

    /**
     * 创建流式配置
     *
     * @return 流式配置对象
     */
    private StreamHttpUtils.StreamHttpConfig<HttpQWenVLChatRequest, ResultContent> createStreamConfig() {
        return StreamHttpUtils.StreamHttpConfig.<HttpQWenVLChatRequest, ResultContent>builder()
                .apiKey(aiProperties.getQwenVL().getApiKey())
                .requestInterceptor(r -> r.header("X-Request-ID", UUID.randomUUID().toString()))
                .responseProcessor(new StreamQWenResponse())
                .build();
    }
}
