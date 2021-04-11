import engine.GameLoop;
import engine.GameScene;
import game.AbstractLocalGame;
import game.GameSceneTest;
import game.map.BouncyBalls;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class TestApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        AbstractLocalGame gameScene = new BouncyBalls();
        gameScene.activatePlayer(0, true);
        gameScene.activatePlayer(1, true);
        gameScene.activatePlayer(2, true);
        gameScene.activatePlayer(3, true);

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
