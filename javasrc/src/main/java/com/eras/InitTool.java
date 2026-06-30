package com.eras;

import com.eras.model.*;
import com.eras.util.JsonUtil;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 工具类 - 初始化配置文件和示例数据（命令行工具）
 * java -cp eras-framework.jar com.eras.InitTool
 */
public class InitTool {

    private static final String BASE_DIR = ".";

    public static void main(String[] args) {
        System.out.println("=== Eras Framework 初始化工具 ===");
        String basePath = args.length > 0 ? args[0] : BASE_DIR;
        initAll(basePath);
        System.out.println("初始化完成！");
    }

    public static void initAll(String basePath) {
        initDirectories(basePath);
        initConfigFiles(basePath);
        System.out.println("✅ 所有配置已就绪");
    }

    private static void initDirectories(String basePath) {
        String[] dirs = {
                "options", "memory", "picture/screen", "picture/character"
        };
        for (String dir : dirs) {
            File f = new File(basePath, dir);
            if (!f.exists()) {
                f.mkdirs();
                System.out.println("📁 创建目录: " + dir);
            }
        }
    }

    private static void initConfigFiles(String basePath) {
        // api.json
        writeIfNotExists(basePath + "/options/api.json",
                "{\n" +
                "  \"provider\": \"openai\",\n" +
                "  \"api_key\": \"\",\n" +
                "  \"api_url\": \"https://api.openai.com/v1\",\n" +
                "  \"model\": \"gpt-4\",\n" +
                "  \"temperature\": 0.8,\n" +
                "  \"max_tokens\": 2048,\n" +
                "  \"description\": \"支持 openai 和 ollama 两种 provider。ollama 示例: {\\\"provider\\\":\\\"ollama\\\",\\\"api_key\\\":\\\"\\\",\\\"api_url\\\":\\\"http://localhost:11434\\\",\\\"model\\\":\\\"qwen2.5\\\"}\"\n" +
                "}"
        );

        // name.json
        writeIfNotExists(basePath + "/options/name.json",
                "{\n" +
                "  \"ai_name\": \"塔菲\",\n" +
                "  \"user_name\": \"主人\"\n" +
                "}"
        );

        // setting.json
        writeIfNotExists(basePath + "/options/setting.json",
                "{\n" +
                "  \"background\": \"\",\n" +
                "  \"character\": \"\",\n" +
                "  \"ai_text_color\": \"#000000\",\n" +
                "  \"user_text_color\": \"#000000\"\n" +
                "}"
        );

        // memory.json
        writeIfNotExists(basePath + "/options/memory.json",
                "{\n" +
                "  \"max_rounds\": 15,\n" +
                "  \"summarize_interval_minutes\": 30,\n" +
                "  \"summarize_on_close\": true,\n" +
                "  \"description\": \"每15轮对话或每30分钟自动总结一次，关闭应用时也总结。手动发送'请你总结刚才我们聊的内容'触发总结。\"\n" +
                "}"
        );

        // nsfw.json
        writeIfNotExists(basePath + "/options/nsfw.json",
                "{\n" +
                "  \"enabled\": false,\n" +
                "  \"description\": \"郊狼连接配置。基于 DG-LAB 开源协议：https://github.com/dungeonlab-open/dglab-bluetooth-protocol\",\n" +
                "  \"device_type\": \"\",\n" +
                "  \"device_id\": \"\",\n" +
                "  \"api_endpoint\": \"\",\n" +
                "  \"api_key\": \"\",\n" +
                "  \"max_duration_minutes\": 120,\n" +
                "  \"safety_limits\": {\n" +
                "    \"max_gear\": 30,\n" +
                "    \"max_duration\": 120,\n" +
                "    \"cooldown_minutes\": 5\n" +
                "  }\n" +
                "}"
        );

        // corememory.json
        writeIfNotExists(basePath + "/memory/corememory.json",
                "{\n" +
                "  \"ai_identity\": \"你是永雏塔菲_bot，一个可爱的AI助手喵~\",\n" +
                "  \"user_info\": \"用户是一个喜欢二次元的玩家\"\n" +
                "}"
        );

        // memory.json (对话摘要)
        writeIfNotExists(basePath + "/memory/memory.json",
                "[]"
        );

        System.out.println("📝 配置文件已创建/检查完成");
    }

    private static void writeIfNotExists(String path, String content) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
                System.out.println("  ✏️  创建: " + file.getName());
            } catch (IOException e) {
                System.err.println("  ❌ 写入失败: " + path + " - " + e.getMessage());
            }
        }
    }
}
