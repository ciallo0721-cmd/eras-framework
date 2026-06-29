package com.eras.model;

/**
 * 名称配置模型 - 对应 options/name.json
 */
public class NameConfig {
    private String ai_name = "助手";
    private String user_name = "你";

    public String getAiName() { return ai_name; }
    public void setAiName(String aiName) { this.ai_name = aiName; }
    public String getUserName() { return user_name; }
    public void setUserName(String userName) { this.user_name = userName; }
}
