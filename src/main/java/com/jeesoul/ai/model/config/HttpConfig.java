package com.jeesoul.ai.model.config;

import org.springframework.context.annotation.Configuration;

/**
 * HTTP 相关配置类（已废弃）
 * HttpUtils 和 StreamHttpUtils 已改为静态工具类，无需通过 Bean 注入
 * 保留此类仅用于向后兼容，新代码无需使用
 * 
 * 注意：HttpUtils 和 StreamHttpUtils 已改为静态工具类，请直接使用静态方法调用：
 * - HttpUtils.post(...)
 * - HttpUtils.postRaw(...)
 * - StreamHttpUtils.postStream(...)
 * - StreamHttpUtils.postStreamRaw(...)
 *
 * @author dxy
 * @date 2025-06-10
 * @deprecated HttpUtils 和 StreamHttpUtils 已改为静态工具类，请直接使用静态方法调用
 *             此类已不再需要，保留仅为向后兼容
 */
@Deprecated
@Configuration
public class HttpConfig {
    // HttpUtils 和 StreamHttpUtils 已改为静态工具类，无需 Bean 配置
    // 请直接使用静态方法调用
}