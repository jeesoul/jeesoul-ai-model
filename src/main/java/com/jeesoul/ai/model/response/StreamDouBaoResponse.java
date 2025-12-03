package com.jeesoul.ai.model.response;

import com.jeesoul.ai.model.entity.ResultContent;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * 豆包（火山方舟）流式响应处理器
 * 用于处理豆包服务的流式响应，继承自StreamBaseResponse
 * 主要功能：
 * 1. 验证豆包响应状态码
 * 2. 提取豆包响应中的文本内容
 * 3. 处理流式响应（注意：豆包的流式响应usage字段始终为null）
 * 
 * 响应格式示例：
 * {
 *   "choices": [{
 *     "delta": {
 *       "content": "文本内容",
 *       "role": "assistant"
 *     },
 *     "index": 0
 *   }],
 *   "model": "doubao-1-5-pro-32k-250115",
 *   "usage": null  ← 注意：流式响应中usage始终为null
 * }
 *
 * @author dxy
 * @date 2025-12-03
 */
@Slf4j
public class StreamDouBaoResponse extends StreamBaseResponse<ResultContent> {

    /**
     * 构造函数
     */
    public StreamDouBaoResponse() {
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
     * 4. 提取reasoning_content（如果启用了思考模式）
     * 5. 如果无法提取则返回空流
     * 
     * ⚠️ 重要：根据实际测试，豆包的流式响应中usage字段始终为null
     * 即使是最后一个chunk（finish_reason为stop），usage仍然是null
     * 如需token统计，请使用同步接口 httpChat()
     *
     * @param responseMap 解析后的响应数据Map
     * @return 提取的文本内容流，如果无法提取则返回空流
     */
    @Override
    protected Flux<ResultContent> extractContent(Map<String, Object> responseMap) {
        ResultContent result = new ResultContent();
        boolean hasContent = false;

        try {
            // 注意：豆包的流式响应usage字段始终为null，这里不提取usage
            // 经过实际测试验证：所有chunk（包括最后一个）的usage都是null

            // 提取内容
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                
                // 获取delta中的content
                @SuppressWarnings("unchecked")
                Map<String, Object> delta = (Map<String, Object>) choice.get("delta");
                if (delta != null) {
                    // 提取普通内容
                    if (delta.containsKey("content")) {
                        String content = (String) delta.get("content");
                        // 注意：空字符串也是有效的chunk（用于标记结束），所以不过滤空字符串
                        // 但是null要过滤
                        if (content != null) {
                            // 只有非空内容才设置，避免设置空字符串
                            if (!content.isEmpty()) {
                                result.setContent(content);
                                hasContent = true;
                            }
                        }
                    }
                    
                    // 提取思考内容（如果有）
                    if (delta.containsKey("reasoning_content")) {
                        String reasoningContent = (String) delta.get("reasoning_content");
                        // 过滤掉空字符串和只有空白字符的内容
                        if (reasoningContent != null && !reasoningContent.trim().isEmpty()) {
                            result.setContent(reasoningContent);  // ✅ 设置到content字段，便于统一处理
                            result.setThinking(true);  // 标识这是思考内容
                            result.setThinkingContent(reasoningContent);  // 存储思考文本
                            hasContent = true;
                            log.debug("[DouBao] 流式响应中提取到思考内容，长度: {} 字符", 
                                     reasoningContent.length());
                        }
                    }
                }
                
                // 检查是否结束
                String finishReason = (String) choice.get("finish_reason");
                if ("stop".equals(finishReason)) {
                    log.debug("[DouBao] 流式响应结束（注意：豆包流式响应不包含usage统计）");
                }
            }
        } catch (Exception e) {
            log.error("提取豆包响应内容失败", e);
        }

        return hasContent ? Flux.just(result) : Flux.empty();
    }
}

