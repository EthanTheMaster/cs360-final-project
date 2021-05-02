package game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainMenu {
    /**
     * Updates the stage to show the main menu
     * @param stage the stage that should hold the menu
     */
    public static void showMenu(Stage stage) {
        VBox vBox = new VBox();
        Button createLocalGame = new Button("Create Local Game");
        createLocalGame.setOnMouseClicked(mouseEvent -> CreateLocalGameMenu.showMenu(stage));
        Button joinServer = new Button("Join Server");
        joinServer.setOnMouseClicked(mouseEvent -> ConnectServer.showMenu(stage));

        vBox.getChildren().addAll(createLocalGame, joinServer);
        vBox.setPadding(new Insets(15));
        vBox.setSpacing(10);
        vBox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Main Menu");
        stage.show();
    }

}
