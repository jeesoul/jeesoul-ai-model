package com.jeesoul.ai.model.util;

import com.jeesoul.ai.model.response.StreamBaseResponse;
import io.netty.channel.ChannelOption;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 流式HTTP请求工具类
 * 用于处理与LLM服务相关的流式HTTP请求，支持Server-Sent Events (SSE)
 *
 * @author dxy
 * @date 2025-06-10
 */
@Slf4j
public class StreamHttpUtils {
    /**
     * 默认的HTTP请求头
     */
    private static final Map<String, String> DEFAULT_HEADERS = new HashMap<>();

    static {
        DEFAULT_HEADERS.put(HttpHeaders.CONTENT_TYPE, "application/json");
        DEFAULT_HEADERS.put(HttpHeaders.ACCEPT, "text/event-stream");
        DEFAULT_HEADERS.put(HttpHeaders.CACHE_CONTROL, "no-cache");
        DEFAULT_HEADERS.put(HttpHeaders.CONNECTION, "keep-alive");
    }

    /**
     * 发送流式POST请求
     *
     * @param <T>    请求体类型，用于指定请求数据的类型
     * @param <R>    响应处理后的类型，用于指定响应数据的类型
     * @param url    请求URL
     * @param body   请求体
     * @param config 请求配置
     * @return 响应数据流
     */
    public <T, R> Flux<R> postStream(String url, T body, StreamHttpConfig<T, R> config) {
        try {
            // 创建WebClient
            WebClient webClient = WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                            .responseTimeout(Duration.ofMillis(config.getReadTimeout()))
                            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeout())))
                    .build();

            // 构建请求URL（包含查询参数）
            String finalUrl = url;
            if (config.getQueryParams() != null && !config.getQueryParams().isEmpty()) {
                StringBuilder urlBuilder = new StringBuilder(url);
                urlBuilder.append("?");
                config.getQueryParams().forEach((key, value) ->
                        urlBuilder.append(key).append("=").append(value).append("&"));
                finalUrl = urlBuilder.substring(0, urlBuilder.length() - 1);
            }

            // 构建请求
            WebClient.RequestBodySpec requestSpec = webClient.post()
                    .uri(finalUrl)
                    .headers(headers -> {
                        // 添加默认请求头
                        DEFAULT_HEADERS.forEach(headers::add);
                        // 添加自定义请求头
                        if (config.getHeaders() != null) {
                            config.getHeaders().forEach(headers::add);
                        }
                        // 添加API密钥
                        if (config.getApiKey() != null) {
                            headers.add("Authorization", "Bearer " + config.getApiKey());
                        }
                    });

            // 应用请求拦截器
            if (config.getRequestInterceptor() != null) {
                config.getRequestInterceptor().accept(requestSpec);
            }

            // 发送请求并处理响应
            return requestSpec.body(BodyInserters.fromValue(Objects.requireNonNull(JsonUtils.toJson(body))))
                    .retrieve()
                    .bodyToFlux(String.class)
                    .filter(data -> !data.trim().isEmpty())
                    .flatMap(data -> {
                        try {
                            // 使用响应处理器处理数据
                            if (config.getResponseProcessor() != null) {
                                return config.getResponseProcessor().process(data);
                            }
                            return Flux.empty();
                        } catch (Exception e) {
                            log.error("处理响应数据失败: {}", e.getMessage());
                            return Flux.empty();
                        }
                    })
                    .doOnError(e -> {
                        log.error("流式请求处理失败: {}", e.getMessage());
                        if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                            org.springframework.web.reactive.function.client.WebClientResponseException ex =
                                    (org.springframework.web.reactive.function.client.WebClientResponseException) e;
                            log.error("错误响应状态码: {}, 错误响应体: {}",
                                    ex.getStatusCode(),
                                    ex.getResponseBodyAsString());
                        }
                    });

        } catch (Exception e) {
            log.error("创建流式请求失败: {}", e.getMessage());
            return Flux.error(e);
        }
    }

    /**
     * 发送流式POST请求（原始响应版本）
     * 用于返回原始模型的响应数据（JSON字符串）
     *
     * @param <T>    请求体类型，用于指定请求数据的类型
     * @param url    完整的请求URL
     * @param body   请求体
     * @param config 流式HTTP配置
     * @return 原始响应数据流（JSON字符串）
     */
    public <T> Flux<String> postStreamRaw(String url, T body, StreamHttpConfig<T, String> config) {
        // 创建WebClient
        WebClient webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .responseTimeout(Duration.ofMillis(config.getReadTimeout()))
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeout())))
                .build();

        // 构建请求URL（包含查询参数）
        String finalUrl = url;
        if (config.getQueryParams() != null && !config.getQueryParams().isEmpty()) {
            StringBuilder urlBuilder = new StringBuilder(url);
            urlBuilder.append("?");
            config.getQueryParams().forEach((key, value) ->
                    urlBuilder.append(key).append("=").append(value).append("&"));
            finalUrl = urlBuilder.substring(0, urlBuilder.length() - 1);
        }

        // 构建请求
        WebClient.RequestBodySpec requestSpec = webClient.post()
                .uri(finalUrl)
                .headers(headers -> {
                    // 添加默认请求头
                    DEFAULT_HEADERS.forEach(headers::add);
                    // 添加自定义请求头
                    if (config.getHeaders() != null) {
                        config.getHeaders().forEach(headers::add);
                    }
                    // 添加API密钥
                    if (config.getApiKey() != null) {
                        headers.add("Authorization", "Bearer " + config.getApiKey());
                    }
                });

        // 应用请求拦截器
        if (config.getRequestInterceptor() != null) {
            config.getRequestInterceptor().accept(requestSpec);
        }

        // 发送请求并返回原始响应流（JSON字符串）
        return requestSpec.body(BodyInserters.fromValue(Objects.requireNonNull(JsonUtils.toJson(body))))
                .retrieve()
                .bodyToFlux(String.class)
                .filter(data -> !data.trim().isEmpty())
                .mapNotNull(data -> {
                    // 处理SSE格式：去掉 "data:" 前缀
                    if (data.startsWith("data:")) {
                        String jsonData = data.substring(5).trim();
                        // 检查是否是结束标记
                        if ("[DONE]".equals(jsonData)) {
                            return null;
                        }
                        return jsonData;
                    }
                    return data;
                })
                .filter(Objects::nonNull)
                .doOnError(e -> {
                    log.error("流式请求处理失败: {}", e.getMessage());
                    if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                        org.springframework.web.reactive.function.client.WebClientResponseException ex =
                                (org.springframework.web.reactive.function.client.WebClientResponseException) e;
                        log.error("错误响应状态码: {}, 错误响应体: {}",
                                ex.getStatusCode(),
                                ex.getResponseBodyAsString());
                    }
                });
    }

    /**
     * 流式HTTP请求配置类
     * 用于配置流式请求的参数，包括API密钥、请求拦截器和响应处理器
     *
     * @param <T> 请求体类型，用于指定请求数据的类型
     * @param <R> 响应处理后的类型，用于指定响应数据的类型
     */
    @Data
    @Builder
    public static class StreamHttpConfig<T, R> {
        /**
         * 查询参数
         */
        private Map<String, String> queryParams;

        /**
         * 自定义请求头
         */
        private Map<String, String> headers;

        /**
         * API密钥
         */
        private String apiKey;

        /**
         * 请求拦截器
         */
        private Consumer<WebClient.RequestBodySpec> requestInterceptor;

        /**
         * 响应处理器
         */
        private StreamBaseResponse<R> responseProcessor;

        /**
         * 连接超时时间（毫秒）
         */
        @Builder.Default
        private int connectTimeout = 50000;

        /**
         * 读取超时时间（毫秒）
         */
        @Builder.Default
        private int readTimeout = 300000;
    }
}