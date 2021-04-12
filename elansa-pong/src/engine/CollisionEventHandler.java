package engine;

import java.io.Serializable;

public interface CollisionEventHandler extends Serializable {
    void handleCollision(Entity other, Collider otherCollider);
}
