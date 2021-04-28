package netcode.packets;

public class LivesUpdate implements Packet {
    private int[] newLives;
    private boolean[] activePlayers;

    public LivesUpdate(int[] newLives, boolean[] activePlayers) {
        this.newLives = newLives;
        this.activePlayers = activePlayers;
    }

    public int[] getNewLives() {
        return newLives;
    }

    public boolean[] getActivePlayers() {
        return activePlayers;
    }
}
