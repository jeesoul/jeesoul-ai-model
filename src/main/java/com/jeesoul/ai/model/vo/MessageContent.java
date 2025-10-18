package com.jeesoul.ai.model.vo;

import com.jeesoul.ai.model.constant.ContentType;
import com.jeesoul.ai.model.constant.ImageDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 消息内容
 * 支持多模态内容（文本、图片、音频、视频等）
 * 用于构建复杂的多模态请求
 *
 * @author dxy
 * @date 2025-10-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageContent {
    
    /**
     * 内容类型
     */
    private ContentType type;
    
    /**
     * 文本内容（当type=text时使用）
     */
    private String text;
    
    /**
     * 图片URL配置（当type=image_url时使用）
     */
    private ImageUrl imageUrl;
    
    /**
     * Base64图片数据（当type=image_base64时使用）
     */
    private String base64;
    
    /**
     * 音频URL（当type=audio_url时使用）
     */
    private String audioUrl;
    
    /**
     * 视频URL（当type=video_url时使用）
     */
    private String videoUrl;
    
    /**
     * 文件URL（当type=file_url时使用）
     */
    private String fileUrl;
    
    /**
     * 扩展字段，用于支持模型特定的参数
     */
    private Map<String, Object> extra;
    
    // ========== 静态工厂方法 ==========
    
    /**
     * 创建文本内容
     *
     * @param text 文本内容
     * @return MessageContent对象
     */
    public static MessageContent text(String text) {
        MessageContent content = new MessageContent();
        content.setType(ContentType.TEXT);
        content.setText(text);
        return content;
    }
    
    /**
     * 创建图片URL内容
     *
     * @param url 图片URL地址
     * @return MessageContent对象
     */
    public static MessageContent imageUrl(String url) {
        MessageContent content = new MessageContent();
        content.setType(ContentType.IMAGE_URL);
        ImageUrl imageUrl = new ImageUrl();
        imageUrl.setUrl(url);
        content.setImageUrl(imageUrl);
        return content;
    }
    
    /**
     * 创建图片URL内容（带详细度参数）
     *
     * @param url 图片URL地址
     * @param detail 图片详细度
     * @return MessageContent对象
     */
    public static MessageContent imageUrl(String url, ImageDetail detail) {
        MessageContent content = new MessageContent();
        content.setType(ContentType.IMAGE_URL);
        ImageUrl imageUrl = new ImageUrl();
        imageUrl.setUrl(url);
        imageUrl.setDetail(detail);
        content.setImageUrl(imageUrl);
        return content;
    }
    
    /**
     * 创建Base64图片内容
     *
     * @param base64Data Base64编码的图片数据
     * @return MessageContent对象
     */
    public static MessageContent imageBase64(String base64Data) {
        MessageContent content = new MessageContent();
        content.setType(ContentType.IMAGE_BASE64);
        content.setBase64(base64Data);
        return content;
    }
    
    /**
     * 创建音频URL内容
     *
     * @param url 音频URL地址
     * @return MessageContent对象
     */
    public static MessageContent audioUrl(String url) {
        MessageContent content = new MessageContent();
        content.setType(ContentType.AUDIO_URL);
        content.setAudioUrl(url);
        return content;
    }
    
    /**
     * 创建视频URL内容
     *
     * @param url 视频URL地址
     * @return MessageContent对象
     */
    public static MessageContent videoUrl(String url) {
        MessageContent content = new MessageContent();
        content.setType(ContentType.VIDEO_URL);
        content.setVideoUrl(url);
        return content;
    }
    
    /**
     * 创建文件URL内容
     *
     * @param url 文件URL地址
     * @return MessageContent对象
     */
    public static MessageContent fileUrl(String url) {
        MessageContent content = new MessageContent();
        content.setType(ContentType.FILE_URL);
        content.setFileUrl(url);
        return content;
    }
    
    // ========== 内部类 ==========
    
    /**
     * 图片URL配置
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageUrl {
        /**
         * 图片URL
         */
        private String url;
        
        /**
         * 图片详细度
         * AUTO: 自动选择
         * LOW: 低分辨率（快速，便宜）
         * HIGH: 高分辨率（详细，较贵）
         */
        private ImageDetail detail;
        
        /**
         * 仅设置URL的构造方法
         *
         * @param url 图片URL地址
         */
        public ImageUrl(String url) {
            this.url = url;
            this.detail = ImageDetail.AUTO;
        }
    }
}
