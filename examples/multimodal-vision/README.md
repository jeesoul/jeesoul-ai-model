# 多模态视觉理解示例

本示例演示如何使用千问视觉模型（qwenVL）进行图片和视频分析。

## 📋 文件说明

- **`MultiModalExampleController.java`** - 多模态示例控制器（8个完整示例）
- **`application.yml`** - 配置文件

## 🎯 示例功能

1. ✅ 单张图片分析 (`/api/vision/analyze-image`)
2. ✅ 高分辨率图片分析 (`/api/vision/analyze-image-hd`)
3. ✅ 多张图片对比 (`/api/vision/compare-images`)
4. ✅ OCR 文字识别 (`/api/vision/ocr`)
5. ✅ 视频内容理解 (`/api/vision/analyze-video`)
6. ✅ 思考模式 (`/api/vision/analyze-with-thinking`)
7. ✅ 流式图片分析 (`/api/vision/stream-analyze`)
8. ✅ 商品图片分析 (`/api/vision/analyze-product`)

## 🚀 快速开始

### 1. 复制代码到你的项目

```bash
# 复制控制器到你的项目
cp MultiModalExampleController.java src/main/java/com/yourcompany/controller/

# 复制配置文件
cp application.yml src/main/resources/
```

### 2. 配置 API 密钥

```bash
# 方式1：环境变量
export QWEN_API_KEY=your-api-key

# 方式2：直接在 application.yml 中配置
ai:
  qwen-vl:
    api-key: your-api-key
```

### 3. 启动应用

```bash
mvn spring-boot:run
```

## 📝 API 测试示例

### 1. 分析图片

```bash
curl -X POST http://localhost:8080/api/vision/analyze-image \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrl": "https://example.com/dog.jpg",
    "question": "这是什么动物？"
  }'
```

### 2. 高分辨率分析

```bash
curl -X POST http://localhost:8080/api/vision/analyze-image-hd \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrl": "https://example.com/detailed.jpg",
    "question": "详细描述图片中的所有细节"
  }'
```

### 3. 对比多张图片

```bash
curl -X POST http://localhost:8080/api/vision/compare-images \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrls": [
      "https://example.com/image1.jpg",
      "https://example.com/image2.jpg"
    ],
    "question": "这两张图片有什么区别？"
  }'
```

### 4. OCR 文字识别

```bash
curl -X POST http://localhost:8080/api/vision/ocr \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrl": "https://example.com/document.jpg"
  }'
```

### 5. 视频分析

```bash
curl -X POST http://localhost:8080/api/vision/analyze-video \
  -H "Content-Type: application/json" \
  -d '{
    "videoUrl": "https://example.com/video.mp4",
    "question": "总结这个视频的主要内容"
  }'
```

### 6. 启用思考模式

```bash
curl -X POST http://localhost:8080/api/vision/analyze-with-thinking \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrl": "https://example.com/math.jpg",
    "question": "解答图片中的数学问题"
  }'
```

### 7. 流式分析

```bash
curl -N -X POST http://localhost:8080/api/vision/stream-analyze \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrl": "https://example.com/image.jpg",
    "question": "详细描述这张图片"
  }'
```

### 8. 商品分析

```bash
curl -X POST http://localhost:8080/api/vision/analyze-product \
  -H "Content-Type: application/json" \
  -d '{
    "imageUrl": "https://example.com/product.jpg"
  }'
```

## 🎯 支持的模型

| 模型 | 支持内容 | 特点 | 使用场景 |
|------|---------|------|---------|
| qwen-vl-plus | 图片 + 文本 | 性价比高 | 日常图片分析 |
| qwen-vl-max | 图片 + 文本 | 效果最好 | 专业图片分析 |
| qwen3-vl-plus | 图片 + 视频 + 文本 | 支持视频、思考模式 | 复杂场景分析 |

## 📊 Token 统计与成本分析（v1.0.9+）

```java
// 获取 Token 使用统计
ModelResponseVO response = aiService.httpChat(request);
TokenUsageVO usage = response.getUsage();

System.out.println("输入Token: " + usage.getInputTokens());      // QWen特有字段
System.out.println("输出Token: " + usage.getOutputTokens());     // QWen特有字段
System.out.println("总Token: " + usage.getTotalTokens());

// 模型信息
System.out.println("提供商: " + response.getModelProvider());    // qWen
System.out.println("模型版本: " + response.getModelName());      // qwen-vl-plus

// 成本估算
double cost = (usage.getInputTokens() * 0.0001) + (usage.getOutputTokens() * 0.0002);
System.out.println("预估成本: ¥" + cost);
```

## 💡 最佳实践

### 1. 图片要求
- ✅ 确保图片 URL 可公开访问
- ✅ 建议图片大小 < 10MB
- ✅ 支持格式：JPG、PNG、WebP
- ✅ 使用 HTTPS 协议（推荐）

### 2. 性能优化
- 🚀 使用流式接口获得更好的用户体验
- 🚀 对相同图片的分析可以缓存结果
- 🚀 视频分析耗时较长，建议异步处理

### 3. 成本控制
- 💰 日常分析使用 qwen-vl-plus
- 💰 高精度需求使用 qwen-vl-max
- 💰 避免不必要的高分辨率分析
- 💰 合理设置 maxTokens 参数

### 4. 提示词优化
```java
// ❌ 不好的提示词
"这是什么？"

// ✅ 好的提示词
"详细描述这张图片的内容，包括：\n" +
"1. 主要物体和人物\n" +
"2. 场景和环境\n" +
"3. 颜色和光线\n" +
"4. 可能的情感或氛围"
```

## 🐛 故障排查

### 问题1：图片无法加载
**错误**: `AiException: 无法访问图片URL`  
**解决方案**:
1. 检查 URL 是否可访问
2. 检查是否有防盗链
3. 尝试使用 Base64 编码

### 问题2：响应时间过长
**原因**: 视频分析或高分辨率图片  
**解决方案**:
1. 使用流式接口
2. 异步处理
3. 降低图片分辨率

### 问题3：分析结果不准确
**解决方案**:
1. 使用更强的模型（qwen-vl-max）
2. 启用高分辨率模式（ImageDetail.HIGH）
3. 优化提示词描述

## 📚 相关文档

- [主框架文档](../../README.md)
- [千问 VL 官方文档](https://help.aliyun.com/document_detail/2712265.html)
