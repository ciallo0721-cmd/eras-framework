package com.eras.controller;

import com.eras.model.Message;
import com.eras.service.*;
import com.eras.ui.*;
import com.eras.util.JsonUtil;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 主控制器 - 协调 UI 和服务的核心逻辑
 */
public class MainController {

    private final String basePath;
    private final MainWindow mainWindow;
    private final Stage stage;

    // 服务
    private LLMService llmService;
    private MemoryService memoryService;
    private ImageService imageService;
    private ConnectionService connectionService;

    // 配置
    private String aiName = "助手";
    private String userName = "你";
    private String aiTextColor = "#000000";
    private String userTextColor = "#000000";
    private String currentBgPath = "";
    private String currentCharPath = "";

    // 对话历史
    private final List<Message> conversationHistory = new ArrayList<>();
    private final List<Message> currentSessionMessages = new ArrayList<>();

    // 待发送图片
    private String pendingImageBase64 = null;

    public MainController(String basePath, MainWindow mainWindow, Stage stage) {
        this.basePath = basePath;
        this.mainWindow = mainWindow;
        this.stage = stage;

        initServices();
        loadConfigs();
        setupCallbacks();
        addSystemMessage("Eras 二游框架已启动！在 options/api.json 中配置 API key，或使用演示模式。");
    }

    /**
     * 初始化服务
     */
    private void initServices() {
        String apiConfigPath = basePath + File.separator + "options" + File.separator + "api.json";
        String nsfwConfigPath = basePath + File.separator + "options" + File.separator + "nsfw.json";

        llmService = new LLMService(apiConfigPath);
        memoryService = new MemoryService(basePath);
        imageService = new ImageService(basePath);
        connectionService = new ConnectionService(nsfwConfigPath);

        // 从 options/memory.json 更新记忆配置
        String memOptionPath = basePath + File.separator + "options" + File.separator + "memory.json";
        var memOpt = JsonUtil.readFromFile(memOptionPath, com.google.gson.JsonObject.class);
        if (memOpt != null) {
            int maxRounds = memOpt.get("max_rounds") != null ? memOpt.get("max_rounds").getAsInt() : 15;
            int interval = memOpt.get("summarize_interval_minutes") != null ? memOpt.get("summarize_interval_minutes").getAsInt() : 30;
            boolean summarizeOnClose = memOpt.get("summarize_on_close") == null || memOpt.get("summarize_on_close").getAsBoolean();
            memoryService.updateConfig(maxRounds, interval, summarizeOnClose);
        }
    }

    /**
     * 加载配置
     */
    private void loadConfigs() {
        String namePath = basePath + File.separator + "options" + File.separator + "name.json";
        String settingPath = basePath + File.separator + "options" + File.separator + "setting.json";

        // 名称
        var nameObj = JsonUtil.readFromFile(namePath, com.google.gson.JsonObject.class);
        if (nameObj != null) {
            if (nameObj.get("ai_name") != null) aiName = nameObj.get("ai_name").getAsString();
            if (nameObj.get("user_name") != null) userName = nameObj.get("user_name").getAsString();
        }

        // 设置
        var settingObj = JsonUtil.readFromFile(settingPath, com.google.gson.JsonObject.class);
        if (settingObj != null) {
            if (settingObj.get("background") != null) currentBgPath = settingObj.get("background").getAsString();
            if (settingObj.get("character") != null) currentCharPath = settingObj.get("character").getAsString();
            if (settingObj.get("ai_text_color") != null) aiTextColor = settingObj.get("ai_text_color").getAsString();
            if (settingObj.get("user_text_color") != null) userTextColor = settingObj.get("user_text_color").getAsString();
        }

        // 应用背景和角色
        if (!currentBgPath.isEmpty()) {
            mainWindow.getBackgroundLayer().setBackgroundImage(currentBgPath);
        }
        if (!currentCharPath.isEmpty()) {
            mainWindow.getBackgroundLayer().setCharacterImage(currentCharPath);
        }
    }

    /**
     * 保存名称配置
     */
    private void saveNameConfig() {
        var obj = new java.util.LinkedHashMap<String, String>();
        obj.put("ai_name", aiName);
        obj.put("user_name", userName);
        JsonUtil.writeToFile(basePath + File.separator + "options" + File.separator + "name.json", obj);
    }

