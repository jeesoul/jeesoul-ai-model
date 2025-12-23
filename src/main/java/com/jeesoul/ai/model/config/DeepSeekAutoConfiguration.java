package com.jeesoul.ai.model.config;

import com.jeesoul.ai.model.service.AiService;
import com.jeesoul.ai.model.service.DeepSeekService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeek大模型自动配置类
 * 当配置文件中存在 ai.deep-seek.api-key 属性时，该配置类才会生效
 * 用于自动配置DeepSeek大模型相关的服务组件
 *
 * @author dxy
 * @date 2025-06-10
 */
@Configuration
@ConditionalOnProperty(prefix = "ai.deep-seek", name = "api-key")
public class DeepSeekAutoConfiguration {

    /**
     * 创建并配置DeepSeek大模型服务实例
     *
     * @param properties DeepSeek大模型配置属性
     * @return 配置好的AiService实现类实例
     */
    @Bean
    public AiService deepSeekService(AiProperties properties) {
        return new DeepSeekService(properties.getDeepSeek());
    }
}

