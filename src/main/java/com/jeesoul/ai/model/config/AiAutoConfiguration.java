package com.jeesoul.ai.model.config;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
/**
 * 配置文件
 *
 * @author dxy
 * @date 2025-06-10
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
@Import({QWenAutoConfiguration.class, ChatGPTAutoConfiguration.class,
        SparkAutoConfiguration.class, DeepSeekAutoConfiguration.class})
public class AiAutoConfiguration {
}

