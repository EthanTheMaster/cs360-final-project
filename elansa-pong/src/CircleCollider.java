public class CircleCollider implements Collider {
    private Vec2d center;
    private double radius;

    public CircleCollider(Vec2d center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    @Override
    public boolean collide(Collider other) {
        if (other instanceof CircleCollider) {
            CircleCollider otherCircle = (CircleCollider) other;
            double distance = otherCircle.center.sub(this.center).mag();
            return distance <= this.radius + otherCircle.radius;
        } else if (other instanceof RectangleCollider) {
            RectangleCollider otherRect = (RectangleCollider) other;
            return otherRect.collide(this);
        }
        return false;
    }

    @Override
    public Vec2d getPosition() {
        return center;
    }

    @Override
    public void setPosition(Vec2d position) {
        center = position;
    }

    public Vec2d getCenter() {
        return center;
    }

    public void setCenter(Vec2d center) {
        this.center = center;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
