package com.jeesoul.ai.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token使用统计VO
 * 封装AI模型调用的Token消耗信息，用于成本分析和优化
 *
 * @author dxy
 * @date 2025-12-03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenUsageVO {
    /**
     * 输入Token数量
     * 表示提示词（prompt）消耗的token数
     * 不同模型计算方式可能不同
     */
    private Integer promptTokens;

    /**
     * 输出Token数量
     * 表示模型生成内容消耗的token数
     * 通常输出token成本高于输入token
     */
    private Integer completionTokens;

    /**
     * 总Token数量
     * promptTokens + completionTokens
     * 用于快速统计总消耗
     */
    private Integer totalTokens;

    /**
     * 从标准Usage对象创建TokenUsageVO
     *
     * @param promptTokens     输入token数
     * @param completionTokens 输出token数
     * @param totalTokens      总token数
     * @return TokenUsageVO实例
     */
    public static TokenUsageVO of(Integer promptTokens, Integer completionTokens, Integer totalTokens) {
        return new TokenUsageVO(promptTokens, completionTokens, totalTokens);
    }

    /**
     * 从QWen/Spark风格的usage创建
     * 这些厂商使用inputTokens和outputTokens字段
     *
     * @param inputTokens  输入token数
     * @param outputTokens 输出token数
     * @return TokenUsageVO实例
     */
    public static TokenUsageVO ofInputOutput(Integer inputTokens, Integer outputTokens) {
        Integer total = null;
        if (inputTokens != null && outputTokens != null) {
            total = inputTokens + outputTokens;
        }
        return new TokenUsageVO(inputTokens, outputTokens, total);
    }
}

