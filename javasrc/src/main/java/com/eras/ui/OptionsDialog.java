package com.eras.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * 选项对话框 - 左上角菜单
 * 可以更换背景/人物/颜色/称呼
 */
public class OptionsDialog extends Dialog<Void> {

    private final VBox content;
    private final List<String> screenImages;
    private final List<String> characterImages;

    // 回调
    private Consumer<String> onCharacterChange;
    private Consumer<String> onAiColorChange;
    private Consumer<String> onUserColorChange;
    private Consumer<String[]> onNameChange;
    private Runnable onConnectionConfig;
    private Runnable onSummarize;
    private Runnable onSaveAll;

    private String currentBackground;
    private String currentCharacter;
    private String currentAiColor;
    private String currentUserColor;
    private String currentAiName;
    private String currentUserName;

    public OptionsDialog(List<String> screenImages, List<String> characterImages) {
        this.screenImages = screenImages;
        this.characterImages = characterImages;

        setTitle("选项");
        setHeaderText("二游框架设置");

        content = new VBox(10);
        content.setPadding(new Insets(15));
        content.setPrefWidth(400);

        buildUI();

        DialogPane pane = getDialogPane();
        pane.setContent(content);
        pane.getButtonTypes().add(ButtonType.CLOSE);

        Window window = getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(e -> window.hide());
    }

