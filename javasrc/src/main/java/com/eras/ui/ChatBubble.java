package com.eras.ui;

import com.eras.model.Message;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * 聊天气泡组件 - 沉浸式，无人头像，半透明
 */
public class ChatBubble extends HBox {

    private final Message message;
    private final boolean isUser;
    private final String aiName;
    private final String userName;

    public ChatBubble(Message msg, String aiName, String userName) {
        this.message = msg;
        this.isUser = msg.getRole() == Message.Role.USER;
        this.aiName = aiName;
        this.userName = userName;
        buildBubble();
    }

    private void buildBubble() {
        setPadding(new Insets(2, 16, 2, 16));
        setSpacing(6);

        // 消息内容区域（无头像，纯文字沉浸式）
        VBox contentBox = new VBox(2);
        contentBox.setMaxWidth(500);

        // 名称标签（极小号，半透明）
        Label nameLabel = new Label(isUser ? userName : aiName);
        nameLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.35); -fx-font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;");

        // 消息文本（半透明气泡）
        Text text = new Text(message.getContent());
        text.setWrappingWidth(460);

        String colorHex = message.getColor() != null && !message.getColor().isEmpty()
                ? message.getColor() : "#FFFFFF";
        text.setStyle("-fx-fill: " + colorHex + "; -fx-font-size: 14px; -fx-font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;");

        TextFlow textFlow = new TextFlow(text);
        textFlow.setPadding(new Insets(8, 12, 8, 12));
        textFlow.setStyle(
                "-fx-background-color: " + (isUser ? "rgba(74,144,217,0.25)" : "rgba(255,255,255,0.12)") + ";" +
                "-fx-background-radius: " + (isUser ? "12 12 4 12" : "12 12 12 4") + ";" +
                "-fx-padding: 6 10 6 10;"
        );

        // 右键复制菜单
        ContextMenu ctxMenu = new ContextMenu();
        MenuItem copyItem = new MenuItem("复制");
        copyItem.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(message.getContent());
            clipboard.setContent(content);
        });
        ctxMenu.getItems().add(copyItem);
        textFlow.setOnContextMenuRequested(e -> ctxMenu.show(textFlow, e.getScreenX(), e.getScreenY()));
        setOnContextMenuRequested(e -> ctxMenu.show(this, e.getScreenX(), e.getScreenY()));

        contentBox.getChildren().addAll(nameLabel, textFlow);

        // 排列：用户消息靠右，AI消息靠左
        if (isUser) {
            setAlignment(Pos.CENTER_RIGHT);
            getChildren().add(contentBox);
            contentBox.setAlignment(Pos.CENTER_RIGHT);
        } else {
            setAlignment(Pos.CENTER_LEFT);
            getChildren().add(contentBox);
        }

        // 如果附带图片，添加提示
        if (message.isHasImage()) {
            Label imgLabel = new Label("📷 图片已发送");
            imgLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 11px; -fx-font-style: italic; -fx-font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;");
            contentBox.getChildren().add(imgLabel);
        }
    }

    public Message getMessage() { return message; }
}
