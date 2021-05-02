package engine;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;

import java.io.Serializable;

public interface GameScene extends Serializable {
    /**
     * The method called to progress the game state
     * @param currentTime the time in nanseconds relative to an arbitrary start time
     */
    void updateState(long currentTime);

    /**
     * The method called when it is detected that key on the keyboard has been depressed
     * @param e a description of the keystroke that occurred
     */
    void onKeyPressed(KeyEvent e);

    /**
     * The method called when it is detected that a key on the keyboard has been released
     * @param e a description of the keystroke that occurred
     */
    void onKeyReleased(KeyEvent e);

    /**
     * The method called to render the game scene to a canvas
     * @param canvas the canvas to draw the scene on
     */
    void render(Canvas canvas);

    /**
     * Creates a Canvas object configured to be compatible with the GameScene interface
     * @param width the Canvas's width in pixels
     * @param height the Canvas's height in pixels
     * @return
     */
    default Canvas generateRenderableComponent(int width, int height) {
        Canvas canvas = new Canvas(width, height);
        canvas.setOnKeyPressed(this::onKeyPressed);
        canvas.setOnKeyReleased(this::onKeyReleased);
        canvas.setFocusTraversable(true);
        canvas.requestFocus();

        return canvas;
    }
}
