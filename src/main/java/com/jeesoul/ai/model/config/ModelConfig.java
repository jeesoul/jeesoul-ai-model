package com.jeesoul.ai.model.config;

/**
 * 模型配置接口
 * 定义获取模型配置的通用方法，支持自定义模型独立管理配置
 * 自定义模型可以实现此接口，无需修改 AiProperties
 *
 * @author dxy
 * @date 2025-01-XX
 */
public interface ModelConfig {
    /**
     * 获取 API 密钥
     *
     * @return API 密钥
     */
    String getApiKey();

    /**
     * 获取服务端点
     *
     * @return 服务端点 URL
     */
    String getEndpoint();

    /**
     * 获取温度参数
     *
     * @return 温度值，如果未配置则返回 null
     */
    Double getTemperature();

    /**
     * 获取 topP 参数
     *
     * @return topP 值，如果未配置则返回 null
     */
    Double getTopP();

    /**
     * 获取最大 token 数
     *
     * @return 最大 token 数，如果未配置则返回 null
     */
    Integer getMaxTokens();

    /**
     * 获取默认模型名称
     *
     * @return 默认模型名称，如果未配置则返回 null
     */
    String getModel();
}

