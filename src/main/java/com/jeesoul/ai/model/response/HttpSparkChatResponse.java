package com.jeesoul.ai.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * Spark 聊天响应体
 * 继承自 HttpBaseChatResponse，用于处理讯飞星火大模型的响应
 *
 * @author dxy
 * @date 2025-06-10
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class HttpSparkChatResponse extends HttpBaseChatResponse {
    /**
     * 错误码：0表示成功，非0表示错误
     */
    private int code;
    /**
     * 本次请求的唯一id
     */
    private String sid;
    /**
     * ”complete“
     */
    private String status;
    /**
     * Spark选项类
     * 继承自BaseChoice，增加特定参数
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class SparkChoice extends BaseChoice {
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
    public static class SparkUsage extends Usage {
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
