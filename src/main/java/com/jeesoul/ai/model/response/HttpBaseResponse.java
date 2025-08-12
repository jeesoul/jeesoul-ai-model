package com.jeesoul.ai.model.response;

import lombok.Data;

import java.util.Map;

/**
 * 基础响应类
 *
 * @author dxy
 * @date 2025-06-10
 */
@Data
public class HttpBaseResponse {
    /**
     * 响应ID
     */
    private String id;

    /**
     * 响应对象类型
     */
    private String object;

    /**
     * 创建时间戳
     */
    private long created;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 状态码
     */
    private Integer httpStatus;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 元数据
     * 用于存储额外的响应字段，如：
     * - logprobs
     * - system_fingerprint
     */
    private Map<String, Object> metadata;
}

