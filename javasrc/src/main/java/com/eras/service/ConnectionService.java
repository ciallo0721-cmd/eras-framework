package com.eras.service;

import com.eras.util.JsonUtil;

/**
 * 郊狼/玩具连接服务（已注释，等待厂商API）
 *
 * 当厂商提供API后：
 * 1. 在 options/nsfw.json 中配置 device_type, device_id, api_endpoint, api_key
 * 2. 设置 enabled = true
 * 3. 从此处实现连接逻辑
 *
 * AI输出格式：[(名称,如郊狼/....),mode={模式},{挡位},time={10/60/120(分钟)(最长120)]
 */
public class ConnectionService {

    private boolean enabled = false;
    private String deviceType = "";
    private String deviceId = "";
    private String apiEndpoint = "";
    private String apiKey = "";
    private int maxDurationMinutes = 120;

    public ConnectionService(String configPath) {
        loadConfig(configPath);
    }

    private void loadConfig(String configPath) {
        try {
            var obj = JsonUtil.readFromFile(configPath, com.google.gson.JsonObject.class);
            if (obj != null) {
                this.enabled = obj.get("enabled") != null && obj.get("enabled").getAsBoolean();
                this.deviceType = obj.get("device_type") != null ? obj.get("device_type").getAsString() : "";
                this.deviceId = obj.get("device_id") != null ? obj.get("device_id").getAsString() : "";
                this.apiEndpoint = obj.get("api_endpoint") != null ? obj.get("api_endpoint").getAsString() : "";
                this.apiKey = obj.get("api_key") != null ? obj.get("api_key").getAsString() : "";
                var safety = obj.getAsJsonObject("safety_limits");
                if (safety != null && safety.get("max_duration") != null) {
                    this.maxDurationMinutes = safety.get("max_duration").getAsInt();
                }
            }
        } catch (Exception e) {
            // config not ready yet
        }
    }

    /**
     * 解析AI输出的命令
     * 格式：[(名称),mode={模式},{挡位},time={分钟}]
     */
    public String parseCommand(String aiOutput) {
        if (!enabled) return null;
        // TODO: 等厂商API提供后实现
        return null;
    }

    /**
     * 发送命令到设备
     */
    public boolean sendCommand(String command) {
        if (!enabled) return false;
        // TODO: 等厂商API提供后实现
        return false;
    }

    public boolean isEnabled() { return enabled; }
}
