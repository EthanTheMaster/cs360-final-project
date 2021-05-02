package game;

import engine.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;

import java.nio.file.Paths;
import java.util.ArrayList;

public class Ball extends Entity {

    private CircleCollider collider;
    private Vec2d lastContactFreePosition;
    private transient AudioClip bonkClip;

    /**
     * Creates a ball
     * @param name the name of the ball
     * @param centerPosition the initial position of the ball's center
     * @param radius the ball's radius
     */
    public Ball(
        String name,
        Vec2d centerPosition,
        double radius
    ) {
        this.id = name;
        this.position = centerPosition;
        this.velocity = new Vec2d(0, 0);
        this.colliders = new ArrayList<>();

        this.collider = new CircleCollider(centerPosition, radius);
        this.colliders.add(collider);

        lastContactFreePosition = centerPosition;
    }

    @Override
    public void setPosition(Vec2d position) {
        lastContactFreePosition = this.position;
        super.setPosition(position);
        collider.setPosition(position);
    }

    @Override
    public void render(Canvas canvas) {
        GraphicsContext ctx = canvas.getGraphicsContext2D();

        ctx.setFill(Color.BLUE);
        double centerX = collider.getCenter().getX();
        double centerY = collider.getCenter().getY();
        double r = collider.getRadius();
        ctx.fillOval(
                (centerX - r) * canvas.getWidth(),
                (centerY - r) * canvas.getHeight(),
                (2*r) * canvas.getWidth(),
                (2*r) * canvas.getHeight()
        );
    }

    @Override
    public void onCollision(Entity other, Collider otherCollider) {
        setPosition(lastContactFreePosition);
        lastContactFreePosition = this.position;

        if (bonkClip == null) {
            bonkClip = new AudioClip(
                    Paths.get(GameSettings.BALL_BONK_AUDIO).toUri().toString()
            );
        }
        double volume = 1.0;
        if (otherCollider instanceof RectangleCollider) {
            RectangleCollider rectangleCollider = (RectangleCollider) otherCollider;
            Vec2d contactPoint = rectangleCollider.findClosestPoint(collider.getCenter());
            Vec2d normal = collider.getCenter().sub(contactPoint);

            volume = Math.abs(this.velocity.normalize().dot(normal.normalize()));
            this.velocity = this.velocity
                    .add(
                            this.velocity.projectOnto(normal).scale(-2.0)
                    );
        } else if (otherCollider instanceof CircleCollider) {
            CircleCollider otherCircleCollider = (CircleCollider) otherCollider;
            Vec2d normal = collider.getCenter().sub(otherCircleCollider.getCenter());
            volume = Math.abs(this.velocity.normalize().dot(normal.normalize()));
            this.velocity = this.velocity
                    .add(
                            this.velocity.projectOnto(normal).scale(-2.0)
                    );
        }

        if (GameSettings.SOUND_EFFECTS_ON) {
            bonkClip.setVolume(volume);
            bonkClip.play();
        }
    }
}
