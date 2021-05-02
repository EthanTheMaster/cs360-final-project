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

    /**
     * Creates a component that can display messages
     * @param fadeTimeMs the time it takes to fade the message into and out of view in milliseconds
     * @param sustainMs the duration of the message displayed in milliseconds
     */
    public DisplayMessage(double fadeTimeMs, double sustainMs) {
        super("");
        super.setOpacity(0.0);
        super.setFont(new Font(20));
        super.setPadding(new javafx.geometry.Insets(15,15,15,15));
        super.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        super.setWrapText(true);
        KeyValue hidden = new KeyValue(super.opacityProperty(), 0.0);
        KeyValue showing = new KeyValue(super.opacityProperty(), 1.0);
        KeyFrame fadeInAndOutFrame0 = new KeyFrame(Duration.ZERO, hidden);
        KeyFrame fadeInAndOutFrame1 = new KeyFrame(Duration.millis(fadeTimeMs), showing);
        KeyFrame fadeInAndOutFrame2 = new KeyFrame(Duration.millis(fadeTimeMs+sustainMs), showing);
        KeyFrame fadeInAndOutFrame3 = new KeyFrame(Duration.millis(fadeTimeMs+sustainMs+fadeTimeMs), hidden);
        fadeAnimation = new Timeline(fadeInAndOutFrame0, fadeInAndOutFrame1, fadeInAndOutFrame2, fadeInAndOutFrame3);
    }

    /**
     * Flashes a message by fading in and then fading out from the scene
     * @param message the message to be displayed
     */
    public void flashMessage(String message) {
        super.setText(message);
        fadeAnimation.play();
    }

    /**
     * Fades in a message that does not disappear
     * @param message the message to be displayed
     */
    public void sustainMessage(String message) {
        // Stop any ongoing animations
        fadeAnimation.stop();
        super.setText(message);
        super.setOpacity(1.0);
    }

    /**
     * Hides the message from view
     */
    public void hideMessage() {
        fadeAnimation.stop();
        super.setOpacity(0.0);
    }

}