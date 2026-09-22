package com.jeesoul.ai.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("top_p")
    private Double topP;
    /**
     * 最大token数
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    /**
     * 新版模型最大输出token数
     */
    @JsonProperty("max_completion_tokens")
    private Integer maxCompletionTokens;

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
        @JsonIgnore
        private Object content;

        /**
         * 获取兼容历史调用的纯文本内容
         *
         * @return 纯文本内容，内容数组时返回null
         */
        @JsonIgnore
        public String getContent() {
            return content instanceof String ? (String) content : null;
        }

        /**
         * 设置兼容历史调用的纯文本内容
         *
         * @param content 纯文本内容
         */
        @JsonIgnore
        public void setContent(String content) {
            this.content = content;
        }

        /**
         * 获取发送给OpenAI的内容字段
         *
         * @return 纯文本或多模态内容数组
         */
        @JsonProperty("content")
        public Object getApiContent() {
            return content;
        }

        /**
         * 设置发送给OpenAI的多模态内容数组
         *
         * @param contentParts 内容数组
         */
        public void setContentParts(List<ContentPart> contentParts) {
            this.content = contentParts;
        }
    }

    /**
     * OpenAI消息内容片段
     */
    @Data
    public static class ContentPart {
        /**
         * 内容类型，支持text和image_url
         */
        private String type;

        /**
         * 文本内容
         */
        private String text;

        /**
         * 图片内容
         */
        @JsonProperty("image_url")
        private ImageUrl imageUrl;

        /**
         * OpenAI图片地址内容
         */
        @Data
        public static class ImageUrl {
            /**
             * 完整图片URL或Base64 Data URL
             */
            private String url;

            /**
             * 图片详细度
             */
            private String detail;
        }
    }
}
