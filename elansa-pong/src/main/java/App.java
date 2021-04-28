import game.ui.CreateLocalGameMenu;
import game.ui.MainMenu;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        MainMenu.showMenu(stage);
    }
}
