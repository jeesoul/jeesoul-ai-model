package com.jeesoul.ai.model.constant;

/**
 * 大模型类型枚举。
 * 定义了支持的 ai 模型及其对应的服务实现名称。
 *
 * @author dxy
 * @date 2025-06-10
 */
public enum AiModel {
    /**
     * 通义千问
     */
    Q_WEN("qWen", "qWenService"),
    /**
     * ChatGPT
     */
    CHATGPT("chatgpt", "chatGPTService"),
    /**
     * 讯飞星火
     */
    SPARK("spark", "sparkService"),
    /**
     * DeepSeek
     */
    DEEP_SEEK("deepSeek", "deepSeekService");

    /**
     * 模型名称
     */
    private final String modelName;
    /**
     * 服务实现名称
     */
    private final String serviceName;

    /**
     * 构造方法
     *
     * @param modelName   模型名称
     * @param serviceName 服务实现名称
     */
    AiModel(String modelName, String serviceName) {
        this.modelName = modelName;
        this.serviceName = serviceName;
    }

    /**
     * 获取模型名称
     *
     * @return 模型名称
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * 获取服务实现名称
     *
     * @return 服务实现名称
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * 根据字符串获取对应的枚举实例。
     *
     * @param model 模型名称字符串
     * @return 对应的 AiModel 枚举
     * @throws IllegalArgumentException 如果模型名称不支持
     */
    public static AiModel fromString(String model) {
        for (AiModel aiModel : AiModel.values()) {
            if (aiModel.getModelName().equalsIgnoreCase(model)) {
                return aiModel;
            }
        }
        throw new IllegalArgumentException("不支持的模型: " + model);
    }
}
