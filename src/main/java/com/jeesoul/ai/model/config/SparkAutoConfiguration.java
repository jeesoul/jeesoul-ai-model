package com.jeesoul.ai.model.config;

import com.jeesoul.ai.model.service.AiService;
import com.jeesoul.ai.model.service.SparkService;
import com.jeesoul.ai.model.util.HttpUtils;
import com.jeesoul.ai.model.util.StreamHttpUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dxy
 * @date 2025-06-10
 */
@RequiredArgsConstructor
@Configuration
@ConditionalOnProperty(prefix = "ai.spark", name = "api-key")
public class SparkAutoConfiguration {

    private final HttpUtils aiHttpUtils;
    private final StreamHttpUtils streamHttpUtils;

    /**
     * 创建并配置星火大模型服务实例
     *
     * @param properties 星火大模型配置属性
     * @return 配置好的AiService实现类实例
     */
    @Bean
    public AiService sparkService(AiProperties properties) {
        return new SparkService(properties, aiHttpUtils, streamHttpUtils);
    }
}
