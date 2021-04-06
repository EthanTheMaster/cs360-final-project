import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

public class GameSceneTest implements GameScene {
    private double rectX = 0.0;
    private double rectY = 250.0;
    private Long lastRecordedTime = null;

    @Override
    public void updateState(long currentTime) {
        double deltaTime = 0.0;
        if (lastRecordedTime == null) {
            lastRecordedTime = currentTime;
        } else {
            deltaTime = (double) (currentTime - lastRecordedTime)/1000000000.0;
            lastRecordedTime = currentTime;
        }

        rectX += deltaTime * 20;
    }

    @Override
    public void onKeyPressed(KeyEvent e) {
        System.out.println("Pressed");
        System.out.println(e.getText());
    }

    @Override
    public void onKeyReleased(KeyEvent e) {
        System.out.println("Released");
        System.out.println(e.getText());
    }

    @Override
    public void render(Canvas canvas) {
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        ctx.setFill(Color.RED);
        ctx.fillRect(rectX, rectY, 50, 50);
    }
}
