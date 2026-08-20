package com.jeesoul.ai.model.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * HTTP 客户端配置属性
 * 用于配置 HttpClientEngine 的连接池和超时参数，所有参数均可通过 YML 配置文件调整。
 *
 * @author dxy
 * @date 2025-06-18
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.http")
public class HttpClientProperties {

    /**
     * 连接池配置
     */
    private Pool pool = new Pool();

    /**
     * 超时配置（毫秒）
     */
    private Timeout timeout = new Timeout();

    /**
     * 连接保活配置
     */
    private KeepAlive keepAlive = new KeepAlive();

    /**
     * 连接池配置
     */
    @Data
    public static class Pool {
        /**
         * 连接池最大连接数
         * 默认 200
         */
        private int maxTotal = 200;

        /**
         * 每个路由最大连接数
         * 默认 200
         */
        private int maxPerRoute = 200;

        /**
         * 空闲连接回收阈值（秒）
         * 默认 30 秒
         */
        private long evictIdleSeconds = 30L;

        /**
         * 连接最长存活时间（秒）
         * 默认 30 秒
         */
        private long timeToLiveSeconds = 30L;
    }

    /**
     * 超时配置（毫秒）
     */
    @Data
    public static class Timeout {
        /**
         * 连接超时（毫秒）
         * 默认 5000 毫秒
         */
        private int connect = 5000;

        /**
         * Socket 读取超时（毫秒）
         * 默认 10000 毫秒
         */
        private int socket = 10000;

        /**
         * 从连接池获取连接的超时（毫秒）
         * 默认 5000 毫秒
         */
        private int connectionRequest = 5000;
    }

    /**
     * 连接保活配置
     */
    @Data
    public static class KeepAlive {
        /**
         * 连接保活时间（毫秒），服务端未指定时使用
         * 默认 20000 毫秒
         */
        private long duration = 20000L;

        /**
         * 是否启用自动重试
         * 默认 false（不重试，适合 LLM 调用场景）
         */
        private boolean enableRetry = false;
    }
}
