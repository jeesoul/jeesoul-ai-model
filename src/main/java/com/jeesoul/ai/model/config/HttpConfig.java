package com.jeesoul.ai.model.config;

import com.jeesoul.ai.model.util.HttpUtils;
import com.jeesoul.ai.model.util.StreamHttpUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HTTP 相关配置类
 * 提供HTTP工具类的Bean配置，包括：
 * 1. 普通HTTP请求工具类（HttpUtils）
 * 2. 流式HTTP请求工具类（StreamHttpUtils）
 * 这些工具类用于处理与AI服务的HTTP通信，支持同步和流式响应
 *
 * @author dxy
 * @date 2025-06-10
 */
@Configuration
public class HttpConfig {

    /**
     * 配置普通HTTP请求工具类Bean
     * 用于处理同步HTTP请求，如普通的API调用
     * 支持：
     * 1. 请求/响应拦截
     * 2. 错误处理
     * 3. JSON序列化/反序列化
     *
     * @return HttpUtils实例
     */
    @Bean
    public HttpUtils httpUtils() {
        return new HttpUtils();
    }

    /**
     * 配置流式HTTP请求工具类Bean
     * 用于处理流式HTTP请求，如SSE（Server-Sent Events）响应
     * 支持：
     * 1. 流式数据处理
     * 2. 超时控制
     * 3. 自定义响应处理
     * 4. 错误处理
     *
     * @return StreamHttpUtils实例
     */
    @Bean
    public StreamHttpUtils kyqbStreamHttpUtils() {
        return new StreamHttpUtils();
    }
}