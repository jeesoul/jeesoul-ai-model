package com.jeesoul.ai.model.response;

import com.jeesoul.ai.model.entity.ResultContent;
import com.jeesoul.ai.model.util.JsonUtils;
import com.jeesoul.ai.model.vo.TokenUsageVO;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * DeepSeek流式响应处理器
 * 支持提取内容和usage信息
 *
 * @author dxy
 * @date 2025-06-10
 */
@Slf4j
public class StreamDeepSeekResponse extends StreamBaseResponse<ResultContent> {

    @Override
    protected boolean isResponseValid(Map<String, Object> responseMap) {
        return responseMap != null && 
               (responseMap.containsKey("choices") || responseMap.containsKey("usage"));
    }

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
                if (delta != null) {
                    // 提取普通内容
                    if (delta.containsKey("content")) {
                        String content = (String) delta.get("content");
                        if (content != null && !content.isEmpty()) {
                            result.setContent(content);
                            result.setThinking(false);  // 标识这是正常内容
                            hasContent = true;
                        }
                    }
                    
                    // 提取思考内容（reasoning_content）
                    // 文档：https://api-docs.deepseek.com/zh-cn/guides/thinking_mode
                    if (delta.containsKey("reasoning_content")) {
                        String reasoningContent = (String) delta.get("reasoning_content");
                        if (reasoningContent != null && !reasoningContent.trim().isEmpty()) {
                            result.setContent(reasoningContent);  // ✅ 设置到content字段，便于统一处理
                            result.setThinking(true);  // 标识这是思考内容
                            result.setThinkingContent(reasoningContent);  // 存储思考文本
                            hasContent = true;
                            log.debug("[DeepSeek] 流式响应中提取到思考内容，长度: {} 字符", 
                                     reasoningContent.length());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("提取DeepSeek响应内容失败", e);
        }

        return hasContent ? Flux.just(result) : Flux.empty();
    }
}
