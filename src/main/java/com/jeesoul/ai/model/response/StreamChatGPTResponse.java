package com.jeesoul.ai.model.response;

import com.jeesoul.ai.model.entity.ResultContent;
import com.jeesoul.ai.model.util.JsonUtils;
import com.jeesoul.ai.model.vo.TokenUsageVO;
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
 * 3. 提取usage信息（在最后一个chunk中）
 * 响应格式示例：
 * {
 * "choices": [{
 * "delta": {
 * "content": "文本内容"
 * }
 * }],
 * "usage": { // 仅在最后一个chunk中出现
 * "prompt_tokens": 10,
 * "completion_tokens": 20,
 * "total_tokens": 30
 * }
 * }
 *
 * @author dxy
 * @date 2025-06-10
 */
@Slf4j
public class StreamChatGPTResponse extends StreamBaseResponse<ResultContent> {

    /**
     * 构造函数
     */
    public StreamChatGPTResponse() {
        super();
    }

    /**
     * 检查响应是否有效
     * 验证规则：
     * 1. 检查响应中是否包含choices字段或usage字段
     * 2. 验证choices是否为非空列表
     *
     * @param responseMap 解析后的响应数据Map
     * @return true表示响应有效，false表示响应无效
     */
    @Override
    protected boolean isResponseValid(Map<String, Object> responseMap) {
        return responseMap != null && 
               (responseMap.containsKey("choices") || responseMap.containsKey("usage"));
    }

    /**
     * 提取响应内容
     * 提取规则：
     * 1. 提取usage信息（如果存在）
     * 2. 检查响应中是否包含choices字段
     * 3. 获取第一个choice中的delta字段
     * 4. 获取content字段内容
     * 5. 如果无法提取则返回空流
     *
     * @param responseMap 解析后的响应数据Map
     * @return 提取的文本内容流，如果无法提取则返回空流
     */
    @Override
    protected Flux<ResultContent> extractContent(Map<String, Object> responseMap) {
        ResultContent result = new ResultContent();
        boolean hasContent = false;

        try {
            // 提取usage信息（通常在最后一个chunk中）
            if (responseMap.containsKey("usage")) {
                Map<String, Object> usageMap = JsonUtils.toMap(responseMap.get("usage"));
                if (usageMap != null) {
                    Integer promptTokens = (Integer) usageMap.get("prompt_tokens");
                    Integer completionTokens = (Integer) usageMap.get("completion_tokens");
                    Integer totalTokens = (Integer) usageMap.get("total_tokens");
                    if (promptTokens != null || completionTokens != null || totalTokens != null) {
                        result.setUsage(TokenUsageVO.of(promptTokens, completionTokens, totalTokens));
                        hasContent = true;
                    }
                }
            }

            // 提取内容
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                @SuppressWarnings("unchecked")
                Map<String, Object> delta = (Map<String, Object>) choice.get("delta");
                if (delta != null && delta.containsKey("content")) {
                    String content = (String) delta.get("content");
                    result.setContent(content);
                    hasContent = true;
                }
            }
        } catch (Exception e) {
            log.error("提取ChatGPT响应内容失败", e);
        }

        return hasContent ? Flux.just(result) : Flux.empty();
    }
}
