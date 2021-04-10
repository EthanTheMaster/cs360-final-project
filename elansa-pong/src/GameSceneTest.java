import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

public class GameSceneTest implements GameScene {
    private Player player = new Player(
            "Player1",
            new Vec2d(0.03, 0.5),
            0.05,
            0.1,
            new Vec2d(0, 1),
            40,
            38,
            0.3
    );

    private Obstacle wall = new Obstacle(
            "Wall",
            new Collider[]{
                    new RectangleCollider(new Vec2d(0, 0), 1.0, 0.05, 0),
                    new RectangleCollider(new Vec2d(0.0, 0.95), 1.0, 0.05, 0)
            },
            new int[]{0, 0, 0},
            true,
            null
    );

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

        player.setPosition(player.getPosition().add(player.getVelocity().scale(deltaTime)));
        Collider[] playerWallColliders = player.collidesWith(wall);
        if (playerWallColliders != null) {
            player.onCollision(wall, playerWallColliders[1]);
            wall.onCollision(player, playerWallColliders[0]);
        }
    }

    @Override
    public void onKeyPressed(KeyEvent e) {
//        System.out.println("Pressed");
//        System.out.println(e.getCode().getCode());
        player.setDirectionKeyPress(e.getCode().getCode());
    }

    @Override
    public void onKeyReleased(KeyEvent e) {
//        System.out.println("Released");
//        System.out.println(e.getCode().getCode());
        player.setDirectionKeyRelease(e.getCode().getCode());
    }

    @Override
    public void render(Canvas canvas) {
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        player.render(canvas);
        wall.render(canvas);
    }
}
