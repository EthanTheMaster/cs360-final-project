package netcode.state;

import netcode.packets.*;

public interface ClientUpdateHandler {
    void receivedPlayerAssignment(PlayerAssignment assignment);
    void receivedSynchronization(Synchronization synchronization);
    void receivedPlayerElimination(PlayerEliminated playerEliminated);
    void receivedLivesUpdate(LivesUpdate livesUpdate);
    void receivedGameOver(GameOver gameOver);
}
