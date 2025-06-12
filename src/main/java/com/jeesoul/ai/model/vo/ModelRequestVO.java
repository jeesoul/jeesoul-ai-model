package com.jeesoul.ai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 大模型请求参数 VO。
 * 封装模型类型、提示词和其他参数。
 * 支持链式调用设置参数。
 *
 * @author dxy
 * @date 2025-06-10
 */
@Data
@Accessors(chain = true)
public class ModelRequestVO {
    /**
     * 模型名称（如 qWen, chatgpt, spark, deepseek）
     */
    private String modelName;
    /**
     * 模型类型
     */
    private String model;

    /**
     * 系统提示词
     */
    private String systemPrompt;
    /**
     * 提示词
     */
    private String prompt;
    /**
     * 是否开启思考
     */
    private boolean enableThinking;
    /**
     * 其他参数
     */
    private Map<String, Object> params;
}
