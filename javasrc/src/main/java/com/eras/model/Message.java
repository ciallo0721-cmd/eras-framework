package com.eras.model;

/**
 * 聊天消息模型
 */
public class Message {
    public enum Role {
        USER, AI, SYSTEM
    }

    private Role role;
    private String content;
    private String color;     // AI文本颜色，默认 #000000
    private long timestamp;
    private boolean hasImage;  // 是否附带图片

    public Message() {}

    public Message(Role role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.color = "#000000";
        this.hasImage = false;
    }

    public Message(Role role, String content, String color) {
        this.role = role;
        this.content = content;
        this.color = color;
        this.timestamp = System.currentTimeMillis();
        this.hasImage = false;
    }

    // Getters & Setters
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isHasImage() { return hasImage; }
    public void setHasImage(boolean hasImage) { this.hasImage = hasImage; }
}
