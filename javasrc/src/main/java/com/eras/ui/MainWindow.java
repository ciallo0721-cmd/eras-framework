package com.eras.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * 主窗口 UI 布局
 *
 * 布局层次（从下到上）：
 * 1. BackgroundLayer - 背景 + 角色立绘
 * 2. ChatArea - 对话列表（从 2/10 到 8/10）
 * 3. TitleBar - 顶部标题栏（左上角选项按钮）
 * 4. InputBar - 底部输入区域（8/10 位置）
 */
public class MainWindow extends StackPane {

    // === 布局常量 ===
    private static final double CHAT_AREA_TOP_RATIO = 0.18;      // 约 2/10
    private static final double INPUT_BAR_TOP_RATIO = 0.82;       // 约 8/10
    private static final double INPUT_BAR_HEIGHT_RATIO = 0.10;    // 输入区域高度

    // === UI 组件 ===
    private BackgroundLayer backgroundLayer;
    private ScrollPane chatScrollPane;
    private VBox chatContainer;
    private TextArea inputField;
    private Button sendButton;
    private Button imageButton;
    private Button optionButton;

    // === 回调 ===
    private Runnable onOptionClick;
    private Runnable onSendClick;
    private Runnable onImageClick;

    public MainWindow() {
        // 整体背景色（无背景图时显示）
        setStyle("-fx-background-color: #1a1a2e; -fx-font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;");

        // === 第一层：背景 + 角色 ===
        backgroundLayer = new BackgroundLayer();
        backgroundLayer.prefWidthProperty().bind(widthProperty());
        backgroundLayer.prefHeightProperty().bind(heightProperty());

        // === 第二层：极淡遮罩（保持背景可见） ===
        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.08);");
        overlay.prefWidthProperty().bind(widthProperty());
        overlay.prefHeightProperty().bind(heightProperty());

        // === 第三层：聊天区域 (2/10 到 8/10) ===
        chatContainer = new VBox(8);
        chatContainer.setFillWidth(true);
        chatContainer.setStyle("-fx-background-color: transparent;");

        chatScrollPane = new ScrollPane(chatContainer);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setStyle(
                "-fx-background: transparent;" +
                "-fx-background-color: transparent;" +
                "-fx-padding: 10 5 10 5;"
        );
        chatScrollPane.getStyleClass().add("chat-scroll-pane");
        chatScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        chatScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // 自动滚动到底部
        chatContainer.heightProperty().addListener((obs, old, newVal) -> {
            if (newVal.doubleValue() > 0) {
                chatScrollPane.setVvalue(1.0);
            }
        });

        // === 第四层：顶部标题栏 ===
        HBox titleBar = createTitleBar();

        // === 第五层：底部输入区域 ===
        HBox inputBar = createInputBar();

        // === 组装 ===
        // 使用绝对定位布局
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.prefWidthProperty().bind(widthProperty());
        anchorPane.prefHeightProperty().bind(heightProperty());

        // 聊天区域：顶部 2/10
        AnchorPane.setTopAnchor(chatScrollPane, 0.0);
        AnchorPane.setLeftAnchor(chatScrollPane, 0.0);
        AnchorPane.setRightAnchor(chatScrollPane, 0.0);
        AnchorPane.setBottomAnchor(chatScrollPane, 0.0);

        // 顶部标题栏
        AnchorPane.setTopAnchor(titleBar, 0.0);
        AnchorPane.setLeftAnchor(titleBar, 0.0);
        AnchorPane.setRightAnchor(titleBar, 0.0);

        // 底部输入区域
        AnchorPane.setBottomAnchor(inputBar, 0.0);
        AnchorPane.setLeftAnchor(inputBar, 0.0);
        AnchorPane.setRightAnchor(inputBar, 0.0);

        anchorPane.getChildren().addAll(chatScrollPane, titleBar, inputBar);

        // 最终堆叠
        getChildren().addAll(backgroundLayer, overlay, anchorPane);

