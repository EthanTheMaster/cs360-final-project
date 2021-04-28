package netcode.packets;

public class PlayerEliminated implements Packet {
    private int eliminatedPlayer;

    public PlayerEliminated(int eliminatedPlayer) {
        this.eliminatedPlayer = eliminatedPlayer;
    }

    public int getEliminatedPlayer() {
        return eliminatedPlayer;
    }
}
