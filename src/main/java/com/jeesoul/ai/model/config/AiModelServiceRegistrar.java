package com.jeesoul.ai.model.config;

import com.jeesoul.ai.model.annotation.AiModelService;
import com.jeesoul.ai.model.strategy.AiStrategyContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * AI模型服务自动注册器
 * 在Spring容器初始化时，自动扫描带有 @AiModelService 注解的Bean
 * 并将其注册到策略上下文中，实现自动发现和注册
 *
 * @author dxy
 * @date 2025-10-18
 */
@Slf4j
@Component
public class AiModelServiceRegistrar implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 检查Bean是否标注了 @AiModelService 注解
        AiModelService annotation = bean.getClass().getAnnotation(AiModelService.class);
        if (annotation != null) {
            String modelName = annotation.modelName();
            // 如果没有指定serviceName，使用beanName
            String serviceName = StringUtils.hasText(annotation.serviceName()) 
                    ? annotation.serviceName() 
                    : beanName;
            
            // 注册到策略上下文
            AiStrategyContext.registerModel(modelName, serviceName);
            
            // 打印日志
            if (StringUtils.hasText(annotation.description())) {
                log.info("已注册自定义AI模型: {} -> {} ({})", 
                        modelName, serviceName, annotation.description());
            } else {
                log.info("已注册自定义AI模型: {} -> {}", modelName, serviceName);
            }
        }
        return bean;
    }
}
