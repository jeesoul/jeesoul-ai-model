package com.jeesoul.ai.model.http;

/**
 * HTTP 响应对象
 * 仿照 Hutool 的 cn.hutool.http.HttpResponse 提供同名、同方法签名的 API。
 * 响应体在请求执行阶段一次性读取并缓存，后续多次调用 body() 返回同一缓存值。
 *
 * @author dxy
 * @date 2025-06-18
 */
public class HttpResponse {
    /**
     * HTTP 状态码
     */
    private final int status;
    /**
     * 响应体字符串（已缓存）
     */
    private final String body;

    /**
     * 构造响应
     *
     * @param status 状态码
     * @param body   响应体
     */
    public HttpResponse(int status, String body) {
        this.status = status;
        this.body = body;
    }

    /**
     * 判断响应是否成功（2xx）
     *
     * @return true 表示状态码在 200-299 之间
     */
    public boolean isOk() {
        return status >= 200 && status < 300;
    }

    /**
     * 获取响应体
     *
     * @return 响应体字符串
     */
    public String body() {
        return body;
    }

    /**
     * 获取状态码
     *
     * @return HTTP 状态码
     */
    public int getStatus() {
        return status;
    }
}
