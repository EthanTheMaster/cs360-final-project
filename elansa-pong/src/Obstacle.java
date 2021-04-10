import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;

public class Obstacle extends Entity {
    private int[] colorRgb;
    private boolean isVisible;
    private CollisionEventHandler trigger;

    public Obstacle(String name, Collider[] hitZones, int[] colorRgb, boolean isVisible, CollisionEventHandler trigger) {
        this.id = name;
        this.position = new Vec2d(0, 0);
        this.velocity = new Vec2d(0, 0);
        this.colliders = new ArrayList<>();
        colliders.addAll(Arrays.asList(hitZones));

        this.colorRgb = colorRgb;
        this.isVisible = isVisible;
        this.trigger = trigger;
    }

    @Override
    public void setPosition(Vec2d position) {
        Vec2d displacement = position.sub(this.position);
        super.setPosition(position);

        for (Collider collider : this.colliders) {
            collider.setPosition(
                    collider.getPosition().add(displacement)
            );
        }
    }

    @Override
    public void render(Canvas canvas) {
        if (!isVisible) {
            return;
        }

        GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.setFill(Color.rgb(colorRgb[0], colorRgb[1], colorRgb[2]));
        for (Collider collider : this.colliders) {
            if (collider instanceof RectangleCollider) {
                RectangleCollider rectangle = (RectangleCollider) collider;
                Vec2d[] verticesAndBasis = rectangle.computeVerticesAndBasis();
                ctx.fillPolygon(
                        new double[]{
                                verticesAndBasis[0].getX() * canvas.getWidth(),
                                verticesAndBasis[1].getX() * canvas.getWidth(),
                                verticesAndBasis[2].getX() * canvas.getWidth(),
                                verticesAndBasis[3].getX() * canvas.getWidth(),
                        },
                        new double[]{
                                verticesAndBasis[0].getY() * canvas.getHeight(),
                                verticesAndBasis[1].getY() * canvas.getHeight(),
                                verticesAndBasis[2].getY() * canvas.getHeight(),
                                verticesAndBasis[3].getY() * canvas.getHeight(),
                        },
                        4
                );
            } else if (collider instanceof CircleCollider) {
                CircleCollider circle = (CircleCollider) collider;
                double centerX = circle.getCenter().getX();
                double centerY = circle.getCenter().getY();
                double r = circle.getRadius();
                ctx.fillOval(
                        (centerX - r) * canvas.getWidth(),
                        (centerY - r) * canvas.getHeight(),
                        2*r * canvas.getWidth(),
                        2*r * canvas.getHeight()
                );
            }
        }
    }

    @Override
    public void onCollision(Entity other, Collider otherCollider) {
        if (trigger != null) {
            trigger.handleCollision(other, otherCollider);
        }
    }

    public int[] getColorRgb() {
        return colorRgb;
    }

    public void setColorRgb(int[] colorRgb) {
        this.colorRgb = colorRgb;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public CollisionEventHandler getTrigger() {
        return trigger;
    }

    public void setTrigger(CollisionEventHandler trigger) {
        this.trigger = trigger;
    }
}
