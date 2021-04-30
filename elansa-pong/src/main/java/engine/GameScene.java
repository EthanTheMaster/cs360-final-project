package engine;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;

import java.io.Serializable;

public interface GameScene extends Serializable {
    void updateState(long currentTime);
    void onKeyPressed(KeyEvent e);
    void onKeyReleased(KeyEvent e);
    void render(Canvas canvas);

    default Canvas generateRenderableComponent(int width, int height) {
        Canvas canvas = new Canvas(width, height);
        canvas.setOnKeyPressed(this::onKeyPressed);
        canvas.setOnKeyReleased(this::onKeyReleased);
        canvas.setFocusTraversable(true);
        canvas.requestFocus();

        return canvas;
    }
}
