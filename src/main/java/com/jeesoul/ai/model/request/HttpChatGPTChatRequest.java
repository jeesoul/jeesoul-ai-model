package com.jeesoul.ai.model.request;

import lombok.Data;

import java.util.List;

/**
 * ChatGPT HTTP请求对象
 *
 * @author dxy
 * @date 2025-06-10
 */
@Data
public class HttpChatGPTChatRequest {
    /**
     * 模型名称
     */
    private String model;
    /**
     * 消息列表
     */
    private List<Message> messages;
    /**
     * 是否流式
     */
    private boolean stream;
    /**
     * 温度
     */
    private Double temperature;
    /**
     * top_p
     */
    private Double topP;
    /**
     * 最大token数
     */
    private Integer maxTokens;

    /**
     * 消息对象
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