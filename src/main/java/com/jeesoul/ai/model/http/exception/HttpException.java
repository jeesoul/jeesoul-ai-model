package com.jeesoul.ai.model.http.exception;

/**
 * HTTP 请求异常
 * 用于封装 HTTP 调用过程中发生的各类错误
 *
 * @author dxy
 * @date 2025-06-18
 */
public class HttpException extends RuntimeException {

    /**
     * 无参构造
     */
    public HttpException() {
        super();
    }

    /**
     * 带消息构造
     *
     * @param message 错误消息
     */
    public HttpException(String message) {
        super(message);
    }

    /**
     * 带消息和原因构造
     *
     * @param message 错误消息
     * @param cause   原始异常
     */
    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 带原因构造
     *
     * @param cause 原始异常
     */
    public HttpException(Throwable cause) {
        super(cause);
    }
}
