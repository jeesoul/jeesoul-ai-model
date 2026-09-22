package com.jeesoul.ai.model.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeesoul.ai.model.config.AiProperties;
import com.jeesoul.ai.model.constant.ImageDetail;
import com.jeesoul.ai.model.request.HttpChatGPTChatRequest;
import com.jeesoul.ai.model.util.JsonUtils;
import com.jeesoul.ai.model.vo.MessageContent;
import com.jeesoul.ai.model.vo.ModelRequestVO;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ChatGPTServiceSelfCheck {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        ChatGPTServiceSelfCheck test = new ChatGPTServiceSelfCheck();
        test.buildsOpenAiImageUrlAsContentArray();
        test.buildsOpenAiBinaryAsDataUrl();
        test.usesNewTokenParameterForGpt5();
        test.usesContentArrayForStreamingRequest();
        test.keepsPlainTextContentAsString();
        test.rejectsEmptyBinaryImage();
    }

    public void buildsOpenAiImageUrlAsContentArray() throws Exception {
        ModelRequestVO request = new ModelRequestVO()
                .setModelName("chatgpt")
                .setModel("gpt-6-astra")
                .setMaxTokens(300)
                .setContents(Arrays.asList(
                        MessageContent.imageUrl("https://example.com/photo.png", ImageDetail.HIGH),
                        MessageContent.text("请描述这张图片")
                ));

        HttpChatGPTChatRequest chatRequest = buildChatRequest(request);
        JsonNode userContent = objectMapper.readTree(JsonUtils.toJson(chatRequest))
                .path("messages").get(0).path("content");
        JsonNode root = objectMapper.readTree(JsonUtils.toJson(chatRequest));

        assertTrue(userContent.isArray(), "OpenAI content must be an array");
        assertEquals(300, root.path("max_completion_tokens").asInt());
        assertTrue(root.path("max_tokens").isMissingNode(),
                "gpt-6-astra不应发送旧的max_tokens字段");
        assertEquals("image_url", userContent.get(0).path("type").asText());
        assertEquals("https://example.com/photo.png",
                userContent.get(0).path("image_url").path("url").asText());
        assertEquals("high", userContent.get(0).path("image_url").path("detail").asText());
        assertEquals("text", userContent.get(1).path("type").asText());
        assertEquals("请描述这张图片", userContent.get(1).path("text").asText());
    }

    public void buildsOpenAiBinaryAsDataUrl() throws Exception {
        ModelRequestVO request = new ModelRequestVO()
                .setModelName("chatgpt")
                .setModel("gpt-6-astra")
                .setContents(Arrays.asList(
                        MessageContent.imageBytes(new byte[]{0, 1, 2}, "image/png"),
                        MessageContent.text("识别图片")
                ));

        HttpChatGPTChatRequest chatRequest = buildChatRequest(request);
        JsonNode imageUrl = objectMapper.readTree(JsonUtils.toJson(chatRequest))
                .path("messages").get(0).path("content").get(0).path("image_url");

        assertEquals("data:image/png;base64,AAEC", imageUrl.path("url").asText());
    }

    public void usesNewTokenParameterForGpt5() throws Exception {
        ModelRequestVO request = new ModelRequestVO()
                .setModelName("chatgpt")
                .setModel("gpt-5")
                .setMaxTokens(128)
                .setPrompt("只回复OK");

        JsonNode root = objectMapper.readTree(JsonUtils.toJson(buildChatRequest(request)));

        assertEquals(128, root.path("max_completion_tokens").asInt());
        assertTrue(root.path("max_tokens").isMissingNode(),
                "gpt-5不应发送旧的max_tokens字段");
    }

    public void usesContentArrayForStreamingRequest() throws Exception {
        ModelRequestVO request = new ModelRequestVO()
                .setModelName("chatgpt")
                .setModel("gpt-6-astra")
                .setContents(Arrays.asList(MessageContent.imageUrl("https://example.com/photo.png")));

        HttpChatGPTChatRequest chatRequest = buildChatRequest(request, true);
        JsonNode root = objectMapper.readTree(JsonUtils.toJson(chatRequest));

        assertTrue(root.path("stream").asBoolean(), "流式请求必须保留stream=true");
        assertTrue(root.path("messages").get(0).path("content").isArray(),
                "流式图片请求也必须使用content数组");
    }

    public void keepsPlainTextContentAsString() throws Exception {
        ModelRequestVO request = new ModelRequestVO()
                .setModelName("chatgpt")
                .setModel("gpt-6-astra")
                .setPrompt("只回复OK");

        HttpChatGPTChatRequest chatRequest = buildChatRequest(request);
        JsonNode content = objectMapper.readTree(JsonUtils.toJson(chatRequest))
                .path("messages").get(0).path("content");

        assertTrue(content.isTextual(), "纯文本请求必须继续使用字符串content");
        assertEquals("只回复OK", content.asText());
    }

    public void rejectsEmptyBinaryImage() {
        try {
            MessageContent.imageBytes(new byte[0], "image/png");
            throw new AssertionError("空图片数据必须被拒绝");
        } catch (IllegalArgumentException expected) {
            assertEquals("图片二进制数据不能为空", expected.getMessage());
        }
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private HttpChatGPTChatRequest buildChatRequest(ModelRequestVO request) throws Exception {
        return buildChatRequest(request, false);
    }

    private HttpChatGPTChatRequest buildChatRequest(ModelRequestVO request, boolean stream) throws Exception {
        AiProperties.ChatGPTProperties properties = new AiProperties.ChatGPTProperties();
        properties.setApiKey("test-key");
        properties.setModel("gpt-6-astra");
        ChatGPTService service = new ChatGPTService(properties);
        Method method = ChatGPTService.class.getDeclaredMethod(
                "buildChatRequest", ModelRequestVO.class, boolean.class);
        method.setAccessible(true);
        return (HttpChatGPTChatRequest) method.invoke(service, request, stream);
    }
}
