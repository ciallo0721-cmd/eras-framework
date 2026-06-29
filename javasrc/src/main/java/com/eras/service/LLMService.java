package com.eras.service;

import com.eras.model.ApiConfig;
import com.eras.util.JsonUtil;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * LLM 服务 - 支持 OpenAI 和 Ollama
 */
public class LLMService {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private ApiConfig config;

    public LLMService(String configPath) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        loadConfig(configPath);
    }

    /**
     * 加载API配置
     */
    public void loadConfig(String configPath) {
        this.config = JsonUtil.readFromFile(configPath, ApiConfig.class);
        if (this.config == null) {
            this.config = new ApiConfig();
        }
    }

    /**
     * 发送聊天请求
     */
    public String chat(String systemPrompt, String userMessage) throws IOException {
        if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
            // 无 API key 时返回模拟回复
            return generateMockReply(userMessage);
        }

        if ("ollama".equalsIgnoreCase(config.getProvider())) {
            return callOllama(systemPrompt, userMessage);
        } else {
            return callOpenAI(systemPrompt, userMessage);
        }
    }

    /**
     * 带图片的聊天请求
     */
    public String chatWithImage(String systemPrompt, String userMessage, String base64Image) throws IOException {
        if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
            return "[识图] " + generateMockReply(userMessage);
        }

        if ("ollama".equalsIgnoreCase(config.getProvider())) {
            return callOllamaWithImage(systemPrompt, userMessage, base64Image);
        } else {
            return callOpenAIWithImage(systemPrompt, userMessage, base64Image);
        }
    }

    /**
     * 调用 OpenAI 兼容 API
     */
    private String callOpenAI(String systemPrompt, String userMessage) throws IOException {
        String url = config.getApiUrl().replaceAll("/+$", "") + "/chat/completions";
        String json = buildOpenAIJson(systemPrompt, userMessage, false, null);

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + config.getApiKey())
                .post(RequestBody.create(json, JSON))
                .build();

        return executeRequest(request);
    }

    /**
     * 调用 OpenAI 带图片
     */
    private String callOpenAIWithImage(String systemPrompt, String userMessage, String base64Image) throws IOException {
        String url = config.getApiUrl().replaceAll("/+$", "") + "/chat/completions";
        String json = buildOpenAIJson(systemPrompt, userMessage, true, base64Image);

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + config.getApiKey())
                .post(RequestBody.create(json, JSON))
                .build();

        return executeRequest(request);
    }

    /**
     * 构建 OpenAI 请求 JSON
     */
    private String buildOpenAIJson(String systemPrompt, String userMessage,
                                   boolean hasImage, String base64Image) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"model\": \"").append(escape(config.getModel())).append("\",\n");
        sb.append("  \"temperature\": ").append(config.getTemperature()).append(",\n");
        sb.append("  \"max_tokens\": ").append(config.getMaxTokens()).append(",\n");
        sb.append("  \"messages\": [\n");
        sb.append("    {\"role\": \"system\", \"content\": \"").append(escape(systemPrompt)).append("\"},\n");
        sb.append("    {\"role\": \"user\", \"content\": [\n");

        if (hasImage && base64Image != null) {
            sb.append("      {\"type\": \"text\", \"text\": \"").append(escape(userMessage)).append("\"},\n");
            sb.append("      {\"type\": \"image_url\", \"image_url\": {\"url\": \"data:image/png;base64,");
            sb.append(base64Image).append("\"}}\n");
        } else {
            sb.append("      {\"type\": \"text\", \"text\": \"").append(escape(userMessage)).append("\"}\n");
        }

        sb.append("    ]}\n");
        sb.append("  ]\n}");
        return sb.toString();
    }

    /**
     * 调用 Ollama API
     */
    private String callOllama(String systemPrompt, String userMessage) throws IOException {
        String url = config.getApiUrl().replaceAll("/+$", "") + "/api/chat";

        String json = "{\n" +
                "  \"model\": \"" + escape(config.getModel()) + "\",\n" +
                "  \"temperature\": " + config.getTemperature() + ",\n" +
                "  \"stream\": false,\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"system\", \"content\": \"" + escape(systemPrompt) + "\"},\n" +
                "    {\"role\": \"user\", \"content\": \"" + escape(userMessage) + "\"}\n" +
                "  ]\n" +
                "}";

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(json, JSON))
                .build();

        return executeOllamaRequest(request);
    }

    /**
     * 调用 Ollama 带图片
     */
    private String callOllamaWithImage(String systemPrompt, String userMessage, String base64Image) throws IOException {
        String url = config.getApiUrl().replaceAll("/+$", "") + "/api/chat";

        String json = "{\n" +
                "  \"model\": \"" + escape(config.getModel()) + "\",\n" +
                "  \"temperature\": " + config.getTemperature() + ",\n" +
                "  \"stream\": false,\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"system\", \"content\": \"" + escape(systemPrompt) + "\"},\n" +
                "    {\"role\": \"user\", \"content\": \"" + escape(userMessage) + "\", \"images\": [\"" + base64Image + "\"]}\n" +
                "  ]\n" +
                "}";

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(json, JSON))
                .build();

        return executeOllamaRequest(request);
    }

    /**
     * 执行 OpenAI 请求
     */
    private String executeRequest(Request request) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "{}";
            if (!response.isSuccessful()) {
                return "[API错误: HTTP " + response.code() + " - " + body + "]";
            }
            // 解析 response
            return parseOpenAIResponse(body);
        }
    }

    /**
     * 执行 Ollama 请求
     */
    private String executeOllamaRequest(Request request) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "{}";
            if (!response.isSuccessful()) {
                return "[API错误: HTTP " + response.code() + " - " + body + "]";
            }
            return parseOllamaResponse(body);
        }
    }

    /**
     * 解析 OpenAI 返回
     */
    private String parseOpenAIResponse(String json) {
        try {
            var obj = JsonUtil.fromJson(json, com.google.gson.JsonObject.class);
            var choices = obj.getAsJsonArray("choices");
            if (choices != null && choices.size() > 0) {
                var message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
                return message.get("content").getAsString();
            }
            return "[无法解析回复]";
        } catch (Exception e) {
            return "[解析错误: " + json.substring(0, Math.min(100, json.length())) + "]";
        }
    }

    /**
     * 解析 Ollama 返回
     */
    private String parseOllamaResponse(String json) {
        try {
            var obj = JsonUtil.fromJson(json, com.google.gson.JsonObject.class);
            var message = obj.getAsJsonObject("message");
            if (message != null) {
                return message.get("content").getAsString();
            }
            return "[无法解析回复]";
        } catch (Exception e) {
            return "[解析错误: " + json.substring(0, Math.min(100, json.length())) + "]";
        }
    }

    /**
     * 总结对话
     */
    public String summarize(String dialogText) throws IOException {
        String prompt = "请用中文简要总结以下对话的核心内容（50字以内）：\n\n" + dialogText;
        return chat("你是一个对话总结助手，请简洁总结。", prompt);
    }

    /**
     * 无 API key 时的模拟回复
     */
    private String generateMockReply(String userMessage) {
        if (userMessage.contains("你好") || userMessage.contains("hello")) {
            return "你好呀～有什么可以帮你的喵？";
        } else if (userMessage.contains("总结") || userMessage.contains("summarize") || userMessage.contains("摘要")) {
            return "这里是刚才的对话摘要喵～（需要配置API key才能使用真正的AI总结哦）";
        } else if (userMessage.contains("?") || userMessage.contains("？")) {
            return "嗯…让我想想喵～（提示：请在 options/api.json 中配置 API key 以启用真正的AI回复）";
        } else {
            return "收到啦～不过我现在是演示模式，配置API key后我就能真正回答你啦喵！（在 options/api.json 中设置）";
        }
    }

    /**
     * JSON 字符串转义
     */
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public ApiConfig getConfig() { return config; }
}
