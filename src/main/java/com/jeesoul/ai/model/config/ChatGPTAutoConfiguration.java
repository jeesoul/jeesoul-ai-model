package com.jeesoul.ai.model.config;

import com.jeesoul.ai.model.service.AiService;
import com.jeesoul.ai.model.service.ChatGPTService;
import com.jeesoul.ai.model.util.HttpUtils;
import com.jeesoul.ai.model.util.StreamHttpUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatGPT大模型自动配置类
 * 当配置文件中存在 ai.chat-gpt.api-key 属性时，该配置类才会生效
 * 用于自动配置ChatGPT大模型相关的服务组件
 *
 * @author dxy
 * @date 2025-06-10
 */
@RequiredArgsConstructor
@Configuration
@ConditionalOnProperty(prefix = "ai.chat-gpt", name = "api-key")
public class ChatGPTAutoConfiguration {

    private final HttpUtils httpUtils;
    private final StreamHttpUtils streamHttpUtils;

    /**
     * 创建并配置ChatGPT大模型服务实例
     *
     * @param properties ChatGPT大模型配置属性
     * @return 配置好的AiService实现类实例
     */
    @Bean("chatGPTService")
    public AiService chatGPTService(AiProperties properties) {
        return new ChatGPTService(properties, httpUtils, streamHttpUtils);
    }
}
