package com.eras.service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Base64;

/**
 * 图片服务 - 识图功能
 */
public class ImageService {

    private final String screenPath;
    private final String characterPath;

    public ImageService(String basePath) {
        this.screenPath = basePath + File.separator + "picture" + File.separator + "screen";
        this.characterPath = basePath + File.separator + "picture" + File.separator + "character";
        ensureDir(screenPath);
        ensureDir(characterPath);
    }

    /**
     * 将图片文件转为 Base64
     */
    public String imageToBase64(String imagePath) throws IOException {
        Path path = Paths.get(imagePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("图片不存在: " + imagePath);
        }
        byte[] bytes = Files.readAllBytes(path);
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 获取所有背景图片列表
     */
    public String[] getScreenImages() {
        return listImages(screenPath);
    }

    /**
     * 获取所有角色图片列表
     */
    public String[] getCharacterImages() {
        return listImages(characterPath);
    }

    /**
     * 列出目录中的图片文件
     */
    private String[] listImages(String dirPath) {
        Path dir = Paths.get(dirPath);
        if (!Files.exists(dir)) return new String[0];
        try (var stream = Files.list(dir)) {
            return stream
                    .filter(p -> {
                        String name = p.toString().toLowerCase();
                        return name.endsWith(".png") || name.endsWith(".jpg")
                                || name.endsWith(".jpeg") || name.endsWith(".gif")
                                || name.endsWith(".webp") || name.endsWith(".bmp");
                    })
                    .map(Path::toString)
                    .toArray(String[]::new);
        } catch (IOException e) {
            return new String[0];
        }
    }

    /**
     * 获取图片的显示名称（不含路径）
     */
    public static String getDisplayName(String fullPath) {
        if (fullPath == null || fullPath.isEmpty()) return "无";
        Path p = Paths.get(fullPath);
        return p.getFileName().toString();
    }

    /**
     * 确保目录存在
     */
    private void ensureDir(String path) {
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException ignored) {}
    }

    /**
     * 检查图片是否有效
     */
    public boolean isValidImage(String path) {
        if (path == null || path.isEmpty()) return false;
        try {
            BufferedImage img = ImageIO.read(new File(path));
            return img != null;
        } catch (Exception e) {
            return false;
        }
    }

    public String getScreenPath() { return screenPath; }
    public String getCharacterPath() { return characterPath; }
}
