package com.jeesoul.ai.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 大模型响应参数 VO
 * 封装模型返回结果、模型信息、Token使用统计等关键信息
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
     * 是否使用了思考模式
     * true表示使用了思考模式，false表示未使用
     */
    private Boolean thinking;

    /**
     * 思考过程内容
     * 仅在启用思考模式时有值（目前QWen和DouBao支持）
     * 包含模型的推理过程和思考细节
     */
    private String thinkingContent;

    /**
     * 模型提供商名称
     * 如：qWen、chatgpt、spark、deepSeek等
     * @deprecated 建议使用 {@link #modelProvider} 字段，该字段将在2.x版本中移除
     */
    @Deprecated
    private String model;

    /**
     * 模型提供商名称
     * 如：qWen、chatgpt、spark、deepSeek等
     */
    private String modelProvider;

    /**
     * 实际使用的具体模型版本
     * 如：qwen-turbo、gpt-4、spark-v3.5等
     * 该字段返回本次调用实际使用的模型版本，方便业务方进行成本分析
     */
    private String modelName;

    /**
     * Token使用统计
     * 包含输入token、输出token和总token数
     * 用于成本统计、计费和优化
     */
    private TokenUsageVO usage;

    /**
     * 兼容性构造方法
     *
     * @param result 结果
     * @param model  模型提供商名称
     * @deprecated 建议使用带有完整参数的构造方法
     */
    @Deprecated
    public ModelResponseVO(String result, String model) {
        this.result = result;
        this.model = model;
        this.modelProvider = model;
    }

    /**
     * 兼容性构造方法（带思考标识）
     *
     * @param result   结果
     * @param thinking 是否思考
     * @param model    模型提供商名称
     * @deprecated 建议使用带有完整参数的构造方法
     */
    @Deprecated
    public ModelResponseVO(String result, Boolean thinking, String model) {
        this.result = result;
        this.thinking = thinking;
        this.model = model;
        this.modelProvider = model;
    }

    /**
     * 完整构造方法
     *
     * @param result         返回结果
     * @param thinking       是否思考
     * @param thinkingContent 思考过程内容
     * @param modelProvider  模型提供商
     * @param modelName      具体模型版本
     * @param usage          Token使用统计
     */
    public ModelResponseVO(String result, Boolean thinking, String thinkingContent,
                          String modelProvider, String modelName, TokenUsageVO usage) {
        this.result = result;
        this.thinking = thinking;
        this.thinkingContent = thinkingContent;
        this.model = modelProvider;
        this.modelProvider = modelProvider;
        this.modelName = modelName;
        this.usage = usage;
    }

    /**
     * 创建简单响应（仅包含结果和模型信息）
     *
     * @param result        返回结果
     * @param modelProvider 模型提供商
     * @param modelName     具体模型版本
     * @return ModelResponseVO实例
     */
    public static ModelResponseVO of(String result, String modelProvider, String modelName) {
        return new ModelResponseVO(result, false, null, modelProvider, modelName, null);
    }

    /**
     * 创建完整响应
     *
     * @param result         返回结果
     * @param thinkingContent 思考过程内容
     * @param modelProvider  模型提供商
     * @param modelName      具体模型版本
     * @param usage          Token使用统计
     * @return ModelResponseVO实例
     */
    public static ModelResponseVO of(String result, String thinkingContent, String modelProvider,
                                    String modelName, TokenUsageVO usage) {
        Boolean hasThinking = (thinkingContent != null && !thinkingContent.isEmpty());
        return new ModelResponseVO(result, hasThinking, thinkingContent, 
                                   modelProvider, modelName, usage);
    }
}
