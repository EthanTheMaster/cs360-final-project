import game.ui.CreateLocalGameMenu;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        CreateLocalGameMenu.showMenu(stage);
    }
}
