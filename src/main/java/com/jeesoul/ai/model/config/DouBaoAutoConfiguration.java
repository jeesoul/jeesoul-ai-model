package com.jeesoul.ai.model.config;

import com.jeesoul.ai.model.service.AiService;
import com.jeesoul.ai.model.service.DouBaoService;
import com.jeesoul.ai.model.util.HttpUtils;
import com.jeesoul.ai.model.util.StreamHttpUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 豆包（火山方舟）大模型自动配置类
 * 当配置文件中存在 ai.doubao.api-key 属性时，该配置类才会生效
 * 用于自动配置豆包大模型相关的服务组件
 *
 * @author dxy
 * @date 2025-12-03
 */
@RequiredArgsConstructor
@Configuration
@ConditionalOnProperty(prefix = "ai.doubao", name = "api-key")
public class DouBaoAutoConfiguration {

    private final HttpUtils httpUtils;
    private final StreamHttpUtils streamHttpUtils;

    /**
     * 创建并配置豆包大模型服务实例
     *
     * @param properties 豆包大模型配置属性
     * @return 配置好的AiService实现类实例
     */
    @Bean
    public AiService douBaoService(AiProperties properties) {
        return new DouBaoService(properties, httpUtils, streamHttpUtils);
    }
}

