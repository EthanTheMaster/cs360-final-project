import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class TestApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        GameScene gameScene = new GameSceneTest();
        Canvas canvas = gameScene.generateRenderableComponent(500, 500);
        GameLoop timer = new GameLoop(gameScene, canvas);
        timer.start();

        Pane pane = new Pane();
        pane.getChildren().addAll(canvas);

        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.setTitle("Test App");
        stage.show();


    }
}
