package game;

public interface GameEventHandler {
    /**
     * This method is invoked when a winner has been determined
     * @param winner the player who won
     */
    void onWinnerDetermined(int winner);

    /**
     * This method is invoked when a player has been eliminated
     * @param eliminatedPlayer the player who was eliminated
     */
    void onPlayerElimination(int eliminatedPlayer);

    /**
     * This method is invoked when there is a change in a player's available lives
     * @param newLives a table holding the player's lives
     * @param activePlayers a table holding the active status of the players
     */
    void onLifeChange(int[] newLives, boolean[] activePlayers);
}
