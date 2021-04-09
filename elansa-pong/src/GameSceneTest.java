import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

public class GameSceneTest implements GameScene {
    private RectangleCollider r1 = new RectangleCollider(new Vec2d(0.0, 250.0), 50, 25, Math.PI / 3);
    private RectangleCollider r2 = new RectangleCollider(new Vec2d(100.0, 250.0), 50, 25, Math.PI / 4);
    private CircleCollider c = new CircleCollider(new Vec2d(200, 250), 40);

    private Long lastRecordedTime = null;
    @Override
    public void updateState(long currentTime) {
        double deltaTime = 0.0;
        if (lastRecordedTime == null) {
            lastRecordedTime = currentTime;
        } else {
            deltaTime = (double) (currentTime - lastRecordedTime)/1000000000.0;
            lastRecordedTime = currentTime;
        }

        r1.setOrigin(r1.getPosition().add(
                new Vec2d( deltaTime * 20, 0)
        ));
    }

    @Override
    public void onKeyPressed(KeyEvent e) {
        System.out.println("Pressed");
        System.out.println(e.getText());
    }

    @Override
    public void onKeyReleased(KeyEvent e) {
        System.out.println("Released");
        System.out.println(e.getText());
    }

    @Override
    public void render(Canvas canvas) {
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        ctx.setFill(Color.RED);

        Vec2d[] vb1 = r1.computeVerticesAndBasis();
        Vec2d[] vb2 = r2.computeVerticesAndBasis();

        ctx.fillPolygon(
                new double[]{vb1[0].getX(), vb1[1].getX(), vb1[2].getX(), vb1[3].getX()},
                new double[]{vb1[0].getY(), vb1[1].getY(), vb1[2].getY(), vb1[3].getY()},
                4
        );
        ctx.fillPolygon(
                new double[]{vb2[0].getX(), vb2[1].getX(), vb2[2].getX(), vb2[3].getX()},
                new double[]{vb2[0].getY(), vb2[1].getY(), vb2[2].getY(), vb2[3].getY()},
                4
        );
        ctx.fillOval(c.getCenter().getX() - c.getRadius(), c.getCenter().getY() - c.getRadius(), c.getRadius()*2, c.getRadius()*2);

        if (r1.collide(r2)) {
            System.out.println("R1 collided with R2!!!");
        }

        if (r1.collide(c)) {
            System.out.println("R1 collided with C!!!");
        }
    }
}
