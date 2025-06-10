package com.jeesoul.ai.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大模型响应参数 VO。
 * 封装模型返回结果、模型类型和错误信息。
 *
 * @author dxy
 * @date 2025-06-10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModelResponseVO {
    /**
     * 返回结果
     */
    private String result;
    /**
     * 是否思考
     */
    private Boolean thinking;
    /**
     * 模型名称
     */
    private String model;

    /**
     * 构造方法
     *
     * @param result 结果
     * @param model  模型名称
     */
    public ModelResponseVO(String result, String model) {
        this.result = result;
        this.model = model;
    }
}
