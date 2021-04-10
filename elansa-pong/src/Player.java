import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class Player extends Entity {
    // The direction the player is moving in. Direction should be in {-1, 0, 1}
    private int direction;
    private final Vec2d positiveDirection;

    // Keycodes associated with positive and negative movement
    private final int directionKeyPositive;
    private final int directionKeyNegative;

    // Use unit square units for distance and seconds should be the time unit
    private double moveSpeed;

    private final RectangleCollider collider;

    private Vec2d lastContactFreePosition;

    public Player(
            String name,
            Vec2d position,
            double width,
            double height,
            Vec2d positiveDirection,
            int directionKeyPositive,
            int directionKeyNegative,
            double moveSpeed
    ) {
        // Fill in Entity attributes
        this.id = name;
        this.position = position;
        this.velocity = new Vec2d(0, 0);

        this.collider = new RectangleCollider(position, width, height, 0);
        this.colliders = new ArrayList<>();
        this.colliders.add(collider);

        // Fill in Player fields
        direction = 0;
        this.positiveDirection = positiveDirection;
        this.directionKeyPositive = directionKeyPositive;
        this.directionKeyNegative = directionKeyNegative;
        this.moveSpeed = moveSpeed;
        lastContactFreePosition = position;

    }

    @Override
    public void render(Canvas canvas) {
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        ctx.setFill(Color.RED);
        ctx.fillRect(
                collider.getOrigin().getX() * canvasWidth,
                collider.getOrigin().getY() * canvasHeight,
                collider.getWidth() * canvasWidth,
                collider.getHeight() * canvasHeight
        );
    }

    @Override
    public void onCollision(Entity other, Collider otherCollider) {
        if (other instanceof Obstacle) {
            setPosition(lastContactFreePosition);
            lastContactFreePosition = this.position;
        }
    }

    @Override
    public void setPosition(Vec2d position) {
        lastContactFreePosition = this.position;
        super.setPosition(position);
        // Update the collider's position
        collider.setPosition(position);
    }

    public void setDirectionKeyPress(int keyCode) {
        if (keyCode == directionKeyPositive) {
            setDirection(1);
        } else if (keyCode == directionKeyNegative) {
            setDirection(-1);
        }
    }

    public void setDirectionKeyRelease(int keyCode) {
        // If the key released matches the direction being moved in, stay still
        if (
            (keyCode == directionKeyPositive && direction == 1) ||
            (keyCode == directionKeyNegative && direction == -1)
        ) {
            setDirection(0);
        }
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
        this.velocity = positiveDirection.scale(direction * moveSpeed);
    }

    public double getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(double moveSpeed) {
        this.moveSpeed = moveSpeed;
    }
}
