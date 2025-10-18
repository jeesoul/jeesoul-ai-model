package com.example.ai.controller;

import com.jeesoul.ai.model.constant.ImageDetail;
import com.jeesoul.ai.model.exception.AiException;
import com.jeesoul.ai.model.factory.FactoryModelService;
import com.jeesoul.ai.model.service.AiService;
import com.jeesoul.ai.model.vo.MessageContent;
import com.jeesoul.ai.model.vo.ModelRequestVO;
import com.jeesoul.ai.model.vo.ModelResponseVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 多模态视觉示例控制器
 * 演示如何使用 QWen-VL 模型进行图片、视频分析
 * 
 * @author jeesoul
 * @date 2025-10-18
 */
@Slf4j
@RestController
@RequestMapping("/api/vision")
public class MultiModalExampleController {

    /**
     * 示例1：分析单张图片
     * 
     * POST /api/vision/analyze-image
     * {
     *   "imageUrl": "https://example.com/image.jpg",
     *   "question": "这张图片里有什么？"
     * }
     */
    @PostMapping("/analyze-image")
    public ModelResponseVO analyzeImage(@RequestBody ImageAnalysisRequest request) {
        try {
            AiService qwenVL = FactoryModelService.create("qwenVL");
            
            ModelRequestVO modelRequest = new ModelRequestVO()
                    .setModelName("qwenVL")
                    .setModel("qwen-vl-plus")
                    .setContents(Arrays.asList(
                        MessageContent.imageUrl(request.getImageUrl()),
                        MessageContent.text(request.getQuestion())
                    ));
            
            return qwenVL.httpChat(modelRequest);
        } catch (AiException e) {
            log.error("图片分析失败: {}", e.getMessage(), e);
            throw new RuntimeException("图片分析失败: " + e.getMessage());
        }
    }

    /**
     * 示例2：高分辨率图片分析
     * 
     * POST /api/vision/analyze-image-hd
     * {
     *   "imageUrl": "https://example.com/image.jpg",
     *   "question": "详细描述图片中的所有细节"
     * }
     */
    @PostMapping("/analyze-image-hd")
    public ModelResponseVO analyzeImageHD(@RequestBody ImageAnalysisRequest request) {
        try {
            AiService qwenVL = FactoryModelService.create("qwenVL");
            
            ModelRequestVO modelRequest = new ModelRequestVO()
                    .setModelName("qwenVL")
                    .setModel("qwen-vl-max")  // 使用最强模型
                    .setContents(Arrays.asList(
                        MessageContent.imageUrl(request.getImageUrl(), ImageDetail.HIGH),  // 高分辨率
                        MessageContent.text(request.getQuestion())
                    ));
            
            return qwenVL.httpChat(modelRequest);
        } catch (AiException e) {
            log.error("高清图片分析失败: {}", e.getMessage(), e);
            throw new RuntimeException("高清图片分析失败: " + e.getMessage());
        }
    }

    /**
     * 示例3：对比多张图片
     * 
     * POST /api/vision/compare-images
     * {
     *   "imageUrls": [
     *     "https://example.com/image1.jpg",
     *     "https://example.com/image2.jpg"
     *   ],
     *   "question": "这两张图片有什么区别？"
     * }
     */
    @PostMapping("/compare-images")
    public ModelResponseVO compareImages(@RequestBody CompareImagesRequest request) {
        try {
            AiService qwenVL = FactoryModelService.create("qwenVL");
            
            List<MessageContent> contents = new ArrayList<>();
            contents.add(MessageContent.text(request.getQuestion()));
            
            // 添加所有图片
            for (String imageUrl : request.getImageUrls()) {
                contents.add(MessageContent.imageUrl(imageUrl));
            }
            
            ModelRequestVO modelRequest = new ModelRequestVO()
                    .setModelName("qwenVL")
                    .setModel("qwen-vl-plus")
                    .setContents(contents);
            
            return qwenVL.httpChat(modelRequest);
        } catch (AiException e) {
            log.error("图片对比失败: {}", e.getMessage(), e);
            throw new RuntimeException("图片对比失败: " + e.getMessage());
        }
    }

    /**
     * 示例4：OCR 文字识别
     * 
     * POST /api/vision/ocr
     * {
     *   "imageUrl": "https://example.com/document.jpg"
     * }
     */
    @PostMapping("/ocr")
    public ModelResponseVO ocr(@RequestBody OcrRequest request) {
        try {
            AiService qwenVL = FactoryModelService.create("qwenVL");
            
            ModelRequestVO modelRequest = new ModelRequestVO()
                    .setModelName("qwenVL")
                    .setModel("qwen-vl-plus")
                    .setContents(Arrays.asList(
                        MessageContent.imageUrl(request.getImageUrl()),
                        MessageContent.text("提取图片中的所有文字，保持原有格式")
                    ));
            
            return qwenVL.httpChat(modelRequest);
        } catch (AiException e) {
            log.error("OCR识别失败: {}", e.getMessage(), e);
            throw new RuntimeException("OCR识别失败: " + e.getMessage());
        }
    }

