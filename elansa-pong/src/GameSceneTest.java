import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class GameSceneTest implements GameScene {
    private final Player player1 = new Player(
            "Player1",
            new Vec2d(0.03, 0.5),
            0.05,
            0.1,
            new Vec2d(0, 1),
            40,
            38,
            0.5
    );

    private final Player player2 = new Player(
            "Player2",
            new Vec2d(1-0.03-0.05, 0.5),
            0.05,
            0.1,
            new Vec2d(0, 1),
            40,
            38,
            0.5
    );

    private final Obstacle wall = new Obstacle(
            "Wall",
            new Collider[]{
                    new RectangleCollider(new Vec2d(0, 0), 1.0, 0.05, 0),
                    new RectangleCollider(new Vec2d(0.0, 0.95), 1.0, 0.05, 0),
                    new CircleCollider(new Vec2d(0.3, 0.5), 0.07),
                    new CircleCollider(new Vec2d(0.7, 0.5), 0.07),
                    new CircleCollider(new Vec2d(0.5, 0.3), 0.07),
                    new CircleCollider(new Vec2d(0.5, 0.7), 0.07)
            },
            new int[]{0, 0, 0},
            true,
            null
    );

    private final Ball ball = new Ball(
            "Ball",
            new Vec2d(0.5, 0.5),
            0.02
    );

    ArrayList<Entity> entities = new ArrayList<>();

    public GameSceneTest() {
        ball.setVelocity(new Vec2d(-0.2, -0.2));

        entities.add(player1);
        entities.add(player2);
        entities.add(ball);
        entities.add(wall);
    }

    private Long lastRecordedTime = null;
    @Override
    public void updateState(long currentTime) {
        double deltaTime = 0.0;
        if (lastRecordedTime != null) {
            deltaTime = (double) (currentTime - lastRecordedTime) / 1000000000.0;
        }
        lastRecordedTime = currentTime;

        for (Entity entity : entities) {
            entity.setPosition(
                    entity.getPosition().add(entity.getVelocity().scale(deltaTime))
            );
        }

        for (int i = 0; i < entities.size()-1; i++) {
            for (int j = i+1; j < entities.size(); j++) {
                Entity entity1 = entities.get(i);
                Entity entity2 = entities.get(j);
                Collider[] colliders = entity1.collidesWith(entity2);
                if (colliders != null) {
                    entity1.onCollision(entity2, colliders[1]);
                    entity2.onCollision(entity1, colliders[0]);
                }
            }
        }
    }

    @Override
    public void onKeyPressed(KeyEvent e) {
//        System.out.println("Pressed");
//        System.out.println(e.getCode().getCode());
        player1.setDirectionKeyPress(e.getCode().getCode());
        player2.setDirectionKeyPress(e.getCode().getCode());
    }

    @Override
    public void onKeyReleased(KeyEvent e) {
//        System.out.println("Released");
//        System.out.println(e.getCode().getCode());
        player1.setDirectionKeyRelease(e.getCode().getCode());
        player2.setDirectionKeyRelease(e.getCode().getCode());
    }

    @Override
    public void render(Canvas canvas) {
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (Entity entity : entities) {
            entity.render(canvas);
        }
    }
}
