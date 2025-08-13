package com.jeesoul.ai.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * * QWen 聊天请求体
 *
 * @author dxy
 * @date 2025-06-10
 */
@Data
public class HttpQWenChatRequest {
    /**
     * 消息列表
     */
    private List<Message> messages;
    /**
     * 模型类别
     */
    private String model;

    /**
     * 采样温度，影响生成内容的多样性，默认0.7
     */
    private Double temperature = 0.7;
    /**
     * top_p
     */
    @JsonProperty("top_p")
    private Double topP;
    /**
     * 最大token数
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    /**
     * 是否流式输出，默认false
     */
    private boolean stream;
    /**
     * stream_options
     */
    @JsonProperty("stream_options")
    private StreamOptions streamOptions;
    /**
     * enable_thinking为true来开启思考模式
     */
    @JsonProperty("enable_thinking")
    private boolean enableThinking;
    /**
     * 思考开关
     */
    @JsonProperty("chat_template_kwargs")
    private ChatThink chatTemplateKwargs = new ChatThink();

    /**
     * stream_options
     */
    @Data
    public static class StreamOptions {
        /**
         * include_usage
         */
        @JsonProperty("include_usage")
        private Boolean includeUsage;
    }

    /**
     * 思考参数设置
     */
    @Data
    public static class ChatThink {
        /**
         * enable_thinking为true来开启思考模式
         */
        @JsonProperty("enable_thinking")
        private boolean enableThinking;
    }

    /**
     * 消息对象，包含角色和内容
     */
    @Data
    public static class Message {
        /**
         * 角色（如 user、assistant）
         */
        private String role;

        /**
         * 消息内容
         */
        private String content;
    }
}
