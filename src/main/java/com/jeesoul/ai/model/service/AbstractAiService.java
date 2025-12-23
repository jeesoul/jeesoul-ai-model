package com.jeesoul.ai.model.service;

import com.jeesoul.ai.model.config.ModelConfig;
import com.jeesoul.ai.model.constant.AiRole;
import com.jeesoul.ai.model.util.JsonUtils;
import com.jeesoul.ai.model.vo.ModelRequestVO;
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
public abstract class AbstractAiService implements AiService {
    /**
     * 模型配置接口
     * 支持自定义模型独立管理配置，无需修改 AiProperties
     */
    protected final ModelConfig modelConfig;

    /**
     * 构造函数（推荐使用）
     * 使用 ModelConfig 接口，支持自定义模型独立管理配置
     * HttpUtils 和 StreamHttpUtils 已改为静态工具类，无需注入
     *
     * @param modelConfig 模型配置接口
     */
    public AbstractAiService(ModelConfig modelConfig) {
        this.modelConfig = modelConfig;
    }

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

    /**
     * 打印请求参数（debug级别）
     *
     * @param requestBody 请求体对象
     */
    protected void logRequestParams(Object requestBody) {
        if (log.isDebugEnabled()) {
            String json = JsonUtils.toJson(requestBody);
            log.debug("[{}] 请求参数: {}", getModelName(), json);
        }
    }

    /**
     * 获取温度参数，优先使用请求参数，如果请求参数为空则使用配置参数
     *
     * @param request           请求参数
     * @param configTemperature 配置中的温度值（可选，如果为null则从modelConfig获取）
     * @return 温度值
     */
    protected Double getTemperature(ModelRequestVO request, Double configTemperature) {
        if (request.getTemperature() != null) {
            return request.getTemperature();
        }
        if (configTemperature != null) {
            return configTemperature;
        }
        return modelConfig != null ? modelConfig.getTemperature() : null;
    }

    /**
     * 获取topP参数，优先使用请求参数，如果请求参数为空则使用配置参数
     *
     * @param request    请求参数
     * @param configTopP 配置中的topP值（可选，如果为null则从modelConfig获取）
     * @return topP值
     */
    protected Double getTopP(ModelRequestVO request, Double configTopP) {
        if (request.getTopP() != null) {
            return request.getTopP();
        }
        if (configTopP != null) {
            return configTopP;
        }
        return modelConfig != null ? modelConfig.getTopP() : null;
    }

    /**
     * 获取最大token数，优先使用请求参数，如果请求参数为空则使用配置参数
     *
     * @param request         请求参数
     * @param configMaxTokens 配置中的最大token数（可选，如果为null则从modelConfig获取）
     * @return 最大token数
     */
    protected Integer getMaxTokens(ModelRequestVO request, Integer configMaxTokens) {
        if (request.getMaxTokens() != null) {
            return request.getMaxTokens();
        }
        if (configMaxTokens != null) {
            return configMaxTokens;
        }
        return modelConfig != null ? modelConfig.getMaxTokens() : null;
    }

    /**
     * 获取模型名称，优先使用请求参数，如果请求参数为空则使用配置参数
     *
     * @param request     请求参数
     * @param configModel 配置中的模型名称（可选，如果为null则从modelConfig获取）
     * @return 模型名称
     * @throws IllegalArgumentException 如果请求参数和配置中都没有模型名称时抛出
     */
    protected String getModel(ModelRequestVO request, String configModel) {
        String model = null;
        if (StringUtils.isNotBlank(request.getModel())) {
            model = request.getModel();
        } else if (StringUtils.isNotBlank(configModel)) {
            model = configModel;
        } else if (modelConfig != null && StringUtils.isNotBlank(modelConfig.getModel())) {
            model = modelConfig.getModel();
        }
        if (StringUtils.isBlank(model)) {
            throw new IllegalArgumentException("模型版本(model)不能为空，请在请求参数或配置文件中设置");
        }
        return model;
    }

    /**
     * 获取API密钥
     *
     * @return API密钥
     * @throws IllegalStateException 如果配置中未设置API密钥
     */
    protected String getApiKey() {
        if (modelConfig != null && StringUtils.isNotBlank(modelConfig.getApiKey())) {
            return modelConfig.getApiKey();
        }
        throw new IllegalStateException("API密钥未配置，请在配置文件中设置 api-key");
    }

    /**
     * 获取服务端点
     *
     * @return 服务端点URL
     * @throws IllegalStateException 如果配置中未设置服务端点
     */
    protected String getEndpoint() {
        if (modelConfig != null && StringUtils.isNotBlank(modelConfig.getEndpoint())) {
            return modelConfig.getEndpoint();
        }
        throw new IllegalStateException("服务端点未配置，请在配置文件中设置 endpoint");
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
}
