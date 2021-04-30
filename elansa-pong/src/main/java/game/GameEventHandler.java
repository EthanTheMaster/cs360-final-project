package game;

public interface GameEventHandler {
    void onWinnerDetermined(int winner);
    void onPlayerElimination(int eliminatedPlayer);
    void onLifeChange(int[] newLives, boolean[] activePlayers);
}
