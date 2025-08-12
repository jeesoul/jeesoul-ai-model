package com.jeesoul.ai.model.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 模型配置
 *
 * @author dxy
 * @date 2025-06-10
 */
@Data
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
    /**
     * 讯飞星火服务配置
     */
    private SparkProperties spark = new SparkProperties();
    /**
     * 通义千问服务配置
     */
    private QWenProperties qwen = new QWenProperties();
    /**
     * DeepSeek服务配置
     */
    private DeepSeekProperties deepSeek = new DeepSeekProperties();
    /**
     * ChatGPT服务配置
     */
    private ChatGPTProperties chatGpt = new ChatGPTProperties();

    /**
     * 讯飞星火服务配置属性
     */
    @Data
    public static class SparkProperties {
        /**
         * API 密钥
         */
        private String apiKey;

        /**
         * WebSocket 服务端点
         */
        private String endpoint = "https://spark-api-open.xf-yun.com/v2/chat/completions";
    }

    /**
     * 通义千问服务配置属性
     */
    @Data
    public static class QWenProperties {
        /**
         * API 密钥
         */
        private String apiKey;

        /**
         * 服务端点
         */
        private String endpoint = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
    }

    /**
     * DeepSeek服务配置属性
     */
    @Data
    public static class DeepSeekProperties {
        /**
         * API 密钥
         */
        private String apiKey;

        /**
         * 服务端点
         */
        private String endpoint = "https://api.deepseek.com/v1/chat/completions";
    }

    /**
     * ChatGPT服务配置属性
     */
    @Data
    public static class ChatGPTProperties {
        /**
         * API 密钥
         */
        private String apiKey;

        /**
         * 服务端点
         */
        private String endpoint = "https://api.openai.com/v1/chat/completions";
    }
}
