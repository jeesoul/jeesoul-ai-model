package com.jeesoul.ai.model.request;

import lombok.Data;

import java.util.List;

/**
 * Spark 聊天请求体
 *
 * @author dxy
 * @date 2025-06-10
 */
@Data
public class HttpSparkChatRequest {
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
    private double temperature = 0.7;

    /**
     * 最大token数
     */
    private Integer maxTokens;

    /**
     * 是否流式输出，默认false
     */
    private boolean stream;

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
