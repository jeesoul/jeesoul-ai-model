package com.jeesoul.ai.model.entity;

import com.jeesoul.ai.model.vo.TokenUsageVO;
import lombok.Data;

/**
 * 结果数据
 * 用于流式响应的内容封装
 *
 * @author dxy
 * @date 2025-06-10
 */
@Data
public class ResultContent {
    /**
     * 返回的文本内容
     * 该字段始终包含实际文本，不管是thinking还是正常content
     * 如果是thinking chunk，content = thinkingContent
     * 如果是正常chunk，content = 正常内容
     */
    private String content;

    /**
     * 是否使用了思考模式
     * true表示该chunk是思考过程，false表示正常内容
     * 注意：这是布尔标识，不是内容
     */
    private Boolean thinking;

    /**
     * 思考过程内容
     * 仅在启用思考模式时有值（目前QWen和DouBao支持）
     * 包含模型的推理过程和思考细节
     */
    private String thinkingContent;

    /**
     * Token使用统计
     * 仅在流式响应的最后一个chunk中有值
     * 业务方可以通过判断此字段是否为null来识别流是否结束
     */
    private TokenUsageVO usage;

    /**
     * 模型提供商名称
     * 用于流式响应中标识使用的模型提供商
     */
    private String modelProvider;

    /**
     * 实际使用的具体模型版本
     * 用于流式响应中标识实际使用的模型版本
     */
    private String modelName;

}
