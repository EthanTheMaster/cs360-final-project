package engine;

import java.io.Serializable;

public interface CollisionEventHandler extends Serializable {
    /**
     * This method is invoked when a collision is detected
     * @param other the entity that triggered a collision
     * @param otherCollider a collider in the other entity that triggered the collision
     */
    void handleCollision(Entity other, Collider otherCollider);
}
