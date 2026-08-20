package com.jeesoul.ai.model.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

/**
 * Spring 上下文持有工具类
 * 基于 Spring 原生 ApplicationContextAware 实现，用于在非 Spring 管理的类中获取 Bean。
 * 替代对 Hutool SpringUtil 的依赖，避免引入额外的初始化约束。
 * 通过 Spring Boot 自动配置 SPI（AutoConfiguration.imports）注册，确保在 Starter 场景下可靠加载。
 * 注册的 Bean 名称为全限定类名，不会与使用方的同名 Bean 冲突。
 *
 * @author dxy
 * @date 2025-06-18
 */
@Configuration
public class SpringContextHolder implements ApplicationContextAware {

    /**
     * Spring 应用上下文
     */
    private static ApplicationContext applicationContext;

    /**
     * 注入 Spring 应用上下文
     *
     * @param context 应用上下文
     * @throws BeansException Bean 异常
     */
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        SpringContextHolder.applicationContext = context;
    }

    /**
     * 获取 Spring 应用上下文
     *
     * @return 应用上下文
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 根据 Bean 名称获取 Bean 实例
     *
     * @param name Bean 名称
     * @param <T>  Bean 类型
     * @return Bean 实例
     * @throws IllegalStateException 如果 Spring 上下文尚未初始化
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        assertContextInjected();
        return (T) applicationContext.getBean(name);
    }

    /**
     * 根据 Bean 类型获取 Bean 实例
     *
     * @param clazz Bean 类型
     * @param <T>   Bean 类型
     * @return Bean 实例
     * @throws IllegalStateException 如果 Spring 上下文尚未初始化
     */
    public static <T> T getBean(Class<T> clazz) {
        assertContextInjected();
        return applicationContext.getBean(clazz);
    }

    /**
     * 校验上下文是否已注入
     *
     * @throws IllegalStateException 如果上下文未初始化
     */
    private static void assertContextInjected() {
        if (applicationContext == null) {
            throw new IllegalStateException(
                    "ApplicationContext 尚未注入，请确保 SpringContextHolder 已被 Spring 容器加载");
        }
    }
}
