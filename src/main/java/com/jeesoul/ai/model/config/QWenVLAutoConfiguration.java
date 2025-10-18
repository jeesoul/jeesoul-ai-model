package com.jeesoul.ai.model.config;

import com.jeesoul.ai.model.service.AiService;
import com.jeesoul.ai.model.service.QWenVLService;
import com.jeesoul.ai.model.util.HttpUtils;
import com.jeesoul.ai.model.util.StreamHttpUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 千问视觉理解大模型自动配置类
 *
 * @author dxy
 * @date 2025-10-18
 */
@RequiredArgsConstructor
@Configuration
@ConditionalOnProperty(prefix = "ai.qwen-vl", name = "api-key")
public class QWenVLAutoConfiguration {
    private final HttpUtils aiHttpUtils;
    private final StreamHttpUtils streamHttpUtils;

    /**
     * 创建并配置千问视觉理解大模型服务实例
     *
     * @param properties 千问视觉理解大模型配置属性
     * @return 配置好的AiService实现类实例
     */
    @Bean
    public AiService qWenVLService(AiProperties properties) {
        return new QWenVLService(properties, aiHttpUtils, streamHttpUtils);
    }
}
