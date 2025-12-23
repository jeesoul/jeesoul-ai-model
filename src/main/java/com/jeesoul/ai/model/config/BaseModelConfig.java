package com.jeesoul.ai.model.config;

import lombok.Data;

/**
 * 基础模型配置类
 * 提供通用的配置属性，所有模型配置类可以继承此类
 * 自定义模型可以直接使用此类，或继承后扩展
 *
 * @author dxy
 * @date 2025-01-XX
 */
@Data
public class BaseModelConfig implements ModelConfig {
    /**
     * API 密钥
     */
    private String apiKey;

    /**
     * 服务端点
     */
    private String endpoint;

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

