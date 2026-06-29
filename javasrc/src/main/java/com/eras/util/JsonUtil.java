package com.eras.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * JSON 工具类
 */
public class JsonUtil {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    /**
     * 从文件读取 JSON 并解析为对象
     */
    public static <T> T readFromFile(String filePath, Class<T> clazz) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
                return gson.fromJson("{}", clazz);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, clazz);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从文件读取 JSON 并解析为带泛型的类型
     */
    public static <T> T readFromFile(String filePath, Type type) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将对象写入 JSON 文件
     */
    public static void writeToFile(String filePath, Object obj) {
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                gson.toJson(obj, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对象转 JSON 字符串
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * JSON 字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    /**
     * 获取 Gson 实例
     */
    public static Gson getGson() {
        return gson;
    }
}
