package game.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import netcode.GameClient;

public class ConnectServer {
    public static void showMenu(Stage stage) {
        GridPane gridPane = new GridPane();
        Label serverIp = new Label("Server IP");
        Label serverTcpPort = new Label("Server TCP Port");
        Label serverUdpPort = new Label("Server UDP Port");

        TextField ipField = new TextField();
        TextField tcpField = new TextField();
        TextField udpField = new TextField();

        Button cancel = new Button("Cancel");
        cancel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        cancel.setOnMouseClicked(mouseEvent -> MainMenu.showMenu(stage));
        Button connect = new Button("Connect");
        connect.setOnMouseClicked(mouseEvent -> {
            new GameClient("127.0.0.1", 8080, 8081).launchClient(stage);
        });
        connect.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        gridPane.addRow(0, serverIp, ipField);
        gridPane.addRow(1, serverTcpPort, tcpField);
        gridPane.addRow(2, serverUdpPort, udpField);
        gridPane.addRow(3, cancel, connect);

        gridPane.setPadding(new Insets(20));
        gridPane.setVgap(20);
        gridPane.setHgap(10);

        Scene scene = new Scene(gridPane);
        stage.setScene(scene);
        stage.setTitle("Connect to a Server");
        stage.setResizable(false);
        stage.show();
    }
}
