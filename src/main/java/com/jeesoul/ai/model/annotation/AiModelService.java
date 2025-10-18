package com.jeesoul.ai.model.annotation;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * AI模型服务注解
 * 用于标记自定义的AI模型服务实现，框架会自动注册到策略上下文中
 * <p>
 * 使用示例：在自定义Service类上添加此注解，指定modelName和serviceName
 *
 * @author dxy
 * @date 2025-10-18
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface AiModelService {
    
    /**
     * 模型名称
     * 用于通过 FactoryModelService.create(modelName) 创建服务实例
     * 例如: "claude", "gemini", "llama"
     *
     * @return 模型名称
     */
    String modelName();
    
    /**
     * 服务Bean名称（可选）
     * 如果不指定，默认使用类名首字母小写作为Bean名称
     * 例如: ClaudeService 对应 claudeService
     *
     * @return 服务Bean名称
     */
    String serviceName() default "";
    
    /**
     * 模型描述（可选）
     * 用于文档和日志说明
     *
     * @return 模型描述
     */
    String description() default "";
}
