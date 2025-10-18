package com.example.ai.controller;

import com.jeesoul.ai.model.exception.AiException;
import com.jeesoul.ai.model.factory.FactoryModelService;
import com.jeesoul.ai.model.service.AiService;
import com.jeesoul.ai.model.vo.ModelRequestVO;
import com.jeesoul.ai.model.vo.ModelResponseVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * Claude AI REST API 示例控制器
 * 
 * @author jeesoul
 * @date 2025-10-18
 */
@Slf4j
@RestController
@RequestMapping("/api/claude")
public class ClaudeController {

    /**
     * 同步对话接口
     * 
     * @param chatRequest 对话请求
     * @return 对话响应
     */
    @PostMapping("/chat")
    public ModelResponseVO chat(@RequestBody ChatRequest chatRequest) {
        try {
            // 创建 Claude 服务
            AiService claudeService = FactoryModelService.create("claude");
            
            // 构建请求
            ModelRequestVO request = new ModelRequestVO()
                    .setModelName("claude")
                    .setModel(chatRequest.getModel() != null ? chatRequest.getModel() : "claude-3-opus-20240229")
                    .setPrompt(chatRequest.getPrompt())
                    .setSystemPrompt(chatRequest.getSystemPrompt())
                    .setTemperature(chatRequest.getTemperature())
                    .setMaxTokens(chatRequest.getMaxTokens());
            
            // 调用模型
            return claudeService.httpChat(request);
        } catch (AiException e) {
            log.error("Claude调用失败: {}", e.getMessage(), e);
            throw new RuntimeException("AI服务调用失败: " + e.getMessage());
        }
    }

    /**
     * 流式对话接口
     * 
     * @param chatRequest 对话请求
     * @return 流式响应
     */
    @PostMapping(value = "/stream-chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody ChatRequest chatRequest) {
        try {
            AiService claudeService = FactoryModelService.create("claude");
            
            ModelRequestVO request = new ModelRequestVO()
                    .setModelName("claude")
                    .setModel(chatRequest.getModel() != null ? chatRequest.getModel() : "claude-3-opus-20240229")
                    .setPrompt(chatRequest.getPrompt())
                    .setSystemPrompt(chatRequest.getSystemPrompt());
            
            return claudeService.streamChatStr(request);
        } catch (AiException e) {
            log.error("Claude流式调用失败: {}", e.getMessage(), e);
            return Flux.error(new RuntimeException("AI服务调用失败: " + e.getMessage()));
        }
    }

    /**
     * 多模型对比接口
     * 同时调用 Claude 和 QWen，对比回答
     * 
     * @param chatRequest 对话请求
     * @return 对比结果
     */
    @PostMapping("/compare")
    public Map<String, ModelResponseVO> compare(@RequestBody ChatRequest chatRequest) {
        Map<String, ModelResponseVO> results = new HashMap<>();
        
        try {
            // 调用 Claude
            AiService claudeService = FactoryModelService.create("claude");
            ModelRequestVO claudeRequest = new ModelRequestVO()
                    .setModelName("claude")
                    .setModel("claude-3-opus-20240229")
                    .setPrompt(chatRequest.getPrompt())
                    .setSystemPrompt(chatRequest.getSystemPrompt());
            
            results.put("claude", claudeService.httpChat(claudeRequest));
        } catch (Exception e) {
            log.error("Claude调用失败: {}", e.getMessage(), e);
            ModelResponseVO errorResponse = new ModelResponseVO();
            errorResponse.setResult("调用失败: " + e.getMessage());
            results.put("claude", errorResponse);
        }
        
        try {
            // 调用 QWen
            AiService qwenService = FactoryModelService.create("qWen");
            ModelRequestVO qwenRequest = new ModelRequestVO()
                    .setModelName("qWen")
                    .setModel("qwen-turbo")
                    .setPrompt(chatRequest.getPrompt());
            
            results.put("qwen", qwenService.httpChat(qwenRequest));
        } catch (Exception e) {
            log.error("QWen调用失败: {}", e.getMessage(), e);
            ModelResponseVO errorResponse = new ModelResponseVO();
            errorResponse.setResult("调用失败: " + e.getMessage());
            results.put("qwen", errorResponse);
        }
        
        return results;
    }

    /**
     * 健康检查接口
     * 
     * @return 状态信息
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "claude-ai");
        health.put("timestamp", System.currentTimeMillis());
        
        try {
            FactoryModelService.create("claude");
            health.put("claude", "available");
        } catch (Exception e) {
            health.put("claude", "unavailable");
            health.put("error", e.getMessage());
        }
        
        return health;
    }

    // ==================== 请求对象 ====================

    @Data
    public static class ChatRequest {
        /**
         * 用户输入
         */
        private String prompt;
        
        /**
         * 系统提示词（可选）
         */
        private String systemPrompt;
        
        /**
         * 模型名称（可选，默认使用 claude-3-opus-20240229）
         */
        private String model;
        
        /**
         * 采样温度（可选，0-1）
         */
        private Double temperature;
        
        /**
         * 最大Token数（可选）
         */
        private Integer maxTokens;
    }
}
