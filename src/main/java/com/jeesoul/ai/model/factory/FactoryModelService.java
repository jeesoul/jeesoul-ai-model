package com.jeesoul.ai.model.factory;

import com.jeesoul.ai.model.constant.AiModel;
import com.jeesoul.ai.model.service.AiService;
import com.jeesoul.ai.model.strategy.AiStrategyContext;

/**
 * 模型service 工厂
 * 支持内置模型和自定义模型的统一创建
 *
 * @author dxy
 * @date 2025-06-10
 */
public class FactoryModelService {
    /**
     * 获取模型服务
     * 支持内置模型（通过枚举）和自定义模型（通过策略上下文）
     *
     * @param modelName 模型名称（内置模型如：qWen、chatgpt、spark等，或自定义模型名称）
     * @return aiService
     * @throws IllegalArgumentException 如果模型未注册
     */
    public static AiService create(String modelName) {
        // 先尝试从枚举中查找（内置模型）
        try {
            AiModel aiModel = AiModel.fromString(modelName);
            return AiStrategyContext.getService(aiModel.getModelName());
        } catch (IllegalArgumentException e) {
            // 如果枚举中找不到，尝试从策略上下文中查找（自定义模型）
            if (AiStrategyContext.isModelRegistered(modelName)) {
                return AiStrategyContext.getService(modelName);
            }
            // 如果都找不到，抛出异常
            throw new IllegalArgumentException(
                    String.format("未注册的模型: %s。请检查模型名称是否正确，或确保自定义模型已使用 @AiModelService 注解注册", modelName)
            );
        }
    }

    /**
     * 获取模型
     *
     * @param aiModel 模型枚举
     * @return aiService
     */
    public static AiService create(AiModel aiModel) {
        return AiStrategyContext.getService(aiModel.getModelName());
    }
}
