package netcode.state;

import netcode.packets.*;

public interface ClientUpdateHandler {
    /**
     * Invoked when the client receives a player assignment from the server
     * @param assignment the client's assignment
     */
    void receivedPlayerAssignment(PlayerAssignment assignment);

    /**
     * Invoked when the client receives a synchronization packet from the server
     * @param synchronization the synchronization packet
     */
    void receivedSynchronization(Synchronization synchronization);

    /**
     * Invoked when the client is notified that some player has been eliminated
     * @param playerEliminated the player eliminated
     */
    void receivedPlayerElimination(PlayerEliminated playerEliminated);

    /**
     * Invoked when the client is notified that there is a change in some player's available lives
     * @param livesUpdate the new state of the players' lives
     */
    void receivedLivesUpdate(LivesUpdate livesUpdate);

    /**
     * Invoked when the client is notified that the server has ended the game
     * @param gameOver the game over response send by the server
     */
    void receivedGameOver(GameOver gameOver);
}
