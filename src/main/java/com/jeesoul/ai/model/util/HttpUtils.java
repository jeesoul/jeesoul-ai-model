package com.jeesoul.ai.model.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import com.jeesoul.ai.model.response.HttpBaseResponse;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * HTTP 请求工具类，用于处理与 LLM 服务相关的 HTTP 请求
 *
 * @author dxy
 * @date 2025-06-10
 */
@Slf4j
public class HttpUtils {
    /**
     * 默认的 HTTP 请求头
     */
    private static final Map<String, String> DEFAULT_HEADERS = new HashMap<>();

    static {
        DEFAULT_HEADERS.put("Content-Type", "application/json");
    }

    /**
     * 发送 POST 请求
     *
     * @param <T>          请求体类型
     * @param <R>          响应类型，必须是 BaseResponse 的子类
     * @param url          请求URL
     * @param headers      请求头
     * @param body         请求体
     * @param responseType 响应类型
     * @param config       HTTP配置
     * @return 响应对象
     * @throws IOException 当请求失败时抛出
     */
    public <T, R extends HttpBaseResponse> R post(String url, Map<String, String> headers, T body,
                                                  Class<R> responseType, HttpConfig config) throws IOException {
        return request(url, Method.POST, headers, body, responseType, config);
    }

    /**
     * 发送 GET 请求
     *
     * @param <R>          响应类型，必须是 BaseResponse 的子类
     * @param url          请求URL
     * @param headers      请求头
     * @param responseType 响应类型
     * @param config       HTTP配置
     * @return 响应对象
     * @throws IOException 当请求失败时抛出
     */
    public <R extends HttpBaseResponse> R get(String url, Map<String, String> headers,
                                              Class<R> responseType, HttpConfig config) throws IOException {
        return request(url, Method.GET, headers, null, responseType, config);
    }

    /**
     * 发送 HTTP 请求
     *
     * @param <T>          请求体类型
     * @param <R>          响应类型，必须是 BaseResponse 的子类
     * @param url          请求URL
     * @param method       HTTP方法
     * @param headers      请求头
     * @param body         请求体
     * @param responseType 响应类型
     * @param config       HTTP配置
     * @return 响应对象
     * @throws IOException 当请求失败时抛出
     */
    private <T, R extends HttpBaseResponse> R request(
            String url,
            Method method,
            Map<String, String> headers,
            T body,
            Class<R> responseType,
            HttpConfig config) throws IOException {
        try {
            // 创建请求
            HttpRequest request = createRequest(url, method, headers, body, config);
            // 发送请求并处理响应
            return handleResponse(request, responseType, config);
        } catch (Exception e) {
            log.error("HTTP请求失败: {}", url, e);
            throw new IOException("HTTP请求失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建 HTTP 请求
     *
     * @param <T>     请求体类型
     * @param url     请求URL
     * @param method  HTTP方法
     * @param headers 请求头
     * @param body    请求体
     * @param config  HTTP配置
     * @return HTTP请求对象
     * @throws IOException 当请求创建失败时抛出
     */
    private <T> HttpRequest createRequest(
            String url,
            Method method,
            Map<String, String> headers,
            T body,
            HttpConfig config) throws IOException {
        // 创建请求
        HttpRequest request = HttpRequest.of(url)
                .timeout(config.getTimeout())
                .method(method);
        // 设置默认请求头
        DEFAULT_HEADERS.forEach(request::header);
        // 设置 API Key
        if (config.getApiKey() != null) {
            request.header("Authorization", "Bearer " + config.getApiKey());
        }
        // 设置自定义请求头（覆盖默认值）
        if (headers != null) {
            headers.forEach(request::header);
        }
        // 设置请求体
        if (body != null) {
            request.body(JsonUtils.toJson(body));
        }
        // 设置SSL
        if (config.isEnableSSL()) {
            request.setSSLProtocol("TLS");
        }
        // 应用请求拦截器
        if (config.getRequestInterceptor() != null) {
            config.getRequestInterceptor().accept(request);
        }
        return request;
    }

    /**
     * 处理 HTTP 响应
     *
     * @param <R>          响应类型，必须是 BaseResponse 的子类
     * @param request      HTTP请求对象
     * @param responseType 响应类型
     * @param config       HTTP配置
     * @return 响应对象
     * @throws IOException 当响应处理失败时抛出
     */
    private <R extends HttpBaseResponse> R handleResponse(
            HttpRequest request,
            Class<R> responseType,
            HttpConfig config) throws IOException {
        // 发送请求
        HttpResponse response = request.execute();
        // 应用响应拦截器
        if (config.getResponseInterceptor() != null) {
            config.getResponseInterceptor().accept(response);
        }
        // 检查响应状态
        if (!response.isOk()) {
            throw new IOException("HTTP请求失败: " + response.getStatus() + " - " + response.body());
        }
        // 解析响应
        String responseBody = response.body();
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return null;
        }
        // 转换响应
        R result = JsonUtils.fromJson(responseBody, responseType);
        // 设置响应信息
        if (result != null) {
            result.setHttpStatus(response.getStatus());
        }
        return result;
    }

    /**
     * HTTP 请求配置类
     * 用于配置 HTTP 请求的各种参数，如超时时间、SSL 设置、API 密钥等
     */
    @Data
    @Builder
    public static class HttpConfig {
        /**
         * 请求超时时间（毫秒）
         */
        private int timeout = 60000;

        /**
         * 是否启用 SSL
         */
        private boolean enableSSL;

        /**
         * API 密钥
         */
        private String apiKey;

        /**
         * 自定义请求头
         */
        private Map<String, String> headers;

        /**
         * 请求拦截器
         */
        private Consumer<HttpRequest> requestInterceptor;

        /**
         * 响应拦截器
         */
        private Consumer<HttpResponse> responseInterceptor;
    }
}

