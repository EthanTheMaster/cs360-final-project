package engine;

public class RectangleCollider implements Collider {
    private Vec2d origin;
    private double width;
    private double height;
    private double angle;

    public RectangleCollider(Vec2d origin, double width, double height, double angle) {
        this.origin = origin;
        this.width = width;
        this.height = height;
        this.angle = angle;
    }

    /**
     * Computes the vertices of the rotated rectangle along and also returns the local coordinate space
     * @return a list of vectors where the first 4 vectors are the vertices and the last two are the basis
     * vectors of the local coordinate space
     */
    public Vec2d[] computeVerticesAndBasis() {
        Vec2d b1 = new Vec2d(width, 0).rotate(angle);
        Vec2d b2 = new Vec2d(0, height).rotate(angle);
        Vec2d sum = b1.add(b2);

        return new Vec2d[] {
                origin,
                origin.add(b1),
                origin.add(sum),
                origin.add(b2),
                b1.normalize(),
                b2.normalize()
        };
    }

    /**
     * Finds the closest point on the rectangle to a given point
     * @param point the point to find the closest point to
     * @return the closest point to the given point
     */
    public Vec2d findClosestPoint(Vec2d point) {
        Vec2d[] thisVerticesAndBasis = this.computeVerticesAndBasis();
        Vec2d basis1 = thisVerticesAndBasis[4];
        Vec2d basis2 = thisVerticesAndBasis[5];
        Vec2d displacement = point.sub(thisVerticesAndBasis[0]);

        // Decompose displacement in terms of basis1 and basis2
        // displacement = c1*basis1 + c2*basis2
        double c1 = basis1.dot(displacement);
        double c2 = basis2.dot(displacement);

        // Clamp c1 to [0, width] and clamp c2 to [0, height]
        c1 = Math.max(0, c1);
        c2 = Math.max(0, c2);
        c1 = Math.min(width, c1);
        c2 = Math.min(height, c2);

        return basis1.scale(c1).add(basis2.scale(c2)).add(thisVerticesAndBasis[0]);
    }

    @Override
    public boolean collide(Collider other) {
        if (other instanceof RectangleCollider) {
            RectangleCollider otherRect = (RectangleCollider) other;
            Vec2d[] thisVerticesAndBasis = this.computeVerticesAndBasis();
            Vec2d[] otherVerticesAndBasis = otherRect.computeVerticesAndBasis();

            // Check if other's vertices are inside this's rectangular region
            for (int i = 0; i < 4; i++) {
                Vec2d vertex = otherVerticesAndBasis[i];
                Vec2d displacement = vertex.sub(thisVerticesAndBasis[0]);
                // Decompose displacement in terms of this's local coordinate space
                // displacement = c1*this.b1 + c2*this.b2
                double c1 = thisVerticesAndBasis[4].dot(displacement);
                double c2 = thisVerticesAndBasis[5].dot(displacement);

                // vertex is in this's region iff
                // 0 <= c1 <= this.width and
                // 0 <= c2 <= this.height
                if (0 <= c1 && c1 <= this.width && 0 <= c2 && c2 <= this.height) {
                    return true;
                }
            }

            // Check if this's vertices are inside other's rectangular region
            for (int i = 0; i < 4; i++) {
                Vec2d vertex = thisVerticesAndBasis[i];
                Vec2d displacement = vertex.sub(otherVerticesAndBasis[0]);
                // Decompose displacement in terms of other's local coordinate space
                // displacement = c1*other.b1 + c2*other.b2
                double c1 = otherVerticesAndBasis[4].dot(displacement);
                double c2 = otherVerticesAndBasis[5].dot(displacement);

                // vertex is in this's region iff
                // 0 <= c1 <= other.width and
                // 0 <= c2 <= other.height
                if (0 <= c1 && c1 <= otherRect.width && 0 <= c2 && c2 <= otherRect.height) {
                    return true;
                }
            }
        } else if (other instanceof CircleCollider) {
            CircleCollider otherCircle = (CircleCollider) other;
            Vec2d closestPoint = findClosestPoint(otherCircle.getCenter());
            return closestPoint.sub(otherCircle.getCenter()).mag() <= otherCircle.getRadius();
        }
        return false;
    }

    @Override
    public Vec2d getPosition() {
        return origin;
    }

    @Override
    public void setPosition(Vec2d position) {
        origin = position;
    }

    public Vec2d getOrigin() {
        return origin;
    }

    public void setOrigin(Vec2d origin) {
        this.origin = origin;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
}
