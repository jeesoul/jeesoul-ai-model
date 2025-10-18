package com.jeesoul.ai.model.constant;

/**
 * 多模态内容类型枚举
 * 定义支持的各种内容类型
 *
 * @author dxy
 * @date 2025-10-18
 */
public enum ContentType {
    /**
     * 文本内容
     */
    TEXT("text"),
    
    /**
     * 图片URL
     */
    IMAGE_URL("image_url"),
    
    /**
     * Base64编码的图片
     */
    IMAGE_BASE64("image_base64"),
    
    /**
     * 视频URL
     */
    VIDEO_URL("video_url"),
    
    /**
     * 音频URL
     */
    AUDIO_URL("audio_url"),
    
    /**
     * 文件URL
     */
    FILE_URL("file_url");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * 根据字符串获取枚举值
     *
     * @param value 字符串值
     * @return ContentType枚举
     */
    public static ContentType fromValue(String value) {
        for (ContentType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("不支持的内容类型: " + value);
    }
}
