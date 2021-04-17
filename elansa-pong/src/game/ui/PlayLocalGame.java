package game.ui;

import engine.GameLoop;
import game.AbstractLocalGame;
import game.GameEventHandler;
import game.GameSettings;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;

import java.nio.file.Paths;


public class PlayLocalGame {
    public static void displayGame(Stage stage, AbstractLocalGame game) {
        Canvas canvas = game.generateRenderableComponent(500, 500);
        AnimationTimer timer = new GameLoop(game, canvas);
        timer.start();
        StackPane pane = new StackPane();
        pane.getChildren().add(canvas);

        DisplayMessage livesBoard = new DisplayMessage(200, 2600);
        DisplayMessage eliminationNotification = new DisplayMessage(200, 2600);
        pane.getChildren().add(livesBoard);
        pane.getChildren().add(eliminationNotification);

        StackPane.setAlignment(livesBoard, Pos.CENTER);
        StackPane.setAlignment(eliminationNotification, Pos.TOP_CENTER);
        AudioClip bell = new AudioClip(
                Paths.get(GameSettings.BELL_AUDIO).toUri().toString()
        );
        game.setGameEventHandler(new GameEventHandler() {
            @Override
            public void onWinnerDetermined(int winner) {
                timer.stop();
                livesBoard.sustainMessage(String.format("The Winner is Player %d", winner + 1));
            }

            @Override
            public void onPlayerElimination(int eliminatedPlayer) {
                eliminationNotification.flashMessage(String.format("Player %d has been eliminated!", eliminatedPlayer + 1));
            }

            @Override
            public void onLifeChange(int[] newLives, boolean[] activePlayers) {
                StringBuilder livesMessage = new StringBuilder();
                for (int i = 1; i <= newLives.length; i++) {
                    if (activePlayers[i-1]) {
                        livesMessage.append(String.format("Player %d's Lives: %d\n", i, newLives[i-1]));
                    }
                }
                if (GameSettings.SOUND_EFFECTS_ON) {
                    bell.play();
                }
                livesBoard.flashMessage(livesMessage.toString());
            }
        });
        Scene scene = new Scene(pane);
        stage.setTitle("Local Game");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}
