package com.eras;

import com.eras.controller.MainController;
import com.eras.ui.MainWindow;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;

/**
 * AI 智能体框架-Eras
 * 主入口
 */
public class ErasApp extends Application {

    private MainController controller;

    @Override
    public void start(Stage primaryStage) {
        // 获取项目根目录（与 options/ 同级）
        String basePath = System.getProperty("eras.basepath");
        if (basePath == null || basePath.isEmpty()) {
            // 默认：从运行目录寻找（JAR所在目录 / 项目根目录）
            basePath = findBasePath();
        }

        // 创建主窗口
        MainWindow mainWindow = new MainWindow();
        Scene scene = new Scene(mainWindow, 800, 600);

        // 加载 CSS 样式（优先从 JAR 内加载，开发环境从文件系统）
        try {
            InputStream cssStream = getClass().getResourceAsStream("/styles.css");
            if (cssStream != null) {
                String cssContent = new String(cssStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                scene.getStylesheets().clear();
                // 写临时文件加载
                java.nio.file.Path tmpCss = java.nio.file.Files.createTempFile("eras-", ".css");
                java.nio.file.Files.writeString(tmpCss, cssContent);
                scene.getStylesheets().add(tmpCss.toUri().toString());
                tmpCss.toFile().deleteOnExit();
            }
        } catch (Exception e) {
            // fallback: 从文件系统加载
            String cssPath = basePath + File.separator + "java源码" + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "styles.css";
            File cssFile = new File(cssPath);
            if (cssFile.exists()) {
                scene.getStylesheets().add(cssFile.toURI().toString());
            }
        }

        // 配置舞台
        primaryStage.setTitle("AI 智能体框架-Eras");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(500);

        // 设置应用图标
        try {
            InputStream iconStream = getClass().getResourceAsStream("/app-icon.png");
            if (iconStream != null) {
                primaryStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception ignored) {}

        // 创建控制器
        controller = new MainController(basePath, mainWindow, primaryStage);

        // 关闭事件
        primaryStage.setOnCloseRequest(e -> {
            if (controller != null) {
                controller.onClose();
            }
            javafx.application.Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
    }

    /**
     * 查找项目根目录
     */
    private String findBasePath() {
        // 1. 尝试 JAR 所在目录
        try {
            String jarPath = ErasApp.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
            File jarFile = new File(jarPath);
            if (jarFile.isFile()) {
                return jarFile.getParentFile().getAbsolutePath();
            }
        } catch (Exception ignored) {}

        // 2. 尝试当前工作目录
        String userDir = System.getProperty("user.dir");
        if (userDir != null) {
            File optDir = new File(userDir, "options");
            if (optDir.exists()) {
                return userDir;
            }
        }

        // 3. 默认返回当前目录
        return ".";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
