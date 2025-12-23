package com.jeesoul.ai.model.config;

import com.jeesoul.ai.model.service.AiService;
import com.jeesoul.ai.model.service.SparkService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 讯飞星火大模型自动配置类
 *
 * @author dxy
 * @date 2025-06-10
 */
@Configuration
@ConditionalOnProperty(prefix = "ai.spark", name = "api-key")
public class SparkAutoConfiguration {

    /**
     * 创建并配置星火大模型服务实例
     *
     * @param properties 星火大模型配置属性
     * @return 配置好的AiService实现类实例
     */
    @Bean
    public AiService sparkService(AiProperties properties) {
        return new SparkService(properties.getSpark());
    }
}
