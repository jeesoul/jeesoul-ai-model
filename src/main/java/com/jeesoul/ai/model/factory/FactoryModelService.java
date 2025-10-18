package com.jeesoul.ai.model.factory;

import com.jeesoul.ai.model.constant.AiModel;
import com.jeesoul.ai.model.service.AiService;
import com.jeesoul.ai.model.strategy.AiStrategyContext;

/**
 * 模型service 工厂
 *
 * @author dxy
 * @date 2025-06-10
 */
public class FactoryModelService {
    /**
     * 获取模型
     *
     * @param modelName 模型名称
     * @return aiService
     */
    public static AiService create(String modelName) {
        AiModel aiModel = AiModel.fromString(modelName);
        return AiStrategyContext.getService(aiModel.getModelName());
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