    /**
     * 保存显示设置
     */
    private void saveSettingConfig() {
        var obj = new java.util.LinkedHashMap<String, String>();
        obj.put("background", currentBgPath);
        obj.put("character", currentCharPath);
        obj.put("ai_text_color", aiTextColor);
        obj.put("user_text_color", userTextColor);
        JsonUtil.writeToFile(basePath + File.separator + "options" + File.separator + "setting.json", obj);
    }

    /**
     * 设置回调
     */
    private void setupCallbacks() {
        // 选项按钮
        mainWindow.setOnOptionClick(this::showOptions);

        // 发送按钮
        mainWindow.setOnSendClick(this::handleSend);

        // 图片按钮
        mainWindow.setOnImageClick(this::handleImageUpload);
    }

    /**
     * 显示选项对话框
     */
    private void showOptions() {
        OptionsDialog dialog = new OptionsDialog(
                Arrays.asList(imageService.getScreenImages()),
                Arrays.asList(imageService.getCharacterImages())
        );

        dialog.setCurrentBackground(currentBgPath);
        dialog.setCurrentCharacter(currentCharPath);
        dialog.setCurrentAiColor(aiTextColor);
        dialog.setCurrentUserColor(userTextColor);
        dialog.setCurrentAiName(aiName);
        dialog.setCurrentUserName(userName);

        // 角色更换（保存按钮触发）
        dialog.setOnCharacterChange(path -> {
            currentCharPath = path;
            mainWindow.getBackgroundLayer().setCharacterImage(path);
        });

        // AI颜色（保存按钮触发）
        dialog.setOnAiColorChange(color -> {
            aiTextColor = color;
        });

        // 用户颜色（保存按钮触发）
        dialog.setOnUserColorChange(color -> {
            userTextColor = color;
        });

        // 称呼（保存按钮触发）
        dialog.setOnNameChange(names -> {
            aiName = names[0];
            userName = names[1];
        });

        // 保存按钮统一回调
        dialog.setOnSaveAll(() -> {
            saveSettingConfig();
            saveNameConfig();
        });

        // 手动总结
        dialog.setOnSummarize(() -> {
            handleManualSummarize();
        });

        dialog.showAndWait();
    }

    /**
     * 手动总结
     */
    private void handleManualSummarize() {
        if (currentSessionMessages.isEmpty()) {
            addAIMessage("当前还没有足够的对话可以总结喵～");
            return;
        }

        addAIMessage("好的，正在总结刚才的对话喵～");

        new Thread(() -> {
            try {
                String summary = memoryService.triggerSummarize(llmService, currentSessionMessages);
                Platform.runLater(() -> {
                    addAIMessage("📝 对话总结完成：\n" + summary);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    addAIMessage("总结时出了点问题喵… (" + e.getMessage() + ")");
                });
            }
        }).start();
    }

