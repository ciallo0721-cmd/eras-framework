package com.eras.service;

import com.eras.model.Message;
import com.eras.util.JsonUtil;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 记忆服务
 * - corememory.json: AI长期记忆（关键事实）
 * - memory.json: 对话摘要（每15轮/30分钟/关闭时总结）
 */
public class MemoryService {
    private final String basePath;
    private final String coreMemoryPath;
    private final String memorySummaryPath;

    // 长期记忆：key-value 形式
    private final Map<String, String> coreMemory = new ConcurrentHashMap<>();

    // 摘要列表
    private final List<SummaryRecord> summaries = new CopyOnWriteArrayList<>();

    // 对话轮次计数
    private int conversationRound = 0;

    // 最后总结时间
    private long lastSummarizeTime = System.currentTimeMillis();

    // 配置
    private int maxRounds = 15;
    private int summarizeIntervalMinutes = 30;
    private boolean summarizeOnClose = true;

    // 定时器
    private ScheduledExecutorService scheduler;

    public MemoryService(String basePath) {
        this.basePath = basePath;
        this.coreMemoryPath = basePath + File.separator + "memory" + File.separator + "corememory.json";
        this.memorySummaryPath = basePath + File.separator + "memory" + File.separator + "memory.json";
        loadCoreMemory();
        loadSummaries();
    }

    /**
     * 加载核心记忆
     */
    private void loadCoreMemory() {
        Path path = Paths.get(coreMemoryPath);
        if (Files.exists(path)) {
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> loaded = JsonUtil.readFromFile(coreMemoryPath, type);
            if (loaded != null) {
                coreMemory.putAll(loaded);
            }
        } else {
            // 初始化默认核心记忆
            coreMemory.put("ai_identity", "你是永雏塔菲_bot，一个可爱的AI助手喵~");
            coreMemory.put("user_info", "用户是一个喜欢二次元的玩家");
            saveCoreMemory();
        }
    }

    /**
     * 保存核心记忆
     */
    public void saveCoreMemory() {
        JsonUtil.writeToFile(coreMemoryPath, coreMemory);
    }

    /**
     * 更新核心记忆条目
     */
    public void updateCoreMemory(String key, String value) {
        coreMemory.put(key, value);
        saveCoreMemory();
    }

    /**
     * 获取核心记忆文本（用于LLM提示）
     */
    public String getCoreMemoryPrompt() {
        if (coreMemory.isEmpty()) return "";
        return coreMemory.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("\n"));
    }

    /**
     * 加载摘要历史
     */
    private void loadSummaries() {
        Path path = Paths.get(memorySummaryPath);
        if (Files.exists(path)) {
            Type type = new TypeToken<List<SummaryRecord>>(){}.getType();
            List<SummaryRecord> loaded = JsonUtil.readFromFile(memorySummaryPath, type);
            if (loaded != null) {
                summaries.addAll(loaded);
            }
        }
    }

    /**
     * 保存摘要
     */
    private void saveSummaries() {
        JsonUtil.writeToFile(memorySummaryPath, summaries);
    }

    /**
     * 从配置更新参数
     */
    public void updateConfig(int maxRounds, int intervalMinutes, boolean summarizeOnClose) {
        this.maxRounds = maxRounds;
        this.summarizeIntervalMinutes = intervalMinutes;
        this.summarizeOnClose = summarizeOnClose;
    }

    /**
     * 增加对话轮次，检查是否需要触发总结
     */
    public boolean checkAndTriggerSummarize(LLMService llmService, List<Message> recentMessages) {
        conversationRound++;

        boolean shouldSummarize = false;

        // 达到最大轮次
        if (conversationRound >= maxRounds) {
            shouldSummarize = true;
        }

        // 达到时间间隔
        long elapsed = System.currentTimeMillis() - lastSummarizeTime;
        if (elapsed >= summarizeIntervalMinutes * 60 * 1000L) {
            shouldSummarize = true;
        }

        if (shouldSummarize) {
            triggerSummarize(llmService, recentMessages);
            return true;
        }
        return false;
    }

    /**
     * 手动触发总结
     */
    public String triggerSummarize(LLMService llmService, List<Message> recentMessages) {
        // 获取需要总结的对话文本
        String dialogText = recentMessages.stream()
                .map(m -> (m.getRole() == Message.Role.USER ? "用户" : "AI") + ": " + m.getContent())
                .collect(Collectors.joining("\n"));

        if (dialogText.isBlank()) {
            return "没有需要总结的对话内容";
        }

        // 调用LLM总结
        String summary = null;
        if (llmService != null) {
            try {
                summary = llmService.summarize(dialogText);
            } catch (Exception e) {
                summary = "[AI总结失败: " + e.getMessage() + "]";
            }
        } else {
            summary = "[对话记录: 共" + recentMessages.size() + "条消息]";
        }

        // 记录摘要
        SummaryRecord record = new SummaryRecord(
                summary,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                Math.max(0, conversationRound - maxRounds),
                conversationRound
        );
        summaries.add(record);

        // 如果摘要太多，只保留最近20条
        if (summaries.size() > 20) {
            int excess = summaries.size() - 20;
            for (int i = 0; i < excess; i++) {
                summaries.remove(0);
            }
        }

        saveSummaries();
        conversationRound = 0;
        lastSummarizeTime = System.currentTimeMillis();

        return summary;
    }

    /**
     * 关闭时总结
     */
    public void onClose(LLMService llmService, List<Message> recentMessages) {
        if (summarizeOnClose && !recentMessages.isEmpty()) {
            triggerSummarize(llmService, recentMessages);
        }
        saveCoreMemory();
    }

    /**
     * 获取历史摘要文本
     */
    public String getSummaryHistory() {
        if (summaries.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("=== 历史对话摘要 ===\n");
        for (int i = summaries.size() - 1; i >= Math.max(0, summaries.size() - 5); i--) {
            SummaryRecord r = summaries.get(i);
            sb.append("[").append(r.time).append("] ").append(r.summary).append("\n");
        }
        return sb.toString();
    }

    /**
     * 重置对话轮次
     */
    public void resetRound() {
        conversationRound = 0;
    }

    public int getConversationRound() { return conversationRound; }

    /**
     * 摘要记录
     */
    static class SummaryRecord {
        String summary;
        String time;
        int roundStart;
        int roundEnd;

        SummaryRecord() {}

        SummaryRecord(String summary, String time, int roundStart, int roundEnd) {
            this.summary = summary;
            this.time = time;
            this.roundStart = roundStart;
            this.roundEnd = roundEnd;
        }
    }
}