        // 输入框自动获取焦点
        setOnMouseClicked(e -> inputField.requestFocus());
    }

    /**
     * 创建顶部标题栏
     */
    private HBox createTitleBar() {
        HBox bar = new HBox(8);
        bar.setPadding(new javafx.geometry.Insets(8, 12, 8, 12));
        bar.setStyle("-fx-background-color: rgba(0,0,0,0.15);");

        // 选项按钮（左上角）
        optionButton = new Button("☰");
        optionButton.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08);" +
                "-fx-text-fill: rgba(255,255,255,0.7);" +
                "-fx-font-size: 18px;" +
                "-fx-padding: 6 12;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
        );
        optionButton.setOnAction(e -> {
            if (onOptionClick != null) onOptionClick.run();
        });

        // 应用标题
        Label titleLabel = new Label("AI 智能体框架-Eras");
        titleLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 14px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bar.getChildren().addAll(optionButton, titleLabel, spacer);
        return bar;
    }

    /**
     * 创建底部输入区域
     */
    private HBox createInputBar() {
        HBox bar = new HBox(8);
        bar.setPadding(new javafx.geometry.Insets(8, 12, 10, 12));
        bar.setStyle(
                "-fx-background-color: rgba(10,10,15,0.4);" +
                "-fx-border-color: rgba(255,255,255,0.04);" +
                "-fx-border-width: 1 0 0 0;"
        );

        // 图片按钮
        imageButton = new Button("📷");
        imageButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: rgba(255,255,255,0.35);" +
                "-fx-font-size: 16px;" +
                "-fx-padding: 6;" +
                "-fx-cursor: hand;"
        );
        imageButton.setOnAction(e -> {
            if (onImageClick != null) onImageClick.run();
        });

        // 输入框（半透明，支持多行）
        inputField = new TextArea();
        inputField.setPromptText("输入文字...（Enter换行，Ctrl+Enter发送）");
        inputField.setPrefRowCount(2);
        inputField.setWrapText(true);
        inputField.setStyle(
                "-fx-background-color: rgba(255,255,255,0.06);" +
                "-fx-text-fill: rgba(255,255,255,0.7);" +
                "-fx-prompt-text-fill: rgba(255,255,255,0.25);" +
                "-fx-font-size: 14px;" +
                "-fx-padding: 8 14;" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: rgba(255,255,255,0.05);" +
                "-fx-border-radius: 12;" +
                "-fx-font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;" +
                "-fx-control-inner-background: transparent;" +
                "-fx-highlight-fill: rgba(91,141,239,0.4);"
        );
        HBox.setHgrow(inputField, Priority.ALWAYS);

        // 快捷键：Ctrl+Enter 发送，Enter 换行
        inputField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && e.isControlDown()) {
                e.consume();
                triggerSend();
            }
        });

        // 发送按钮（纸飞机，半透明）
        sendButton = new Button("➤");
        sendButton.setStyle(
                "-fx-background-color: rgba(91,141,239,0.35);" +
                "-fx-text-fill: rgba(255,255,255,0.7);" +
                "-fx-font-size: 16px;" +
                "-fx-padding: 6 14;" +
                "-fx-background-radius: 18;" +
                "-fx-cursor: hand;"
        );
        sendButton.setOnAction(e -> triggerSend());

        bar.getChildren().addAll(imageButton, inputField, sendButton);
        return bar;
    }

    /**
     * 触发发送
     */
    private void triggerSend() {
        if (onSendClick != null) onSendClick.run();
    }

    // ========== 公共方法 ==========

    /**
     * 添加聊天消息到容器
     */
    public void addMessage(com.eras.model.Message message, String aiName, String userName) {
        ChatBubble bubble = new ChatBubble(message, aiName, userName);
        chatContainer.getChildren().add(bubble);
    }

    /**
     * 清除所有聊天消息
     */
    public void clearMessages() {
        chatContainer.getChildren().clear();
    }

    /**
     * 显示加载中的消息
     */
    public void showLoading(boolean show) {
        if (show) {
            Label loading = new Label("AI思考中...");
            loading.setId("loading-indicator");
            loading.setStyle(
                    "-fx-text-fill: rgba(255,255,255,0.35);" +
                    "-fx-font-size: 12px;" +
                    "-fx-padding: 8 16;" +
                    "-fx-font-style: italic;"
            );
            chatContainer.getChildren().add(loading);
        } else {
            Node loading = chatContainer.lookup("#loading-indicator");
            if (loading != null) {
                chatContainer.getChildren().remove(loading);
            }
        }
    }

    /**
     * 获取输入框文本
     */
    public String getInputText() {
        String text = inputField.getText();
        return text != null ? text.stripTrailing() : "";
    }

    /**
     * 清空输入框
     */
    public void clearInput() {
        inputField.clear();
    }

    /**
     * 获取背景层
     */
    public BackgroundLayer getBackgroundLayer() { return backgroundLayer; }

    // ========== 回调设置 ==========

    public void setOnOptionClick(Runnable cb) { this.onOptionClick = cb; }
    public void setOnSendClick(Runnable cb) { this.onSendClick = cb; }
    public void setOnImageClick(Runnable cb) { this.onImageClick = cb; }
}
