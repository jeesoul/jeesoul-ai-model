package com.jeesoul.ai.model.response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesoul.ai.model.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 基础流式响应处理器
 * 用于处理流式响应的通用逻辑，支持Server-Sent Events (SSE)格式的响应处理
 * 主要功能：
 * 1. 处理SSE格式的响应数据
 * 2. 解析JSON响应
 * 3. 验证响应有效性
 * 4. 提取响应内容
 * 使用方式：
 * 1. 继承此类并实现抽象方法
 * 2. 在isResponseValid中实现响应验证逻辑
 * 3. 在extractContent中实现内容提取逻辑
 *
 * @author dxy
 * @date 2025-06-10
 */
@Slf4j
public abstract class StreamBaseResponse<R> {

    /**
     * 构造函数
     * 子类必须调用此构造函数
     */
    protected StreamBaseResponse() {
    }

    /**
     * 处理响应数据
     * 处理流程：
     * 1. 处理SSE格式数据（如果存在）
     * 2. 检查结束标记
     * 3. 解析JSON数据
     * 4. 验证响应有效性
     * 5. 提取响应内容
     *
     * @param data 原始响应数据，可能是SSE格式或普通JSON格式
     * @return 处理后的数据流，如果处理失败则返回空流
     */
    public Flux<R> process(String data) {
        try {
            // 处理SSE格式的数据
            String jsonData = data;
            if (data.startsWith("data:")) {
                jsonData = data.substring(5).trim();
            }

            // 检查是否是结束标记
            if ("[DONE]".equals(jsonData)) {
                return Flux.empty();
            }

            // 解析JSON数据
            Map<String, Object> responseMap = JsonUtils.fromJson(jsonData, new TypeReference<Map<String, Object>>() {
            });

            // 检查响应状态
            if (!isResponseValid(responseMap)) {
                return Flux.empty();
            }

            // 提取内容
            return extractContent(responseMap);
        } catch (Exception e) {
            log.error("处理响应数据失败: {}", e.getMessage());
            return Flux.empty();
        }
    }

    /**
     * 检查响应是否有效
     * 子类必须实现此方法，用于验证响应数据的有效性
     * 例如：
     * - 检查响应状态码
     * - 验证必要字段是否存在
     * - 检查业务状态码
     *
     * @param responseMap 解析后的响应数据Map
     * @return true表示响应有效，false表示响应无效
     */
    protected abstract boolean isResponseValid(Map<String, Object> responseMap);

    /**
     * 提取响应内容
     * 子类必须实现此方法，用于从响应数据中提取所需内容
     * 例如：
     * - 提取文本内容
     * - 提取特定字段
     * - 转换数据格式
     *
     * @param responseMap 解析后的响应数据Map
     * @return 提取的内容流，如果无法提取则返回空流
     */
    protected abstract Flux<R> extractContent(Map<String, Object> responseMap);
}
