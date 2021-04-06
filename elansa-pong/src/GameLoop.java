import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;

public class GameLoop extends AnimationTimer {
    private GameScene gameScene;
    private Canvas canvas;

    public GameLoop(GameScene gameScene, Canvas canvas) {
        this.gameScene = gameScene;
        this.canvas = canvas;
    }

    @Override
    public void handle(long now) {
        gameScene.updateState(now);
        gameScene.render(canvas);
    }
}
