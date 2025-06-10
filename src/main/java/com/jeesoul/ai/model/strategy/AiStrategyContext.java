package com.jeesoul.ai.model.strategy;

import cn.hutool.extra.spring.SpringUtil;
import com.jeesoul.ai.model.constant.AiModel;
import com.jeesoul.ai.model.service.AiService;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

/**
 * 大模型策略上下文工具类。
 * 用于根据模型名称动态获取对应的 AiService 实现。
 *
 * @author dxy
 * @date 2025-06-10
 */
@UtilityClass
public class AiStrategyContext {
    /**
     * 策略映射表，存储模型名称与服务 Bean 名称的映射关系。
     */
    private static final Map<String, String> AI_STRATEGY_MAP = new HashMap<String, String>() {
        {
            put(AiModel.SPARK.getModelName(), AiModel.SPARK.getServiceName());
            put(AiModel.Q_WEN.getModelName(), AiModel.Q_WEN.getServiceName());
            put(AiModel.DEEP_SEEK.getModelName(), AiModel.DEEP_SEEK.getServiceName());
            put(AiModel.CHATGPT.getModelName(), AiModel.CHATGPT.getServiceName());
        }
    };

    /**
     * 根据模型名称获取对应的 AiService Bean。
     *
     * @param modelName 模型名称
     * @return AiService 实现
     */
    public AiService getService(String modelName) {
        return SpringUtil.getBean(AI_STRATEGY_MAP.get(modelName));
    }
}