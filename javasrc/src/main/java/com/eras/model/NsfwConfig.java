package com.eras.model;

/**
 * 郊狼连接配置模型 - 对应 options/nsfw.json
 */
public class NsfwConfig {
    private boolean enabled = false;
    private String device_type = "";
    private String device_id = "";
    private String api_endpoint = "";
    private String api_key = "";
    private int max_duration_minutes = 120;
    private SafetyLimits safety_limits = new SafetyLimits();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getDeviceType() { return device_type; }
    public void setDeviceType(String deviceType) { this.device_type = deviceType; }
    public String getDeviceId() { return device_id; }
    public void setDeviceId(String deviceId) { this.device_id = deviceId; }
    public String getApiEndpoint() { return api_endpoint; }
    public void setApiEndpoint(String apiEndpoint) { this.api_endpoint = apiEndpoint; }
    public String getApiKey() { return api_key; }
    public void setApiKey(String apiKey) { this.api_key = apiKey; }
    public int getMaxDurationMinutes() { return max_duration_minutes; }
    public SafetyLimits getSafetyLimits() { return safety_limits; }

    public static class SafetyLimits {
        private int max_gear = 30;
        private int max_duration = 120;
        private int cooldown_minutes = 5;

        public int getMaxGear() { return max_gear; }
        public int getMaxDuration() { return max_duration; }
        public int getCooldownMinutes() { return cooldown_minutes; }
    }
}
