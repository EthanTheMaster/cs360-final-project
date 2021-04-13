import game.AbstractLocalGame;
import game.map.BouncyBalls;
import game.map.Spin;
import javafx.application.Application;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].toLowerCase().equals("export")) {
            HashMap<String, AbstractLocalGame> nameMap = new HashMap<>();
            nameMap.put("BouncyBalls", new BouncyBalls());
            nameMap.put("Spin", new Spin());

            for (String mapName : nameMap.keySet()) {
                try {
                    AbstractLocalGame game = nameMap.get(mapName);
                    FileOutputStream fileOutputStream = new FileOutputStream(mapName + ".map");
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                    objectOutputStream.writeObject(game);
                    objectOutputStream.flush();
                    fileOutputStream.close();
                    objectOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            Application.launch(App.class);
        }
    }
}