    private void buildUI() {
        // 角色选择
        addSectionLabel("角色背景");
        ComboBox<String> charCombo = new ComboBox<>();
        charCombo.getItems().add("无");
        for (String s : characterImages) {
            charCombo.getItems().add(new File(s).getName());
        }
        charCombo.setValue(currentCharacter != null ? new File(currentCharacter).getName() : "无");
        content.getChildren().add(charCombo);

        // AI颜色
        addSectionLabel("AI文字颜色");
        TextField aiColorField = new TextField(currentAiColor != null ? currentAiColor : "#FFFFFF");
        aiColorField.setPromptText("#FFFFFF");
        Label aiPreview = new Label("预览文字");
        aiPreview.setStyle("-fx-text-fill: " + (currentAiColor != null ? currentAiColor : "#FFFFFF") + ";");
        aiColorField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                Color.web(newVal);
                aiPreview.setStyle("-fx-text-fill: " + newVal + ";");
            } catch (Exception ignored) {}
        });
        HBox aiColorRow = new HBox(10, aiColorField, aiPreview);
        content.getChildren().add(aiColorRow);

        // 用户颜色
        addSectionLabel("用户文字颜色");
        TextField userColorField = new TextField(currentUserColor != null ? currentUserColor : "#FFFFFF");
        userColorField.setPromptText("#FFFFFF");
        Label userPreview = new Label("预览文字");
        userPreview.setStyle("-fx-text-fill: " + (currentUserColor != null ? currentUserColor : "#FFFFFF") + ";");
        userColorField.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                Color.web(newVal);
                userPreview.setStyle("-fx-text-fill: " + newVal + ";");
            } catch (Exception ignored) {}
        });
        HBox userColorRow = new HBox(10, userColorField, userPreview);
        content.getChildren().add(userColorRow);

        // 称呼
        addSectionLabel("称呼设置");
        TextField aiNameField = new TextField(currentAiName != null ? currentAiName : "助手");
        aiNameField.setPromptText("AI名称");
        TextField userNameField = new TextField(currentUserName != null ? currentUserName : "你");
        userNameField.setPromptText("用户名称");
        HBox nameRow = new HBox(10, aiNameField, userNameField);
        content.getChildren().add(nameRow);

        // 分割线
        Separator sep = new Separator();
        sep.setPadding(new Insets(10, 0, 10, 0));
        content.getChildren().add(sep);

        // --- 保存按钮 ---
        Label saveStatus = new Label();
        saveStatus.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 13px;");

        Button saveBtn = new Button("保存设置");
        saveBtn.setStyle(
            "-fx-background-color: #4CAF50;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 30;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        saveBtn.setMaxWidth(200);
        saveBtn.setOnAction(e -> {
            // 验证颜色（为空则跳过）
            String aiColor = aiColorField.getText().trim();
            String userColor = userColorField.getText().trim();
            if (!aiColor.isEmpty()) {
                try { Color.web(aiColor); } catch (Exception ex) {
                    saveStatus.setStyle("-fx-text-fill: #f44336; -fx-font-size: 13px;");
                    saveStatus.setText("AI颜色格式错误");
                    return;
                }
            }
            if (!userColor.isEmpty()) {
                try { Color.web(userColor); } catch (Exception ex) {
                    saveStatus.setStyle("-fx-text-fill: #f44336; -fx-font-size: 13px;");
                    saveStatus.setText("用户颜色格式错误");
                    return;
                }
            }

            // 执行保存
            if (onCharacterChange != null) {
                String selected = charCombo.getValue();
                if ("无".equals(selected) || selected == null) {
                    onCharacterChange.accept("");
                } else {
                    for (String s : characterImages) {
                        if (new File(s).getName().equals(selected)) {
                            onCharacterChange.accept(s);
                            break;
                        }
                    }
                }
            }
            if (onAiColorChange != null) onAiColorChange.accept(aiColorField.getText());
            if (onUserColorChange != null) onUserColorChange.accept(userColorField.getText());
            if (onNameChange != null) {
                onNameChange.accept(new String[]{aiNameField.getText(), userNameField.getText()});
            }
            if (onSaveAll != null) onSaveAll.run();

            // 显示成功
            saveStatus.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 13px; -fx-font-weight: bold;");
            saveStatus.setText("✓ 已保存");
        });

        HBox saveRow = new HBox(10, saveBtn, saveStatus);
        saveRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        content.getChildren().add(saveRow);

        // 临时记忆总结
        addSectionLabel("临时记忆");
        Button summarizeBtn = new Button("总结最近对话");
        summarizeBtn.setStyle("-fx-background-color: #4A90D9; -fx-text-fill: white; -fx-cursor: hand;");
        summarizeBtn.setOnAction(e -> {
            if (onSummarize != null) onSummarize.run();
        });
        content.getChildren().add(summarizeBtn);

        // 帮助
        addSectionLabel("关于");
        Label helpLabel = new Label("Eras Framework v1.0.0\nAI 二游框架\n\n" +
                "· 在 options/api.json 配置 API key\n" +
                "· 图片放在 picture/ 目录下");
        helpLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
        helpLabel.setWrapText(true);
        content.getChildren().add(helpLabel);

        // 滚动
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        getDialogPane().setContent(scrollPane);
    }

    private void addSectionLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 0 2 0; -fx-font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;");
        content.getChildren().add(label);
    }

    // --- Setters for initial values ---

    public void setCurrentBackground(String path) { this.currentBackground = path; }
    public void setCurrentCharacter(String path) { this.currentCharacter = path; }
    public void setCurrentAiColor(String color) { this.currentAiColor = color; }
    public void setCurrentUserColor(String color) { this.currentUserColor = color; }
    public void setCurrentAiName(String name) { this.currentAiName = name; }
    public void setCurrentUserName(String name) { this.currentUserName = name; }

    // --- Callbacks ---

    public void setOnCharacterChange(Consumer<String> cb) { this.onCharacterChange = cb; }
    public void setOnAiColorChange(Consumer<String> cb) { this.onAiColorChange = cb; }
    public void setOnUserColorChange(Consumer<String> cb) { this.onUserColorChange = cb; }
    public void setOnNameChange(Consumer<String[]> cb) { this.onNameChange = cb; }
    public void setOnConnectionConfig(Runnable cb) { this.onConnectionConfig = cb; }
    public void setOnSummarize(Runnable cb) { this.onSummarize = cb; }
    public void setOnSaveAll(Runnable cb) { this.onSaveAll = cb; }
}
