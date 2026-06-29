package com.eras.model;

/**
 * 核心记忆条目 - AI需要知道的长期事实
 */
public class CoreMemoryEntry {
    private String key;
    private String value;
    private long timestamp;

    public CoreMemoryEntry() {}
    public CoreMemoryEntry(String key, String value) {
        this.key = key;
        this.value = value;
        this.timestamp = System.currentTimeMillis();
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

