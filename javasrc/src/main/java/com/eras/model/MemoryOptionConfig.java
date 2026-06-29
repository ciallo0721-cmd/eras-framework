package com.eras.model;

/**
 * 记忆配置模型 - 对应 options/memory.json
 */
public class MemoryOptionConfig {
    private int max_rounds = 15;
    private int summarize_interval_minutes = 30;
    private boolean summarize_on_close = true;
    private String description = "每15轮对话或每30分钟自动总结一次，关闭应用时也总结";

    public int getMaxRounds() { return max_rounds; }
    public void setMaxRounds(int maxRounds) { this.max_rounds = maxRounds; }
    public int getSummarizeIntervalMinutes() { return summarize_interval_minutes; }
    public void setSummarizeIntervalMinutes(int minutes) { this.summarize_interval_minutes = minutes; }
    public boolean isSummarizeOnClose() { return summarize_on_close; }
    public void setSummarizeOnClose(boolean summarizeOnClose) { this.summarize_on_close = summarizeOnClose; }
    public String getDescription() { return description; }
}
