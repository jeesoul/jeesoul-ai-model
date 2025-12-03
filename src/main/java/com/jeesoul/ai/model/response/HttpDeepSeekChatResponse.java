package com.jeesoul.ai.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * DeepSeek HTTP聊天响应类
 *
 * @author dxy
 * @date 2025-06-10
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HttpDeepSeekChatResponse extends HttpBaseResponse {
    /**
     * 选择列表
     */
    private List<Choice> choices;

    /**
     * Token使用统计
     */
    private Usage usage;

    /**
     * 选择实体类
     */
    @Data
    public static class Choice {
        /**
         * 消息
         */
        private Message message;

        /**
         * 完成原因
         */
        private String finishReason;

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

            /**
             * 思考过程内容（reasoning_content）
             * 仅在启用思考模式时有值
             * 文档：https://api-docs.deepseek.com/zh-cn/guides/thinking_mode
             */
            @JsonProperty("reasoning_content")
            private String reasoningContent;
        }
    }

    /**
     * Token使用统计类
     */
    @Data
    public static class Usage {
        /**
         * 提示词token数
         */
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;

        /**
         * 完成token数
         */
        @JsonProperty("completion_tokens")
        private Integer completionTokens;

        /**
         * 总token数
         */
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}
