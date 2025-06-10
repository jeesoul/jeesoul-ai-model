package com.jeesoul.ai.model.response;

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
        }
    }
}
