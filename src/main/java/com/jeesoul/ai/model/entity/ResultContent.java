package com.jeesoul.ai.model.entity;

import lombok.Data;

/**
 * 结果数据
 *
 * @author dxy
 * @date 2025-06-10
 */
@Data
public class ResultContent {
    /**
     * 文本
     */
    private String content;
    /**
     * 是否思考
     */
    private Boolean thinking;

}
