package com.jeesoul.ai.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 豆包（火山方舟）HTTP聊天响应类
 * 兼容OpenAI Chat Completions API格式
 *
 * @author dxy
 * @date 2025-12-03
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class HttpDouBaoChatResponse extends HttpBaseResponse {
    /**
     * 选择列表
     */
    private List<Choice> choices;

    /**
     * Token使用统计
     */
    private Usage usage;

    /**
     * 服务层级
     */
    @JsonProperty("service_tier")
    private String serviceTier;

    /**
     * 选择实体类
     */
    @Data
    public static class Choice {
        /**
         * 索引
         */
        private Integer index;

        /**
         * 消息
         */
        private Message message;

        /**
         * 完成原因
         */
        @JsonProperty("finish_reason")
        private String finishReason;

        /**
         * log概率
         */
        private Object logprobs;

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
             * 内容（最终回答）
             */
            private String content;

            /**
             * 推理内容（思考过程）
             * 仅在启用思考模式时有值
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

        /**
         * 提示词token详情
         */
        @JsonProperty("prompt_tokens_details")
        private PromptTokensDetails promptTokensDetails;

        /**
         * 完成token详情
         */
        @JsonProperty("completion_tokens_details")
        private CompletionTokensDetails completionTokensDetails;

        /**
         * 提示词token详情类
         */
        @Data
        public static class PromptTokensDetails {
            /**
             * 缓存的token数
             */
            @JsonProperty("cached_tokens")
            private Integer cachedTokens;
        }

        /**
         * 完成token详情类
         */
        @Data
        public static class CompletionTokensDetails {
            /**
             * 推理token数（思考模式）
             */
            @JsonProperty("reasoning_tokens")
            private Integer reasoningTokens;
        }
    }
}

