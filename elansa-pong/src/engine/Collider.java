package engine;

import java.io.Serializable;

public interface Collider extends Serializable {
    boolean collide(Collider other);
    Vec2d getPosition();
    void setPosition(Vec2d position);
}
