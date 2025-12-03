package com.jeesoul.ai.model.response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jeesoul.ai.model.entity.ResultContent;
import com.jeesoul.ai.model.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * QWen流式响应处理器
 * 用于处理QWen服务的流式响应，继承自BaseStreamResponse
 * 主要功能：
 * 1. 验证QWen响应状态码
 * 2. 提取QWen响应中的文本内容
 * 3. 支持content和reasoning_content两种内容格式
 * 响应格式示例：
 * {
 * "code": 0,
 * "choices": [{
 * "delta": {
 * "content": "文本内容"
 * }
 * }]
 * }
 *
 * @author dxy
 * @date 2025-06-10
 */
@Slf4j
public class StreamQWenResponse extends StreamBaseResponse<ResultContent> {

    /**
     * 构造函数
     */
    public StreamQWenResponse() {
        super();
    }

    /**
     * 检查响应是否有效
     * 验证规则：
     * 1. 检查响应中是否包含code字段
     * 2. 验证code值是否为0（0表示成功）
     * 3. 如果code不为0，记录错误信息
     *
     * @param responseMap 解析后的响应数据Map
     * @return true表示响应有效，false表示响应无效
     */
    @Override
    protected boolean isResponseValid(Map<String, Object> responseMap) {
        if (responseMap.containsKey("code")) {
            int code = (Integer) responseMap.get("code");
            if (code != 0) {
                log.error("服务器返回错误: {}", responseMap.get("message"));
                return false;
            }
        }
        return true;
    }

    /**
     * 提取响应内容
     * 提取规则：
     * 1. 检查响应中是否包含choices字段
     * 2. 获取第一个choice中的delta字段
     * 3. 优先获取content字段内容
     * 4. 如果没有content则尝试获取reasoning_content
     * 5. 提取usage字段（如果存在，通常在最后一个chunk中）
     * 6. 如果都没有则返回空流
     *
     * @param responseMap 解析后的响应数据Map
     * @return 提取的文本内容流，如果无法提取则返回空流
     */
    @Override
    protected Flux<ResultContent> extractContent(Map<String, Object> responseMap) {
        ResultContent response = new ResultContent();
        boolean hasContent = false;

        // 检查并提取usage信息（通常在最后一个chunk中）
        if (responseMap.containsKey("usage")) {
            Map<String, Object> usageMap = JsonUtils.toMap(responseMap.get("usage"));
            if (usageMap != null) {
                Integer inputTokens = (Integer) usageMap.get("input_tokens");
                Integer outputTokens = (Integer) usageMap.get("output_tokens");
                if (inputTokens != null || outputTokens != null) {
                    response.setUsage(com.jeesoul.ai.model.vo.TokenUsageVO.ofInputOutput(
                            inputTokens, outputTokens));
                    hasContent = true;
                }
            }
        }

        // 检查choices字段
        if (!responseMap.containsKey("choices") || !(responseMap.get("choices") instanceof List)) {
            return hasContent ? Flux.just(response) : Flux.empty();
        }

        List<Map<String, Object>> choices = JsonUtils.fromJson(
                JsonUtils.toJson(responseMap.get("choices")),
                new TypeReference<List<Map<String, Object>>>() {
                }
        );
        if (choices == null || choices.isEmpty()) {
            return hasContent ? Flux.just(response) : Flux.empty();
        }

        // 获取第一个choice
        Map<String, Object> choice = choices.get(0);
        if (!choice.containsKey("delta")) {
            return hasContent ? Flux.just(response) : Flux.empty();
        }

        // 获取delta中的内容
        Map<String, Object> delta = JsonUtils.toMap(choice.get("delta"));
        if (delta == null) {
            return hasContent ? Flux.just(response) : Flux.empty();
        }

        // 先尝试获取reasoning_content（思考内容）
        String reasoningContent = delta.containsKey("reasoning_content") 
            ? (String) delta.get("reasoning_content") : null;
        
        // 再获取content（正常内容）
        String contentText = delta.containsKey("content") 
            ? (String) delta.get("content") : null;
        
        // 优先使用reasoning_content（如果有值）
        if (reasoningContent != null && !reasoningContent.isEmpty()) {
            response.setContent(reasoningContent);  // ✅ content字段包含思考文本
            response.setThinking(true);  // 标识这是思考内容
            response.setThinkingContent(reasoningContent);  // 存储思考文本
            hasContent = true;
        } 
        // 否则使用content（如果有值）
        else if (contentText != null && !contentText.isEmpty()) {
            response.setContent(contentText);  // ✅ content字段包含正常文本
            response.setThinking(false);  // 标识这是正常内容
            hasContent = true;
        }

        return hasContent ? Flux.just(response) : Flux.empty();
    }
}
