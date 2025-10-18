package com.jeesoul.ai.model.constant;

/**
 * 图片详细度枚举
 * 用于控制多模态模型对图片的处理精度
 *
 * @author dxy
 * @date 2025-10-18
 */
public enum ImageDetail {
    /**
     * 自动选择
     * 模型自动决定使用低分辨率还是高分辨率
     */
    AUTO("auto"),
    
    /**
     * 低分辨率
     * 处理速度快，成本低，适合简单识别任务
     */
    LOW("low"),
    
    /**
     * 高分辨率
     * 处理详细，效果好，但速度慢且成本高
     */
    HIGH("high");

    private final String value;

    ImageDetail(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * 根据字符串获取枚举值
     *
     * @param value 字符串值
     * @return ImageDetail枚举
     */
    public static ImageDetail fromValue(String value) {
        if (value == null) {
            return AUTO;
        }
        for (ImageDetail detail : values()) {
            if (detail.value.equalsIgnoreCase(value)) {
                return detail;
            }
        }
        throw new IllegalArgumentException("不支持的图片详细度: " + value + 
                "，支持的值: auto, low, high");
    }
}
