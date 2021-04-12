import engine.GameLoop;
import engine.GameScene;
import game.AbstractLocalGame;
import game.GameEventHandler;
import game.GameSceneTest;
import game.map.BouncyBalls;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.Arrays;

public class TestApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        AbstractLocalGame gameScene = new BouncyBalls();
        gameScene.setGameEventHandler(new GameEventHandler() {
            @Override
            public void onWinnerDetermined(int winner) {
                System.out.println("Winner is Player " + winner);
                Platform.exit();
            }

            @Override
            public void onPlayerElimination(int eliminatedPlayer) {
                System.out.println("Player " + eliminatedPlayer + " has been eliminated.");
            }

            @Override
            public void onLifeChange(int[] newLives, boolean[] activePlayers) {
                System.out.println("The lives are now: " + Arrays.toString(newLives));
                System.out.println("The players active are: " + Arrays.toString(activePlayers));
            }
        });
        gameScene.activatePlayer(0, false);
        gameScene.activatePlayer(1, false);
        gameScene.activatePlayer(2, false);
        gameScene.activatePlayer(3, false);

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
