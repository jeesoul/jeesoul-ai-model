package com.jeesoul.ai.model.response;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * DeepSeek流式响应处理器
 *
 * @author dxy
 * @date 2025-06-10
 */
@Slf4j
public class StreamDeepSeekResponse extends StreamBaseResponse<String> {

    @Override
    protected boolean isResponseValid(Map<String, Object> responseMap) {
        return responseMap != null && responseMap.containsKey("choices");
    }

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
            log.error("提取DeepSeek响应内容失败", e);
        }
        return Flux.empty();
    }
}
