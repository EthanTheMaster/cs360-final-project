import java.io.Serializable;

public class Vec2d implements Serializable {
    private double x;
    private double y;

    public Vec2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Scales a vector by a constant multiple
     * @param c the constant multiple
     * @return a scaled vector
     */
    public Vec2d scale(double c) {
        return new Vec2d(x*c, y*c);
    }

    /**
     * Adds the current vector the another vector
     * @param other the vector to add with
     * @return the vector sum
     */
    public Vec2d add(Vec2d other) {
        return new Vec2d(
            this.x + other.x, this.y + other.y
        );
    }

    /**
     * Computes the difference between the current vector with another vector
     * @param other the vector to subtract with
     * @return the vector difference
     */
    public Vec2d sub(Vec2d other) {
        return new Vec2d(
                this.x - other.x, this.y - other.y
        );
    }

    /**
     * Computes the dot product between the current vector and another vector
     * @param other the other vector
     * @return the dot product
     */
    public double dot(Vec2d other) {
        return this.x * other.x + this.y * other.y;
    }

    /**
     * Computes the length of the current vector
     * @return the length of the vector
     */
    public double mag() {
        return Math.sqrt(this.dot(this));
    }

    /**
     * Computes the unit vector pointing in the same direction as the current vector
     * @return unit vector pointing in the same direction as the current vector
     */
    public Vec2d normalize() {
        return this.scale(1.0 / this.mag());
    }

    /**
     * Computes the projection of the current vector onto another vector
     * @param other the vector to project onto
     * @return the projection vector
     */
    public Vec2d projectOnto(Vec2d other) {
        Vec2d unit = other.normalize();
        return unit.scale(unit.dot(this));
    }

    /**
     * Compute the rejection of the current vector on another vector
     * @param other the vector to reject on
     * @return the rejection vector
     */
    public Vec2d rejectOn(Vec2d other) {
        return this.sub(this.projectOnto(other));
    }

    /**
     * Rotates the current vector about the origin
     * @param angle the angle to rotate by in radians
     * @return a rotated vector
     */
    public Vec2d rotate(double angle) {
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        return new Vec2d(x*c - y*s, x*s + y*c);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "<" + x + ", " + y + ">";
    }
}
