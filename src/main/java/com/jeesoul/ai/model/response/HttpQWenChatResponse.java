package com.jeesoul.ai.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * QWen 聊天响应体，继承自 BaseResponse，包含选项和使用情况等信息
 *
 * @author dxy
 * @date 2025-06-10
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class HttpQWenChatResponse extends HttpBaseChatResponse {
    /**
     * QWen 选项类
     * 继承自BaseChoice，增加特定参数
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class QWenChoice extends HttpBaseChatResponse.BaseChoice {
        /**
         * 特定参数
         */
        private Map<String, Object> parameters;
    }

    /**
     * Spark使用情况类
     * 继承自Usage，增加输入输出token数
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class QWenUsage extends HttpBaseChatResponse.Usage {
        /**
         * 输入token数
         */
        private Integer inputTokens;

        /**
         * 输出token数
         */
        private Integer outputTokens;
    }
}

