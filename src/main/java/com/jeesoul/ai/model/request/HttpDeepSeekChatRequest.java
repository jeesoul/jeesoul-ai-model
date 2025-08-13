package com.jeesoul.ai.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DeepSeek HTTP聊天请求类
 *
 * @author dxy
 * @date 2025-06-10
 */
@Data
public class HttpDeepSeekChatRequest {
    /**
     * 模型名称
     */
    private String model;

    /**
     * 消息列表
     */
    private List<Message> messages;
    /**
     * 温度
     */
    private Double temperature;
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
     * 是否使用流式响应
     */
    private boolean stream;

    /**
     * 消息实体类
     */
    @Data
    public static class Message {
        /**
         * 角色
         */
        private String role;

        /**
         * 内容
         */
        private String content;
    }
}