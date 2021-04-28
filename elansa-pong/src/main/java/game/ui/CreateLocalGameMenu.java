package game.ui;

import game.AbstractLocalGame;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import netcode.packets.Serializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class CreateLocalGameMenu {
    static String[] playerDesignations = {"Human", "AI", "Neither"};
    static int[] chosenPlayerDesignations = {0, 0, 0, 0};
    static File mapChosen = null;

    static Stage mainStage;
    public static void showMenu(Stage stage) {
        mainStage = stage;

        GridPane gridPane = new GridPane();
        // Make button to select map
        Button chooseMap = new Button("Choose a Map");
        chooseMap.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Pick a map");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Map File", "*.map"));
                mapChosen = fileChooser.showOpenDialog(stage);

                if (mapChosen != null) {
                    chooseMap.setText(mapChosen.getName());
                }
            }
        });
        chooseMap.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        gridPane.add(chooseMap, 0, 0, 2, 1);

        // Create player designation menu
        for (int i = 1; i <= 4; i++) {
            Label playerLabel = new Label(String.format("Player %d Designation", i));
            playerLabel.setPadding(new Insets(5));

            ChoiceBox<String> playerDesignationChoice = new ChoiceBox<>();
            playerDesignationChoice.getItems().addAll(playerDesignations);
            playerDesignationChoice.getSelectionModel().selectFirst();
            int finalI = i;
            playerDesignationChoice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                    chosenPlayerDesignations[finalI - 1] = (int) newValue;
                }
            });
            gridPane.addRow(i, playerLabel, playerDesignationChoice);
        }
        Button startGame = new Button("Start Game!");
        startGame.setOnMouseClicked(CreateLocalGameMenu::onGameStart);
        startGame.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        Button cancel = new Button("Cancel");
        cancel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        cancel.setOnMouseClicked(mouseEvent -> MainMenu.showMenu(stage));
        gridPane.addRow(5, cancel, startGame);

        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(20);
        gridPane.setHgap(10);

        Scene scene = new Scene(gridPane);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setTitle("Local Game Menu");
        stage.show();
    }

    private static void displayError(String reason, String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(reason);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }

    private static void onGameStart(MouseEvent mouseEvent) {
        if (mapChosen == null) {
            displayError("No Map Selected", "Please select a map.");
            return;
        }

        boolean existsOneHuman = false;
        for (int chosenPlayerDesignation : chosenPlayerDesignations) {
            existsOneHuman = chosenPlayerDesignation == 0;
            if (existsOneHuman) {
                break;
            }
        }
        if (!existsOneHuman) {
            displayError("Human Player is Required", "There must be at least one human player.");
            return;
        }

        try {
            AbstractLocalGame game = Serializer.readGameMapFromFile(mapChosen);
            for (int i = 0; i < chosenPlayerDesignations.length; i++) {
                if (chosenPlayerDesignations[i] == 0) {
                    game.activatePlayer(i, false);
                } else if (chosenPlayerDesignations[i] == 1) {
                    game.activatePlayer(i, true);
                }
            }

            PlayLocalGame.displayGame(mainStage, game);
        } catch (Exception e) {
            displayError("Could Not Read Map", "There was an error in reading the map. Please try again.");
        }
    }
}