    /**
     * 处理发送消息
     */
    private void handleSend() {
        String text = mainWindow.getInputText();
        if (text.isBlank() && pendingImageBase64 == null) return;

        mainWindow.clearInput();

        boolean hasImage = pendingImageBase64 != null;

        // 创建用户消息
        Message userMsg = new Message(Message.Role.USER, text.isBlank() ? "[图片]" : text, userTextColor);
        userMsg.setHasImage(hasImage);
        addUserMessage(userMsg);

        String imgB64 = pendingImageBase64;
        pendingImageBase64 = null;

        // 显示加载中
        mainWindow.showLoading(true);

        // 异步调用 AI
        new Thread(() -> {
            try {
                String prompt = buildSystemPrompt();
                String reply;

                if (hasImage) {
                    reply = llmService.chatWithImage(prompt, text.isBlank() ? "请描述这张图片" : text, imgB64);
                } else {
                    reply = llmService.chat(prompt, text);
                }

                String finalReply = reply;
                Platform.runLater(() -> {
                    mainWindow.showLoading(false);
                    addAIMessage(finalReply);
                    processConnectionCommand(finalReply);

                    // 检查是否需要总结
                    memoryService.checkAndTriggerSummarize(llmService, currentSessionMessages);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    mainWindow.showLoading(false);
                    addAIMessage("[错误: " + e.getMessage() + "]");
                });
            }
        }).start();
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();

        // 加载 prompt/core.md（角色设定提示词）
        try {
            String promptPath = basePath + File.separator + "prompt" + File.separator + "core.md";
            File promptFile = new File(promptPath);
            if (promptFile.exists() && promptFile.length() > 0) {
                String promptContent = java.nio.file.Files.readString(promptFile.toPath());
                sb.append("【角色设定】\n").append(promptContent.trim()).append("\n\n");
            }
        } catch (Exception ignored) {}

        // 核心记忆
        String core = memoryService.getCoreMemoryPrompt();
        if (!core.isEmpty()) {
            sb.append("【长期记忆】\n").append(core).append("\n\n");
        }

        // 历史摘要
        String summary = memoryService.getSummaryHistory();
        if (!summary.isEmpty()) {
            sb.append(summary).append("\n\n");
        }

        // 最近对话
        if (!conversationHistory.isEmpty()) {
            sb.append("【最近对话】\n");
            int start = Math.max(0, conversationHistory.size() - 30);
            for (int i = start; i < conversationHistory.size(); i++) {
                Message m = conversationHistory.get(i);
                String role = m.getRole() == Message.Role.USER ? userName : aiName;
                sb.append(role).append(": ").append(m.getContent()).append("\n");
            }
        }

        // 角色设定
        sb.append("\n\n你的名字是").append(aiName).append("。请用活泼可爱的语气回复用户（用户叫").append(userName).append("）。");
        sb.append("回复时请保持角色一致性喵～");

        // 郊狼/玩具连接指令（注释掉）
        // sb.append("\n\n【连接指令】需要输出命令时使用格式：[(名称),mode={模式},{挡位},time={分钟}]");

        return sb.toString();
    }

    /**
     * 添加用户消息
     */
    private void addUserMessage(Message msg) {
        conversationHistory.add(msg);
        currentSessionMessages.add(msg);
        mainWindow.addMessage(msg, aiName, userName);
    }

    /**
     * 添加AI消息
     */
    private void addAIMessage(String content) {
        Message msg = new Message(Message.Role.AI, content, aiTextColor);
        conversationHistory.add(msg);
        currentSessionMessages.add(msg);
        mainWindow.addMessage(msg, aiName, userName);

        // 尝试从回复中提取核心记忆
        extractCoreMemory(content);
    }

    /**
     * 添加系统消息
     */
    private void addSystemMessage(String content) {
        Message msg = new Message(Message.Role.SYSTEM, content);
        mainWindow.addMessage(msg, aiName, userName);
    }

    /**
     * 处理图片上传
     */
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择图片");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp", "*.bmp")
        );

        // 默认打开图片目录
        File picDir = new File(basePath + File.separator + "picture");
        if (picDir.exists()) {
            fileChooser.setInitialDirectory(picDir);
        }

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String base64 = Base64.getEncoder().encodeToString(bytes);
                pendingImageBase64 = base64;
                // 在输入框显示提示
                mainWindow.getInputText(); // placeholder
                // 不直接清空输入框，但显示提示
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("图片已选择");
                alert.setHeaderText(null);
                alert.setContentText("已选择图片: " + file.getName() + "\n输入文字后发送即可附带图片。");
                alert.showAndWait();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("图片加载失败");
                alert.setContentText("无法读取图片: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    /**
     * 处理连接命令
     */
    private void processConnectionCommand(String aiReply) {
        // TODO: 等厂商 API 提供后解析格式：[(名称),mode={模式},{挡位},time={分钟}]
        // 示例：connectionService.parseCommand(aiReply);
    }

    /**
     * 尝试从AI回复中提取核心记忆
     */
    private void extractCoreMemory(String content) {
        // 如果AI回复中包含"记住"、"我叫"、"我是"等关键词，提取为长期记忆
        if (content.contains("记住") && content.length() < 200) {
            memoryService.updateCoreMemory("note_" + System.currentTimeMillis(), content);
        }
    }

    /**
     * 关闭应用
     */
    public void onClose() {
        memoryService.onClose(llmService, currentSessionMessages);
    }
}
