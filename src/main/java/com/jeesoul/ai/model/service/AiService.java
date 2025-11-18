package com.jeesoul.ai.model.service;

import com.jeesoul.ai.model.exception.AiException;
import com.jeesoul.ai.model.vo.ModelRequestVO;
import com.jeesoul.ai.model.vo.ModelResponseVO;
import reactor.core.publisher.Flux;

/**
 * 大模型服务接口
 * 定义了与大模型交互的通用方法，包括同步和流式对话两种模式
 * 该接口可以被不同的AI服务提供商实现，如讯飞星火、ChatGPT等
 *
 * @author dxy
 * @date 2025-06-10
 */
public interface AiService {

    /**
     * 通过 HTTP 与大模型进行同步对话
     * 该方法会等待大模型完整响应后一次性返回结果
     *
     * @param request 包含对话内容、参数等信息的请求对象
     * @return 大模型的响应结果，包含生成的文本内容
     * @throws AiException 当与大模型交互过程中发生错误时抛出
     */
    ModelResponseVO httpChat(ModelRequestVO request) throws AiException;

    /**
     * 通过流式方式与大模型进行对话
     * 该方法支持实时返回大模型的生成结果，适用于长文本生成场景
     *
     * @param request 包含对话内容、参数等信息的请求对象
     * @return 大模型的流式响应结果
     * @throws AiException 当与大模型交互过程中发生错误时抛出
     */
    Flux<ModelResponseVO> streamChat(ModelRequestVO request) throws AiException;

    /**
     * 通过流式方式与大模型进行对话，返回原始文本流
     * 该方法直接返回文本内容流，不进行额外的对象封装
     *
     * @param request 包含对话内容、参数等信息的请求对象
     * @return 大模型的原始文本流
     * @throws AiException 当与大模型交互过程中发生错误时抛出
     */
    Flux<String> streamChatStr(ModelRequestVO request) throws AiException;

    /**
     * 通过 HTTP 与大模型进行同步对话，返回原始响应数据
     * 该方法返回模型响应的原始 JSON 字符串，不进行任何解析和封装
     * 适用于需要获取完整响应数据的场景，如调试、日志记录等
     *
     * @param request 包含对话内容、参数等信息的请求对象
     * @return 大模型的原始响应数据（JSON字符串）
     * @throws AiException 当与大模型交互过程中发生错误时抛出
     */
    String httpChatRaw(ModelRequestVO request) throws AiException;

    /**
     * 通过流式方式与大模型进行对话，返回原始响应数据流
     * 该方法返回模型响应的原始 JSON 字符串流，不进行任何解析和封装
     * 适用于需要获取完整流式响应数据的场景，如调试、日志记录等
     *
     * @param request 包含对话内容、参数等信息的请求对象
     * @return 大模型的原始响应数据流（JSON字符串流）
     * @throws AiException 当与大模型交互过程中发生错误时抛出
     */
    Flux<String> streamChatRaw(ModelRequestVO request) throws AiException;
}

