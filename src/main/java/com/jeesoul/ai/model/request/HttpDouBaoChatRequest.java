package com.jeesoul.ai.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 豆包（火山方舟）HTTP聊天请求类
 * 兼容OpenAI Chat Completions API格式
 *
 * @author dxy
 * @date 2025-12-03
 */
@Data
public class HttpDouBaoChatRequest {
    /**
     * 模型名称
     * 如：doubao-1-5-pro-32k-250115
     */
    private String model;

    /**
     * 消息列表
     */
    private List<Message> messages;

    /**
     * 是否开启流式输出
     */
    private Boolean stream;

    /**
     * 采样温度
     * 取值范围：0.0-2.0
     */
    private Double temperature;

    /**
     * 核采样参数
     * 取值范围：0.0-1.0
     */
    @JsonProperty("top_p")
    private Double topP;

    /**
     * 最大生成Token数
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    /**
     * 深度思考配置
     * 控制模型是否启用深度思考模式
     * 默认值：{"type":"enabled"}
     */
    private ThinkingConfig thinking;

    /**
     * 消息实体类
     */
    @Data
    public static class Message {
        /**
         * 角色
         * system, user, assistant
         */
        private String role;

        /**
         * 消息内容
         * 可以是：
         * 1. String - 纯文本内容
         * 2. List<ContentPart> - 多模态内容（文本、图片等）
         */
        private Object content;
    }

    /**
     * 内容部分（用于多模态）
     */
    @Data
    public static class ContentPart {
        /**
         * 内容类型
         * text, image_url等
         */
        private String type;

        /**
         * 文本内容（当type为text时）
         */
        private String text;

        /**
         * 图片URL（当type为image_url时）
         */
        @JsonProperty("image_url")
        private String imageUrl;

        /**
         * 创建文本内容
         *
         * @param text 文本
         * @return ContentPart实例
         */
        public static ContentPart text(String text) {
            ContentPart part = new ContentPart();
            part.setType("text");
            part.setText(text);
            return part;
        }

        /**
         * 创建图片内容
         *
         * @param imageUrl 图片URL
         * @return ContentPart实例
         */
        public static ContentPart image(String imageUrl) {
            ContentPart part = new ContentPart();
            part.setType("image_url");
            part.setImageUrl(imageUrl);
            return part;
        }
    }

    /**
     * 深度思考配置类
     * 用于控制豆包模型的深度思考行为
     */
    @Data
    public static class ThinkingConfig {
        /**
         * 思考模式类型
         * - enabled: 开启思考模式，模型将进行深度推理
         * - disabled: 关闭思考模式，模型将直接回答
         * - auto: 自动思考模式，模型根据问题自动判断是否需要思考
         */
        private String type;

        /**
         * 创建启用思考的配置
         *
         * @return ThinkingConfig实例
         */
        public static ThinkingConfig enabled() {
            ThinkingConfig config = new ThinkingConfig();
            config.setType("enabled");
            return config;
        }

        /**
         * 创建禁用思考的配置
         *
         * @return ThinkingConfig实例
         */
        public static ThinkingConfig disabled() {
            ThinkingConfig config = new ThinkingConfig();
            config.setType("disabled");
            return config;
        }

        /**
         * 创建自动思考的配置
         *
         * @return ThinkingConfig实例
         */
        public static ThinkingConfig auto() {
            ThinkingConfig config = new ThinkingConfig();
            config.setType("auto");
            return config;
        }
    }
}