    /**
     * 示例5：视频分析
     * 
     * POST /api/vision/analyze-video
     * {
     *   "videoUrl": "https://example.com/video.mp4",
     *   "question": "总结这个视频的主要内容"
     * }
     */
    @PostMapping("/analyze-video")
    public ModelResponseVO analyzeVideo(@RequestBody VideoAnalysisRequest request) {
        try {
            AiService qwenVL = FactoryModelService.create("qwenVL");
            
            ModelRequestVO modelRequest = new ModelRequestVO()
                    .setModelName("qwenVL")
                    .setModel("qwen3-vl-plus")  // 支持视频的模型
                    .setContents(Arrays.asList(
                        MessageContent.videoUrl(request.getVideoUrl()),
                        MessageContent.text(request.getQuestion())
                    ));
            
            return qwenVL.httpChat(modelRequest);
        } catch (AiException e) {
            log.error("视频分析失败: {}", e.getMessage(), e);
            throw new RuntimeException("视频分析失败: " + e.getMessage());
        }
    }

    /**
     * 示例6：启用思考模式
     * 
     * POST /api/vision/analyze-with-thinking
     * {
     *   "imageUrl": "https://example.com/math-problem.jpg",
     *   "question": "解答图片中的数学问题"
     * }
     */
    @PostMapping("/analyze-with-thinking")
    public ModelResponseVO analyzeWithThinking(@RequestBody ImageAnalysisRequest request) {
        try {
            AiService qwenVL = FactoryModelService.create("qwenVL");
            
            ModelRequestVO modelRequest = new ModelRequestVO()
                    .setModelName("qwenVL")
                    .setModel("qwen3-vl-plus")
                    .setEnableThinking(true)  // 启用思考模式
                    .setContents(Arrays.asList(
                        MessageContent.imageUrl(request.getImageUrl()),
                        MessageContent.text(request.getQuestion())
                    ));
            
            ModelResponseVO response = qwenVL.httpChat(modelRequest);
            
            // 思考模式会返回 thinking 字段
            if (response.getThinking() != null) {
                log.info("思考过程: {}", response.getThinking());
            }
            
            return response;
        } catch (AiException e) {
            log.error("思考模式分析失败: {}", e.getMessage(), e);
            throw new RuntimeException("思考模式分析失败: " + e.getMessage());
        }
    }

    /**
     * 示例7：流式图片分析
     * 
     * POST /api/vision/stream-analyze
     * {
     *   "imageUrl": "https://example.com/image.jpg",
     *   "question": "详细描述这张图片"
     * }
     */
    @PostMapping(value = "/stream-analyze", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamAnalyze(@RequestBody ImageAnalysisRequest request) {
        try {
            AiService qwenVL = FactoryModelService.create("qwenVL");
            
            ModelRequestVO modelRequest = new ModelRequestVO()
                    .setModelName("qwenVL")
                    .setModel("qwen-vl-plus")
                    .setContents(Arrays.asList(
                        MessageContent.imageUrl(request.getImageUrl()),
                        MessageContent.text(request.getQuestion())
                    ));
            
            return qwenVL.streamChatStr(modelRequest);
        } catch (AiException e) {
            log.error("流式分析失败: {}", e.getMessage(), e);
            return Flux.error(new RuntimeException("流式分析失败: " + e.getMessage()));
        }
    }

    /**
     * 示例8：商品图片分析
     * 
     * POST /api/vision/analyze-product
     * {
     *   "imageUrl": "https://example.com/product.jpg"
     * }
     */
    @PostMapping("/analyze-product")
    public ModelResponseVO analyzeProduct(@RequestBody OcrRequest request) {
        try {
            AiService qwenVL = FactoryModelService.create("qwenVL");
            
            String prompt = """
                    分析这个商品图片，提供以下信息：
                    1. 商品类别和名称
                    2. 主要特征和卖点
                    3. 可能的材质或成分
                    4. 适用场景
                    5. 估计的价格区间
                    6. 目标用户群体
                    """;
            
            ModelRequestVO modelRequest = new ModelRequestVO()
                    .setModelName("qwenVL")
                    .setModel("qwen-vl-plus")
                    .setContents(Arrays.asList(
                        MessageContent.imageUrl(request.getImageUrl()),
                        MessageContent.text(prompt)
                    ));
            
            return qwenVL.httpChat(modelRequest);
        } catch (AiException e) {
            log.error("商品分析失败: {}", e.getMessage(), e);
            throw new RuntimeException("商品分析失败: " + e.getMessage());
        }
    }

    // ==================== 请求对象 ====================

    @Data
    public static class ImageAnalysisRequest {
        private String imageUrl;
        private String question;
    }

    @Data
    public static class CompareImagesRequest {
        private List<String> imageUrls;
        private String question;
    }

    @Data
    public static class OcrRequest {
        private String imageUrl;
    }

    @Data
    public static class VideoAnalysisRequest {
        private String videoUrl;
        private String question;
    }
}
