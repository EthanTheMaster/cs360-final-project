package game.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class DisplayMessage extends Label {
    private final Timeline fadeAnimation;

    public DisplayMessage(double fadeTimeMs, double sustainMs) {
        super("");
        super.setOpacity(0.0);
        super.setFont(new Font(20));
        super.setPadding(new javafx.geometry.Insets(15,15,15,15));
        super.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        KeyValue hidden = new KeyValue(super.opacityProperty(), 0.0);
        KeyValue showing = new KeyValue(super.opacityProperty(), 1.0);
        KeyFrame fadeInAndOutFrame0 = new KeyFrame(Duration.ZERO, hidden);
        KeyFrame fadeInAndOutFrame1 = new KeyFrame(Duration.millis(fadeTimeMs), showing);
        KeyFrame fadeInAndOutFrame2 = new KeyFrame(Duration.millis(fadeTimeMs+sustainMs), showing);
        KeyFrame fadeInAndOutFrame3 = new KeyFrame(Duration.millis(fadeTimeMs+sustainMs+fadeTimeMs), hidden);
        fadeAnimation = new Timeline(fadeInAndOutFrame0, fadeInAndOutFrame1, fadeInAndOutFrame2, fadeInAndOutFrame3);
    }

    public void flashMessage(String message) {
        super.setText(message);
        fadeAnimation.play();
    }

    public void sustainMessage(String message) {
        // Stop any ongoing animations
        fadeAnimation.stop();
        super.setText(message);
        super.setOpacity(1.0);
    }

    public void hideMessage() {
        fadeAnimation.stop();
        super.setOpacity(0.0);
    }

}