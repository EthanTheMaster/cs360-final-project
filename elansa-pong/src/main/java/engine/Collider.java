package engine;

import java.io.Serializable;

public interface Collider extends Serializable {
    /**
     * Determines whether two Colliders intersect each other
     * @param other the other collider with which to check for an intersection
     * @return a boolean that determines if the current Collider intersects with other
     */
    boolean collide(Collider other);

    /**
     * Gets the position of the Collider
     * @return the position of the Collider
     */
    Vec2d getPosition();

    /**
     * Sets the position of the Collider
     * @param position the new location of the Collider:
     */
    void setPosition(Vec2d position);
}
