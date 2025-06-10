package com.jeesoul.ai.model.response;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * ChatGPT流式响应处理器
 * 用于处理ChatGPT服务的流式响应，继承自StreamBaseResponse
 * 主要功能：
 * 1. 验证ChatGPT响应状态码
 * 2. 提取ChatGPT响应中的文本内容
 * 响应格式示例：
 * {
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
public class StreamChatGPTResponse extends StreamBaseResponse<String> {

    /**
     * 构造函数
     */
    public StreamChatGPTResponse() {
        super();
    }

    /**
     * 检查响应是否有效
     * 验证规则：
     * 1. 检查响应中是否包含choices字段
     * 2. 验证choices是否为非空列表
     *
     * @param responseMap 解析后的响应数据Map
     * @return true表示响应有效，false表示响应无效
     */
    @Override
    protected boolean isResponseValid(Map<String, Object> responseMap) {
        return responseMap != null && responseMap.containsKey("choices");
    }

    /**
     * 提取响应内容
     * 提取规则：
     * 1. 检查响应中是否包含choices字段
     * 2. 获取第一个choice中的delta字段
     * 3. 获取content字段内容
     * 4. 如果无法提取则返回空流
     *
     * @param responseMap 解析后的响应数据Map
     * @return 提取的文本内容流，如果无法提取则返回空流
     */
    @Override
    protected Flux<String> extractContent(Map<String, Object> responseMap) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                @SuppressWarnings("unchecked")
                Map<String, Object> delta = (Map<String, Object>) choice.get("delta");
                if (delta != null && delta.containsKey("content")) {
                    return Flux.just((String) delta.get("content"));
                }
            }
        } catch (Exception e) {
            log.error("提取ChatGPT响应内容失败", e);
        }
        return Flux.empty();
    }
}
