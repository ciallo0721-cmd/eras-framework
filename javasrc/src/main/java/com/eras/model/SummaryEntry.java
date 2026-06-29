package com.eras.model;

/**
 * 临时记忆 - 存储对话摘要
 */
public class SummaryEntry {
    private String summary;
    private long timestamp;
    private int roundStart;
    private int roundEnd;

    public SummaryEntry() {}

    public SummaryEntry(String summary, int roundStart, int roundEnd) {
        this.summary = summary;
        this.timestamp = System.currentTimeMillis();
        this.roundStart = roundStart;
        this.roundEnd = roundEnd;
    }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public int getRoundStart() { return roundStart; }
    public void setRoundStart(int roundStart) { this.roundStart = roundStart; }
    public int getRoundEnd() { return roundEnd; }
    public void setRoundEnd(int roundEnd) { this.roundEnd = roundEnd; }
}
