package com.jeesoul.ai.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
     * 思考配置（Spark专用）
     * 用于控制深度思考模式
     * 支持：enabled（开启）、disabled（关闭）、auto（自动判断）
     */
    private ThinkingConfig thinking;

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

    /**
     * 思考配置类
     * 用于控制Spark的深度思考模式
     */
    @Data
    public static class ThinkingConfig {
        /**
         * 思考类型：enabled、disabled、auto
         */
        private String type;

        /**
         * 创建启用思考的配置
         */
        public static ThinkingConfig enabled() {
            ThinkingConfig config = new ThinkingConfig();
            config.setType("enabled");
            return config;
        }

        /**
         * 创建禁用思考的配置
         */
        public static ThinkingConfig disabled() {
            ThinkingConfig config = new ThinkingConfig();
            config.setType("disabled");
            return config;
        }

        /**
         * 创建自动判断的配置
         */
        public static ThinkingConfig auto() {
            ThinkingConfig config = new ThinkingConfig();
            config.setType("auto");
            return config;
        }
    }
}
