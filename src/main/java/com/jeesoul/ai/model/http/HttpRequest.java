package com.jeesoul.ai.model.http;

import com.jeesoul.ai.model.http.engine.HttpClientEngine;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * HTTP 请求构建器
 * 仿照 Hutool 的 cn.hutool.http.HttpRequest 提供同名、同方法签名的链式 API，
 * 底层委托 HttpClientEngine（基于 Apache HttpClient 5.x）执行，便于无缝替换 Hutool。
 *
 * @author dxy
 * @date 2025-06-18
 */
public class HttpRequest {
    /**
     * 请求地址
     */
    private final String url;
    /**
     * 请求方法
     */
    private Method method = Method.GET;
    /**
     * 请求头
     */
    private final Map<String, String> headers = new LinkedHashMap<>();
    /**
     * 请求体（JSON 字符串）
     */
    private String body;
    /**
     * 超时时间（毫秒）
     */
    private int timeout;

    /**
     * 私有构造，通过静态工厂创建
     *
     * @param url 请求地址
     */
    private HttpRequest(String url) {
        this.url = url;
    }

    /**
     * 创建请求
     *
     * @param url 请求地址
     * @return HttpRequest 实例
     */
    public static HttpRequest of(String url) {
        return new HttpRequest(url);
    }

    /**
     * 设置超时时间
     *
     * @param milliseconds 超时毫秒数
     * @return 当前请求对象
     */
    public HttpRequest timeout(int milliseconds) {
        this.timeout = milliseconds;
        return this;
    }

    /**
     * 设置请求方法
     *
     * @param method HTTP 方法
     * @return 当前请求对象
     */
    public HttpRequest method(Method method) {
        this.method = method;
        return this;
    }

    /**
     * 添加请求头
     *
     * @param name  头名称
     * @param value 头值
     * @return 当前请求对象
     */
    public HttpRequest header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    /**
     * 设置请求体
     *
     * @param body 请求体字符串
     * @return 当前请求对象
     */
    public HttpRequest body(String body) {
        this.body = body;
        return this;
    }

    /**
     * 设置 SSL 协议
     * Apache HttpClient 5.x 默认支持 HTTPS/TLS，此方法保留以兼容 Hutool 调用，内部无需额外处理。
     *
     * @param protocol 协议名称
     * @return 当前请求对象
     */
    public HttpRequest setSSLProtocol(String protocol) {
        return this;
    }

    /**
     * 获取请求地址
     *
     * @return 请求地址
     */
    public String getUrl() {
        return url;
    }

    /**
     * 执行请求
     *
     * @return 响应对象
     */
    public HttpResponse execute() {
        HttpClientEngine.Result result = HttpClientEngine.execute(
                method.name(), url, headers, body, timeout);
        return new HttpResponse(result.getStatus(), result.getBody());
    }
}
