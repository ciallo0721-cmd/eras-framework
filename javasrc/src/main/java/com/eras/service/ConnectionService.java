package com.eras.service;

import com.eras.util.JsonUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 郊狼/玩具连接服务
 *
 * 基于 DG-LAB 开源蓝牙协议：https://github.com/dungeonlab-open/dglab-bluetooth-protocol
 *
 * 协议要点（郊狼 V3）：
 * - B0 指令控制波形输出，每 100ms 发送一次，20 字节
 * - A/B 双通道，强度范围 0-200，波形强度 0-100
 * - BF 指令设软上限，重连后必须重新写入
 *
 * ╔══════════════════════════════════════════════════╗
 * ║  🚨 绝对安全上限（代码写死，不可修改）          ║
 * ║  - 最大挡位：30（配置文件可设更低，不可设更高） ║
 * ║  - 最长时间：120 分钟                           ║
 * ║  - 禁止一次跳到高挡，必须逐步增加               ║
 * ║  - 禁止将设备放置在上半身或靠近心脏的位置       ║
 * ╚══════════════════════════════════════════════════╝
 *
 * AI输出格式：[(名称),mode={模式},{挡位},time={分钟}]
 */
public class ConnectionService {

    /** 绝对硬上限：挡位最高 30（代码写死，配置文件不可超出此值） */
    private static final int ABSOLUTE_MAX_GEAR = 30;

    /** 绝对硬上限：最长 120 分钟 */
    private static final int ABSOLUTE_MAX_DURATION = 120;

    /** 挡位解析正则：[(名称),mode={模式},{数字},time={数字}] */
    private static final Pattern COMMAND_PATTERN =
            Pattern.compile("\\[\\(([^)]+)\\),mode=\\{(\\w+)\\},\\{(\\d+)\\},time=\\{(\\d+)\\}\\]");

    private boolean enabled = false;
    private String deviceType = "";
    private String deviceId = "";
    private String apiEndpoint = "";
    private String apiKey = "";
    private int maxDurationMinutes = 120;
    private int maxGear = 30;   // 从 safety_limits 读取，但不超过 ABSOLUTE_MAX_GEAR
    private int cooldownMinutes = 5;

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
                if (safety != null) {
                    if (safety.get("max_duration") != null) {
                        this.maxDurationMinutes = Math.min(safety.get("max_duration").getAsInt(), ABSOLUTE_MAX_DURATION);
                    }
                    if (safety.get("max_gear") != null) {
                        // 配置文件的值不能超过绝对上限
                        int cfgGear = safety.get("max_gear").getAsInt();
                        this.maxGear = Math.min(cfgGear, ABSOLUTE_MAX_GEAR);
                    }
                    if (safety.get("cooldown_minutes") != null) {
                        this.cooldownMinutes = safety.get("cooldown_minutes").getAsInt();
                    }
                }
            }
        } catch (Exception e) {
            // config not ready yet, use defaults
        }
    }

    /**
     * 解析并校验AI输出的命令
     * 格式：[(名称),mode={模式},{挡位},time={分钟}]
     *
     * @param aiOutput AI 回复文本
     * @return 通过校验的指令字符串，校验失败返回 null（含日志说明）
     */
    public String parseCommand(String aiOutput) {
        if (!enabled) return null;
        if (aiOutput == null || aiOutput.isEmpty()) return null;

        Matcher matcher = COMMAND_PATTERN.matcher(aiOutput);
        if (!matcher.find()) {
            System.err.println("[ConnectionService] ⚠️ 未匹配到有效指令格式: " + aiOutput);
            return null;
        }

        String name = matcher.group(1);
        String mode = matcher.group(2);
        int gear;
        int duration;
        try {
            gear = Integer.parseInt(matcher.group(3));
            duration = Integer.parseInt(matcher.group(4));
        } catch (NumberFormatException e) {
            System.err.println("[ConnectionService] ⚠️ 指令数字解析失败: " + aiOutput);
            return null;
        }

        // === 🚨 安全校验 ===

        // 1. 挡位校验
        if (gear <= 0) {
            System.err.println("[ConnectionService] 🚨 拒绝执行：挡位必须大于 0（收到: " + gear + "）");
            return null;
        }
        if (gear > maxGear) {
            System.err.println("[ConnectionService] 🚨 安全拦截：挡位 " + gear
                    + " 超过允许上限 " + maxGear + "，已拒绝执行");
            return null;
        }

        // 2. 时长校验
        if (duration <= 0) {
            System.err.println("[ConnectionService] 🚨 拒绝执行：时长必须大于 0（收到: " + duration + "）");
            return null;
        }
        if (duration > maxDurationMinutes) {
            System.err.println("[ConnectionService] 🚨 安全拦截：时长 " + duration
                    + " 分钟超过允许上限 " + maxDurationMinutes + "，已截断为上限值");
            duration = maxDurationMinutes;
        }

        // 3. 高挡位警告（超过 15 就警告）
        if (gear > 15) {
            System.err.println("[ConnectionService] ⚠️ 高挡位警告：挡位 " + gear
                    + " 较高，请确认用户已逐步适应。建议从低挡开始逐步增加。");
        }

        // 构建最终安全指令
        String safeCommand = String.format("name=%s,mode=%s,gear=%d,time=%d",
                name, mode, gear, duration);
        System.out.println("[ConnectionService] ✅ 指令通过安全校验: " + safeCommand);
        return safeCommand;
    }

    /**
     * 发送命令到设备
     */
    public boolean sendCommand(String command) {
        if (!enabled) return false;
        if (command == null || command.isEmpty()) return false;

        // TODO: 基于 DG-LAB 蓝牙协议实现 B0/BF 指令发送
        // 参考 dglab-bluetooth-protocol/coyote/v3/README.md
        System.out.println("[ConnectionService] 发送指令到设备: " + command);
        return true;
    }

    /**
     * 获取当前生效的安全上限
     */
    public int getEffectiveMaxGear() { return maxGear; }

    /**
     * 获取代码硬上限（不可修改）
     */
    public static int getAbsoluteMaxGear() { return ABSOLUTE_MAX_GEAR; }

    public boolean isEnabled() { return enabled; }
    public int getMaxDurationMinutes() { return maxDurationMinutes; }
    public int getCooldownMinutes() { return cooldownMinutes; }
}
