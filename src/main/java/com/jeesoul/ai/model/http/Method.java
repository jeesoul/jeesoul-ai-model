package com.jeesoul.ai.model.http;

/**
 * HTTP 请求方法枚举
 * 与 Hutool 的 cn.hutool.http.Method 保持同名同语义，便于无缝替换。
 *
 * @author dxy
 * @date 2025-06-18
 */
public enum Method {
    /**
     * GET 请求
     */
    GET,
    /**
     * POST 请求
     */
    POST,
    /**
     * PUT 请求
     */
    PUT,
    /**
     * DELETE 请求
     */
    DELETE,
    /**
     * HEAD 请求
     */
    HEAD
}
