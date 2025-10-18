package com.jeesoul.ai.model.strategy;

import cn.hutool.extra.spring.SpringUtil;
import com.jeesoul.ai.model.constant.AiModel;
import com.jeesoul.ai.model.service.AiService;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 大模型策略上下文工具类。
 * 用于根据模型名称动态获取对应的 AiService 实现。
 * 支持动态注册自定义模型服务。
 *
 * @author dxy
 * @date 2025-06-10
 */
@Slf4j
@UtilityClass
public class AiStrategyContext {
    /**
     * 策略映射表，存储模型名称与服务 Bean 名称的映射关系。
     * 使用 ConcurrentHashMap 支持并发访问和动态注册
     */
    private static final Map<String, String> AI_STRATEGY_MAP = new ConcurrentHashMap<>();

    static {
        // 自动从枚举中初始化所有模型
        for (AiModel model : AiModel.values()) {
            AI_STRATEGY_MAP.put(model.getModelName(), model.getServiceName());
        }
        log.debug("已初始化 {} 个AI模型策略", AI_STRATEGY_MAP.size());
    }

    /**
     * 根据模型名称获取对应的 AiService Bean。
     *
     * @param modelName 模型名称
     * @return AiService 实现
     * @throws IllegalArgumentException 如果模型未注册
     */
    public AiService getService(String modelName) {
        String serviceName = AI_STRATEGY_MAP.get(modelName);
        if (serviceName == null) {
            throw new IllegalArgumentException("未注册的模型: " + modelName);
        }
        return SpringUtil.getBean(serviceName);
    }

    /**
     * 动态注册自定义模型服务
     * 允许用户扩展框架，注册自己的AI模型实现
     *
     * @param modelName   模型名称
     * @param serviceName 服务Bean名称
     */
    public void registerModel(String modelName, String serviceName) {
        if (AI_STRATEGY_MAP.containsKey(modelName)) {
            log.warn("模型 {} 已存在，将被覆盖为新的服务: {}", modelName, serviceName);
        }
        AI_STRATEGY_MAP.put(modelName, serviceName);
        log.info("已注册自定义模型: {} -> {}", modelName, serviceName);
    }

    /**
     * 检查模型是否已注册
     *
     * @param modelName 模型名称
     * @return true 表示已注册
     */
    public boolean isModelRegistered(String modelName) {
        return AI_STRATEGY_MAP.containsKey(modelName);
    }
}