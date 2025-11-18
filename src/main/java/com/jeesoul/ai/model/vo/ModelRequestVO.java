package com.jeesoul.ai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 大模型请求参数 VO。
 * 封装模型类型、提示词和其他参数。
 * 支持链式调用设置参数。
 *
 * @author dxy
 * @date 2025-06-10
 */
@Data
@Accessors(chain = true)
public class ModelRequestVO {
    /**
     * 模型提供商名称（必填）
     * 可选值: qWen, chatgpt, spark, deepSeek
     * 示例: "qWen"
     */
    private String modelName;
    
    /**
     * 具体模型版本（必填）
     * 示例:
     * - qWen: "qwen-turbo", "qwen-plus", "qwen-max"
     * - chatgpt: "gpt-3.5-turbo", "gpt-4", "gpt-4-turbo"
     * - spark: "x1"
     * - deepSeek: "deepseek-chat", "deepseek-coder"
     */
    private String model;

    /**
     * 系统提示词（可选）
     * 用于设定AI的角色和行为规范
     * 所有模型都支持此参数，会将其转换为消息列表中的 system 角色消息
     * 示例: "你是一个专业的Java工程师"
     */
    private String systemPrompt;
    
    /**
     * 用户提示词（必填，简单模式）
     * 用户的具体问题或指令
     * 示例: "请帮我写一个快速排序算法"
     * 
     * 注意：
     * - 如果只需要发送纯文本，使用此字段即可
     * - 如果需要多模态输入（图片、音频等），使用 contents 字段
     * - 如果同时设置了 prompt 和 contents，优先使用 contents
     */
    private String prompt;
    
    /**
     * 多模态消息内容（可选，多模态模式）
     * 用于支持图片、音频、视频等多模态输入
     * 
     * 使用示例：
     * <pre>
     * List<MessageContent> contents = Arrays.asList(
     *     MessageContent.imageUrl("https://example.com/image.jpg"),
     *     MessageContent.text("这是什么？")
     * );
     * request.setContents(contents);
     * </pre>
     * 
     * 注意：
     * - 如果设置了此字段，prompt 字段会被忽略
     * - 仅支持多模态的模型才能使用此字段（如 qwen-vl-plus）
     * - 不同模型对多模态内容的顺序和组合要求可能不同
     */
    private List<MessageContent> contents;
    
    /**
     * 是否开启思考模式（可选）
     * 注意: 仅 QWen 支持此参数，其他模型会忽略
     * 启用后模型会先进行思考，然后给出答案
     * 默认: false
     */
    private boolean enableThinking;
    
    /**
     * 采样温度（可选）
     * 控制生成内容的随机性和创造性
     * 取值范围: 0.0 ~ 2.0
     * - 较低值(0.0-0.5): 更确定、更保守的输出
     * - 中等值(0.5-1.0): 平衡创造性和确定性
     * - 较高值(1.0-2.0): 更有创造性和随机性
     * 默认: 0.7
     */
    private Double temperature = 0.7;
    
    /**
     * 核采样参数（可选）
     * 控制输出多样性的另一种方式
     * 取值范围: 0.0 ~ 1.0
     * 值越小，输出越确定；值越大，输出越多样
     * 默认: null（使用模型默认值）
     * 注意: 不建议同时调整 temperature 和 topP
     */
    private Double topP;
    
    /**
     * 最大生成Token数（可选）
     * 控制生成内容的最大长度
     * 注意: 1个Token约等于1.5个汉字或0.75个英文单词
     * 示例: 2000 表示最多生成约3000个汉字
     * 默认: null（使用模型默认值）
     */
    private Integer maxTokens;
    
    /**
     * 扩展参数（可选）
     * 用于传递模型特定的自定义参数
     * 框架会尝试通过反射将这些参数设置到请求体中
     * 示例:
     * - frequencyPenalty: 频率惩罚
     * - presencePenalty: 存在惩罚
     * - stop: 停止序列
     * 默认: null
     */
    private Map<String, Object> params;

    /**
     * 消息列表（可选，用于支持聊天上下文）
     * 用于构建多轮对话的上下文消息
     * 如果设置了此字段，将优先使用此字段构建消息列表
     * 
     * 使用示例：
     * <pre>
     * List&lt;Message&gt; messages = Arrays.asList(
     *     new Message().setRole("user").setContent("你好"),
     *     new Message().setRole("assistant").setContent("你好！有什么可以帮助你的吗？"),
     *     new Message().setRole("user").setContent("请介绍一下Java")
     * );
     * request.setMessages(messages);
     * </pre>
     * 
     * 注意：
     * - 如果设置了此字段，prompt 和 contents 字段可能会被忽略
     * - 消息列表中的角色通常为: "user"（用户）、"assistant"（助手）、"system"（系统）
     * - 不同模型对消息格式的要求可能不同
     */
    private List<Message> messages;

    /**
     * 消息实体类
     */
    @Data
    @Accessors(chain = true)
    public static class Message {
        /**
         * 角色
         */
        private String role;

        /**
         * 内容
         */
        private String content;
    }
}
