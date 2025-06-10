package com.jeesoul.ai.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 基础聊天响应体
 * 包含响应ID、选项列表和使用情况等信息
 *
 * @author dxy
 * @date 2025-06-10
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HttpBaseChatResponse extends HttpBaseResponse {
    /**
     * 选项列表
     */
    private List<BaseChoice> choices;

    /**
     * 使用情况
     */
    private Usage usage;

    /**
     * 基础选项类
     * 描述每个选项的内容、索引和结束原因
     */
    @Data
    public static class BaseChoice {
        /**
         * 选项索引
         */
        private Integer index;

        /**
         * 消息内容
         */
        private Message message;

        /**
         * 结束原因
         */
        @JsonProperty("finish_reason")
        private String finishReason;

        /**
         * 消息类
         * 包含角色和内容
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

    /**
     * 使用情况类
     * 描述token的使用统计
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
