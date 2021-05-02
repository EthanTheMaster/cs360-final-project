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

    /**
     * Implements the life deduction mechanism
     * @param playerNumber the player to deduct a life from
     */
    protected abstract void deductLife(int playerNumber);

    /**
     * Updates the entities list after entities have been deleted or added
     */
    protected abstract void updateEntitiesList();

    /**
     * Updates the game state when a player is determined to be alive/is participating in the game
     * @param playerNumber the player to be declared as an active participant of the game
     * @param automated specifies whether the player should be controlled by the computer
     */
    public abstract void activatePlayer(int playerNumber, boolean automated);

    /**
     * Updates the game state when a player is eliminated from the game
     * @param playerNumber the player eliminated
     */
    public abstract void deactivatePlayer(int playerNumber);

    /**
     * Resets the game particularly after a life has been lost
     */
    public abstract void resetGame();

    public void setGameEventHandler(GameEventHandler gameEventHandler) {
        this.gameEventHandler = gameEventHandler;
    }

    public ArrayList<Entity> getEntities() {
        return entities;
    }

    public ArrayList<Entity> getStaticEntities() {
        return staticEntities;
    }

    public ArrayList<Entity> getDynamicEntities() {
        return dynamicEntities;
    }

    public Player[] getPlayers() {
        return players;
    }

    public boolean[] getActivePlayers() {
        return activePlayers;
    }

    public boolean[] getAutomatedPlayers() {
        return automatedPlayers;
    }
}
