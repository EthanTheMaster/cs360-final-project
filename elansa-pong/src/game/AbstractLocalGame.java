package game;

import engine.Entity;
import engine.GameScene;
import engine.Vec2d;

import java.util.ArrayList;

public abstract class AbstractLocalGame implements GameScene {
    protected ArrayList<Entity> entities = new ArrayList<>();
    protected ArrayList<Entity> staticEntities = new ArrayList<>();
    protected ArrayList<Entity> dynamicEntities = new ArrayList<>();

    protected Player[] players = new Player[4];
    protected boolean[] activePlayers = {false, false, false, false};
    protected boolean[] automatedPlayers = {false, false, false, false};

    protected transient GameEventHandler gameEventHandler = new GameEventHandler() {
        @Override
        public void onWinnerDetermined(int winner) {

        }

        @Override
        public void onPlayerElimination(int eliminatedPlayer) {

        }

        @Override
        public void onLifeChange(int[] newLives, boolean[] activePlayers) {

        }
    };

    public static final Player EMPTY_PLAYER = new Player(
            "EMPTY",
            new Vec2d(1000, 1000),
            0.0,
            0.0,
            new Vec2d(0.0, 0.0),
            9999,
            9999,
            0.0
    );

    protected abstract void deductLife(int playerNumber);
    protected abstract void updateEntitiesList();
    public abstract void activatePlayer(int playerNumber, boolean automated);
    public abstract void deactivatePlayer(int playerNumber);
    public abstract void resetGame();
}
