package engine;

import javafx.scene.canvas.Canvas;

import java.io.Serializable;
import java.util.ArrayList;

public abstract class Entity implements Serializable {
    protected String id;
    protected Vec2d position;
    protected Vec2d velocity;
    protected ArrayList<Collider> colliders;

    /**
     * Renders the entity to a canvas
     * @param canvas the canvas to render the entity on
     */
    public abstract void render(Canvas canvas);

    /**
     * The method invoked when this entity has collided with another entity
     * @param other the entity that this entity collided with
     * @param otherCollider the collider on the other entity that detected the collision
     */
    public abstract void onCollision(Entity other, Collider otherCollider);

    /**
     * Checks if this entity collides with another entity
     * @param other the entity to check collision with
     * @return return either null or 2 colliders. Null is returned if there is not collision, but
     * if there is a collision the first collider will be the collider in this entity and the
     * second collider will be the collider in the other entity.
     */
    public Collider[] collidesWith(Entity other) {
        for (Collider thisCollider : this.colliders) {
            for (Collider otherCollider : other.colliders) {
                if (thisCollider.collide(otherCollider)) {
                    return new Collider[]{thisCollider, otherCollider};
                }
            }
        }

        return null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Vec2d getPosition() {
        return position;
    }

    public void setPosition(Vec2d position) {
        this.position = position;
    }

    public Vec2d getVelocity() {
        return velocity;
    }

    public void setVelocity(Vec2d velocity) {
        this.velocity = velocity;
    }

    public ArrayList<Collider> getColliders() {
        return colliders;
    }

    public void setColliders(ArrayList<Collider> colliders) {
        this.colliders = colliders;
    }
}
