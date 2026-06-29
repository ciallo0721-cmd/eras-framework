package com.eras.ui;

import javafx.animation.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.io.File;

/**
 * 角色背景层 - 角色立绘即背景，充满全屏
 *
 * 支持：
 * - PNG / JPG（静态背景级立绘）
 * - GIF（自动播放动画）
 * - Live2D（预留接口）
 * - 全身半透明
 */
public class BackgroundLayer extends Pane {

    private final ImageView characterView;

    private Image characterImage;

    // 待机动画(呼吸/浮动效果)
    private Timeline idleAnimation;

    public BackgroundLayer() {
        setPickOnBounds(false);

        characterView = new ImageView();
        characterView.setPreserveRatio(false);  // 拉伸填满全屏
        characterView.setSmooth(true);
        characterView.setOpacity(0.75); // 半透明沉浸感

        getChildren().add(characterView);

        widthProperty().addListener((obs, oldVal, newVal) -> updateLayout());
        heightProperty().addListener((obs, oldVal, newVal) -> updateLayout());
    }

    /**
     * 设置角色背景（立绘充满全屏）
     *
     * @param imagePath 图片路径（支持 .png .jpg .gif .live2d）
     */
    public void setBackgroundImage(String imagePath) {
        stopIdleAnimation();
        if (imagePath == null || imagePath.isEmpty()) {
            characterView.setImage(null);
            characterImage = null;
            setStyle("-fx-background-color: rgba(26,26,46,0.85);");
            return;
        }
        try {
            File file = new File(imagePath);
            if (!file.exists()) return;

            String lower = imagePath.toLowerCase();

            if (lower.endsWith(".gif")) {
                // GIF动画 - JavaFX原生支持
                characterImage = new Image(file.toURI().toString(), true);
                characterView.setImage(characterImage);
            } else if (lower.endsWith(".live2d") || lower.endsWith(".model3.json")) {
                // Live2D预留 - 降级显示为静态图
                characterImage = new Image(file.toURI().toString(), true);
                characterView.setImage(characterImage);
            } else {
                // 静态PNG/JPG - 全屏背景
                characterImage = new Image(file.toURI().toString(), false);
                characterView.setImage(characterImage);
            }

            setStyle(null);
            updateLayout();
            startIdleAnimation();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 兼容旧接口：setCharacterImage 等同于 setBackgroundImage
     */
    public void setCharacterImage(String imagePath) {
        setBackgroundImage(imagePath);
    }

    /**
     * 待机浮动动画（全屏画面微微浮动）
     */
    private void startIdleAnimation() {
        if (characterImage == null) return;
        stopIdleAnimation();

        double baseY = characterView.getLayoutY();

        idleAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(characterView.layoutYProperty(), baseY - 3, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(3),
                        new KeyValue(characterView.layoutYProperty(), baseY + 3, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(6),
                        new KeyValue(characterView.layoutYProperty(), baseY - 3, Interpolator.EASE_BOTH))
        );
        idleAnimation.setCycleCount(Animation.INDEFINITE);
        idleAnimation.play();
    }

    private void stopIdleAnimation() {
        if (idleAnimation != null) {
            idleAnimation.stop();
            idleAnimation = null;
        }
    }

    /**
     * 更新布局 - 角色全屏铺满
     */
    private void updateLayout() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) return;

        if (characterImage != null) {
            characterView.setFitWidth(w);
            characterView.setFitHeight(h);
            characterView.setLayoutX(0);
            characterView.setLayoutY(0);
        }
    }

    public void clear() {
        stopIdleAnimation();
        characterView.setImage(null);
        characterImage = null;
    }
}
