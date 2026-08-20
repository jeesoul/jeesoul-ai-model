package com.jeesoul.ai.model.http.engine;

import com.jeesoul.ai.model.http.exception.HttpException;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * HTTP 客户端引擎
 * 基于 Apache HttpClient 5.x 实现，持有连接池化的单例客户端，对外提供请求执行能力。
 * 设计参考企业级封装实践：连接池复用、连接保活、三级超时、空闲连接回收、不自动重试。
 *
 * @author dxy
 * @date 2025-06-18
 */
public final class HttpClientEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientEngine.class);

    /**
     * 连接池最大连接数
     */
    private static final int DEFAULT_MAX_CONN = 200;
    /**
     * 每个路由最大连接数
     */
    private static final int DEFAULT_CONN_PER_ROUTE = 200;
    /**
     * 连接超时（毫秒）
     */
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    /**
     * Socket 读取超时（毫秒）
     */
    private static final int DEFAULT_SOCKET_TIMEOUT = 10000;
    /**
     * 从连接池获取连接的超时（毫秒）
     */
    private static final int DEFAULT_CONN_REQUEST_TIMEOUT = 5000;
    /**
     * 连接保活时间（毫秒），服务端未指定时使用
     */
    private static final long DEFAULT_KEEP_ALIVE = 20000L;
    /**
     * 空闲连接回收阈值（秒）
     */
    private static final long EVICT_IDLE_SECONDS = 30L;
    /**
     * 连接最长存活时间（秒）
     */
    private static final long CONN_TIME_TO_LIVE_SECONDS = 30L;

    /**
     * 单例客户端
     */
    private static final CloseableHttpClient HTTP_CLIENT = buildClient();

    /**
     * 私有构造，禁止实例化
     */
    private HttpClientEngine() {
        throw new UnsupportedOperationException("引擎类不允许实例化");
    }

    /**
     * 构建连接池化的 HttpClient 单例
     *
     * @return CloseableHttpClient
     */
    private static CloseableHttpClient buildClient() {
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(DEFAULT_CONNECT_TIMEOUT))
                .setSocketTimeout(Timeout.ofMilliseconds(DEFAULT_SOCKET_TIMEOUT))
                .setTimeToLive(TimeValue.ofSeconds(CONN_TIME_TO_LIVE_SECONDS))
                .build();

        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(DEFAULT_MAX_CONN)
                .setMaxConnPerRoute(DEFAULT_CONN_PER_ROUTE)
                .setDefaultConnectionConfig(connectionConfig)
                .build();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(DEFAULT_CONN_REQUEST_TIMEOUT))
                .setResponseTimeout(Timeout.ofMilliseconds(DEFAULT_SOCKET_TIMEOUT))
                .build();

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .setKeepAliveStrategy((response, context) -> TimeValue.ofMilliseconds(DEFAULT_KEEP_ALIVE))
                .evictIdleConnections(TimeValue.ofSeconds(EVICT_IDLE_SECONDS))
                .disableAutomaticRetries()
                .build();
    }

    /**
     * 执行 HTTP 请求
     *
     * @param method        HTTP 方法名（GET/POST 等）
     * @param url           请求地址
     * @param headers       请求头，可为 null
     * @param jsonBody      请求体（JSON 字符串），可为 null（GET 等无体请求）
     * @param timeoutMillis 响应超时（毫秒），大于 0 时覆盖默认值
     * @return 执行结果（状态码 + 响应体）
     */
    public static Result execute(String method, String url, Map<String, String> headers,
                                 String jsonBody, int timeoutMillis) {
        ClassicHttpRequest request = buildRequest(method, url, headers, jsonBody, timeoutMillis);
        long startTime = System.currentTimeMillis();
        try {
            return HTTP_CLIENT.execute(request, (ClassicHttpResponse response) -> {
                int code = response.getCode();
                String body = response.getEntity() == null
                        ? null
                        : EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                long cost = System.currentTimeMillis() - startTime;
                LOGGER.debug("[HttpClientEngine]|{}|{}|{}ms|{}", method, url, cost, code);
                return new Result(code, body);
            });
        } catch (Exception e) {
            throw new HttpException("HTTP请求执行失败: " + url, e);
        }
    }

    /**
     * 构建请求对象
     *
     * @param method        HTTP 方法名
     * @param url           请求地址
     * @param headers       请求头
     * @param jsonBody      请求体
     * @param timeoutMillis 响应超时（毫秒）
     * @return ClassicHttpRequest
     */
    private static ClassicHttpRequest buildRequest(String method, String url, Map<String, String> headers,
                                                   String jsonBody, int timeoutMillis) {
        HttpUriRequestBase request = new HttpUriRequestBase(method, URI.create(url));
        if (headers != null) {
            headers.forEach(request::setHeader);
        }
        if (jsonBody != null) {
            request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
        }
        if (timeoutMillis > 0) {
            request.setConfig(RequestConfig.custom()
                    .setConnectionRequestTimeout(Timeout.ofMilliseconds(DEFAULT_CONN_REQUEST_TIMEOUT))
                    .setResponseTimeout(Timeout.ofMilliseconds(timeoutMillis))
                    .build());
        }
        return request;
    }

    /**
     * 请求执行结果
     */
    public static final class Result {
        /**
         * HTTP 状态码
         */
        private final int status;
        /**
         * 响应体字符串
         */
        private final String body;

        /**
         * 构造结果
         *
         * @param status 状态码
         * @param body   响应体
         */
        public Result(int status, String body) {
            this.status = status;
            this.body = body;
        }

        /**
         * 获取状态码
         *
         * @return 状态码
         */
        public int getStatus() {
            return status;
        }

        /**
         * 获取响应体
         *
         * @return 响应体字符串
         */
        public String getBody() {
            return body;
        }
    }
}
