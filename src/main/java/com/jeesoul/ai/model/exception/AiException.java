package com.jeesoul.ai.model.exception;

/**
 * LLM 相关业务异常类。
 * 用于封装 LLM 相关操作中的运行时异常。
 *
 * @author dxy
 * @date 2025-06-10
 */
public class AiException extends RuntimeException {

    /**
     * 构造方法，根据异常信息创建异常对象。
     *
     * @param message 异常信息
     */
    public AiException(String message) {
        super(message);
    }

    /**
     * 构造方法，根据异常信息和原始异常创建异常对象。
     *
     * @param message 异常信息
     * @param cause   原始异常
     */
    public AiException(String message, Throwable cause) {
        super(message, cause);
    }
}