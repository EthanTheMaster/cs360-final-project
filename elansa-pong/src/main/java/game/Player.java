package game;

import engine.Collider;
import engine.Entity;
import engine.RectangleCollider;
import engine.Vec2d;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class Player extends Entity implements Cloneable {
    // The direction the player is moving in. Direction should be in {-1, 0, 1}
    private int direction;
    private final Vec2d positiveDirection;

    // Keycodes associated with positive and negative movement
    private int directionKeyPositive;
    private int directionKeyNegative;

    // Use unit square units for distance and seconds should be the time unit
    private double moveSpeed;

    private final RectangleCollider collider;

    private Vec2d lastContactFreePosition;

    /**
     * Creates a player represented as a rectangle
     * @param name the name of the player
     * @param position the position of the player's top-left corner
     * @param width the width of the player
     * @param height the height of the player
     * @param positiveDirection a unit vector pointing in the direction the player should move when their direction is positive
     * @param directionKeyPositive the key on the keyboard to make the player move in the positive direction
     * @param directionKeyNegative the key on the keyboard to make the player move in the negative direction
     * @param moveSpeed the velocity of the player in board units per second
     */
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
        // Fill in engine.Entity attributes
        this.id = name;
        this.position = position;
        this.velocity = new Vec2d(0, 0);

        this.collider = new RectangleCollider(position, width, height, 0);
        this.colliders = new ArrayList<>();
        this.colliders.add(collider);

        // Fill in game.Player fields
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
        setPosition(lastContactFreePosition);
        lastContactFreePosition = this.position;
    }

    @Override
    public void setPosition(Vec2d position) {
        lastContactFreePosition = this.position;
        super.setPosition(position);
        // Update the collider's position
        collider.setPosition(position);
    }

    /**
     * Computes the direction of the player should move in based on the locations of a collection of balls
     * @param balls a collection of balls
     */
    public void setDirectionAutomatically(ArrayList<Ball> balls) {
        Vec2d[] verticesAndBasis = collider.computeVerticesAndBasis();
        Vec2d paddleCenter = verticesAndBasis[0]
                .add(verticesAndBasis[1])
                .add(verticesAndBasis[2])
                .add(verticesAndBasis[3])
                .scale(0.25);
        balls.stream()
            .min((b1, b2) -> {
                double d1 = b1.getPosition().sub(paddleCenter).mag();
                double d2 = b2.getPosition().sub(paddleCenter).mag();
                return Double.compare(d1, d2);
            })
            .ifPresent(b -> {
                double signedMagnitude = b.getPosition().sub(paddleCenter).dot(positiveDirection);
                double paddleSpan = Math.max(collider.getHeight(), collider.getWidth()) / 2;
                if (Math.abs(signedMagnitude) < paddleSpan) {
                     setDirection(0);
                } else if (signedMagnitude < 0) {
                    setDirection(-1);
                } else {
                    setDirection(1);
                }
            });
    }

    /**
     * Sets the direction based on the key pressed on the keyboard
     * @param keyCode the key code of the key pressed on the keyboard
     */
    public void setDirectionKeyPress(int keyCode) {
        if (keyCode == directionKeyPositive) {
            setDirection(1);
        } else if (keyCode == directionKeyNegative) {
            setDirection(-1);
        }
    }

    /**
     * Sets the direction based on the key released on the keyboard
     * @param keyCode the key code of the key pressed on the keyboard
     */
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

    public int getDirectionKeyPositive() {
        return directionKeyPositive;
    }

    public void setDirectionKeyPositive(int directionKeyPositive) {
        this.directionKeyPositive = directionKeyPositive;
    }

    public int getDirectionKeyNegative() {
        return directionKeyNegative;
    }

    public void setDirectionKeyNegative(int directionKeyNegative) {
        this.directionKeyNegative = directionKeyNegative;
    }

    @Override
    public Object clone() {
//        Player res = new Player(
//                this.id,
//                this.position,
//                this.collider.getWidth(),
//                this.collider.getHeight(),
//                this.positiveDirection,
//                this.directionKeyPositive,
//                this.directionKeyNegative,
//                this.moveSpeed
//        );
//        res.setDirection(this.direction);
//        res.setVelocity(this.velocity);
//        return res;
        Player res = null;
        try {
            res = (Player) super.clone();
            res.setPosition(this.position);
            res.setVelocity(this.velocity);
            res.setDirection(this.direction);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return res;
    }
}
