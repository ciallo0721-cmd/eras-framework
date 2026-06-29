package com.eras.model;

/**
 * 设置配置模型 - 对应 options/setting.json
 */
public class SettingConfig {
    private String background = "";
    private String character = "";
    private String character_type = "auto"; // auto / gif / live2d / static
    private String ai_text_color = "#FFFFFF";
    private String user_text_color = "#FFFFFF";

    public String getBackground() { return background; }
    public void setBackground(String background) { this.background = background; }
    public String getCharacter() { return character; }
    public void setCharacter(String character) { this.character = character; }
    public String getCharacterType() { return character_type; }
    public void setCharacterType(String characterType) { this.character_type = characterType; }
    public String getAiTextColor() { return ai_text_color; }
    public void setAiTextColor(String aiTextColor) { this.ai_text_color = aiTextColor; }
    public String getUserTextColor() { return user_text_color; }
    public void setUserTextColor(String userTextColor) { this.user_text_color = userTextColor; }
}
