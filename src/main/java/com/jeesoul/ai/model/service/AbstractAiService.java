package com.jeesoul.ai.model.service;

import com.jeesoul.ai.model.config.AiProperties;
import com.jeesoul.ai.model.constant.AiRole;
import com.jeesoul.ai.model.util.HttpUtils;
import com.jeesoul.ai.model.util.StreamHttpUtils;
import com.jeesoul.ai.model.vo.ModelRequestVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * AI服务抽象基类
 * 提供公共的参数处理、消息构建等功能，减少代码重复
 *
 * @author dxy
 * @date 2025-10-18
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractAiService implements AiService {
    /**
     * AI配置信息
     */
    protected final AiProperties aiProperties;
    /**
     * HTTP工具类实例
     */
    protected final HttpUtils aiHttpUtils;
    /**
     * 流式HTTP工具类实例
     */
    protected final StreamHttpUtils streamHttpUtils;

    /**
     * 获取当前服务的模型名称
     *
     * @return 模型名称
     */
    protected abstract String getModelName();

    /**
     * 验证请求参数的合法性
     *
     * @param request 请求参数
     * @throws IllegalArgumentException 参数不合法时抛出
     */
    protected void validateRequest(ModelRequestVO request) {
        if (request == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        if (StringUtils.isBlank(request.getPrompt())) {
            throw new IllegalArgumentException("提示词(prompt)不能为空");
        }
        if (StringUtils.isBlank(request.getModel())) {
            throw new IllegalArgumentException("模型版本(model)不能为空");
        }
        // 验证 temperature
        if (request.getTemperature() != null) {
            if (request.getTemperature() < 0 || request.getTemperature() > 2) {
                throw new IllegalArgumentException("temperature 必须在 0-2 之间，当前值: " + request.getTemperature());
            }
        }
        // 验证 topP
        if (request.getTopP() != null) {
            if (request.getTopP() < 0 || request.getTopP() > 1) {
                throw new IllegalArgumentException("topP 必须在 0-1 之间，当前值: " + request.getTopP());
            }
        }
        // 验证 maxTokens
        if (request.getMaxTokens() != null && request.getMaxTokens() <= 0) {
            throw new IllegalArgumentException("maxTokens 必须大于 0，当前值: " + request.getMaxTokens());
        }
    }

    /**
     * 检查当前模型是否支持系统提示词
     *
     * @return true 表示支持
     */
    protected abstract boolean supportSystemPrompt();

    /**
     * 检查当前模型是否支持思考模式
     *
     * @return true 表示支持
     */
    protected abstract boolean supportThinking();

    /**
     * 如果设置了不支持的功能，打印警告日志
     *
     * @param request 请求参数
     */
    protected void warnUnsupportedFeatures(ModelRequestVO request) {
        if (!supportSystemPrompt() && StringUtils.isNotBlank(request.getSystemPrompt())) {
            log.warn("[{}] 当前模型不支持 systemPrompt，该参数将被忽略", getModelName());
        }
        if (!supportThinking() && request.isEnableThinking()) {
            log.warn("[{}] 当前模型不支持 thinking 模式，该参数将被忽略", getModelName());
        }
    }

    /**
     * 将params参数合并到请求体
     * 使用反射动态设置属性值，支持各种自定义参数
     *
     * @param request 请求体对象
     * @param params  扩展参数映射
     */
    protected void mergeParamsToRequest(Object request, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return;
        }
        params.forEach((key, value) -> {
            if (value == null) {
                return;
            }
            try {
                String setterName = "set" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
                // 遍历所有方法，找到匹配的 setter
                for (java.lang.reflect.Method method : request.getClass().getMethods()) {
                    if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                        // 尝试调用 setter 方法
                        method.invoke(request, value);
                        log.debug("[{}] 参数透传成功: {} = {}", getModelName(), key, value);
                        break;
                    }
                }
            } catch (Exception e) {
                log.debug("[{}] 参数透传失败 - {}: {}", getModelName(), key, e.getMessage());
            }
        });
    }

    /**
     * 创建消息对象的通用方法
     * 子类可以通过 lambda 传入具体的消息构建逻辑
     *
     * @param role    角色
     * @param content 内容
     * @param creator 消息创建函数
     * @param <T>     消息类型
     * @return 创建的消息对象
     */
    protected <T> T createMessage(AiRole role, String content, MessageCreator<T> creator) {
        return creator.create(role, content);
    }

    /**
     * 消息创建器接口
     *
     * @param <T> 消息类型
     */
    @FunctionalInterface
    protected interface MessageCreator<T> {
        /**
         * 创建消息
         *
         * @param role    角色
         * @param content 内容
         * @return 消息对象
         */
        T create(AiRole role, String content);
    }

    /**
     * 截断响应内容用于日志打印
     * 避免日志内容过长
     *
     * @param content   原始内容
     * @param maxLength 最大长度
     * @return 截断后的内容
     */
    protected String truncateForLog(String content, int maxLength) {
        if (content == null) {
            return "null";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "... (共" + content.length() + "字符)";
    }
}
