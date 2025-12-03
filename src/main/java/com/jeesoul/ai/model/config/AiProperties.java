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
     * 通义千问视觉理解服务配置
     */
    private QWenVLProperties qwenVL = new QWenVLProperties();
    /**
     * 豆包（火山方舟）服务配置
     */
    private DouBaoProperties douBao = new DouBaoProperties();

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

        /**
         * 温度
         */
        private Double temperature = 0.7;

        /**
         * top_p
         */
        private Double topP = 0.9;

        /**
         * 最大token数
         */
        private Integer maxTokens = 2000;

        /**
         * 模型名称
         */
        private String model;
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

        /**
         * 温度
         */
        private Double temperature = 0.7;

        /**
         * top_p
         */
        private Double topP = 0.9;

        /**
         * 最大token数
         */
        private Integer maxTokens = 2000;

        /**
         * 模型名称
         */
        private String model;
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

        /**
         * 温度
         */
        private Double temperature = 0.7;

        /**
         * top_p
         */
        private Double topP = 0.9;

        /**
         * 最大token数
         */
        private Integer maxTokens = 2000;

        /**
         * 模型名称
         */
        private String model;
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

        /**
         * 温度
         */
        private Double temperature = 0.7;

        /**
         * top_p
         */
        private Double topP = 0.9;

        /**
         * 最大token数
         */
        private Integer maxTokens = 2000;

        /**
         * 模型名称
         */
        private String model;
    }

    /**
     * 通义千问视觉理解服务配置属性
     */
    @Data
    public static class QWenVLProperties {
        /**
         * API 密钥
         */
        private String apiKey;

        /**
         * 服务端点
         */
        private String endpoint = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";

        /**
         * 温度
         */
        private Double temperature = 0.7;

        /**
         * top_p
         */
        private Double topP = 0.9;

        /**
         * 最大token数
         */
        private Integer maxTokens = 2000;

        /**
         * 模型名称
         */
        private String model;
    }

    /**
     * 豆包（火山方舟）服务配置属性
     */
    @Data
    public static class DouBaoProperties {
        /**
         * API 密钥
         */
        private String apiKey;

        /**
         * 服务端点
         */
        private String endpoint = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";

        /**
         * 温度
         */
        private Double temperature = 0.7;

        /**
         * top_p
         */
        private Double topP = 0.9;

        /**
         * 最大token数
         */
        private Integer maxTokens = 2000;

        /**
         * 模型名称
         * 如：doubao-1-5-pro-32k-250115
         */
        private String model;
    }
}
