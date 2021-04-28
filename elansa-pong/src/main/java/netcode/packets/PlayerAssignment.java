package netcode.packets;

import game.Player;

public class PlayerAssignment implements Packet {
    private int playerNumber;
    private Player player;

    public PlayerAssignment(int playerNumber, Player player) {
        this.playerNumber = playerNumber;
        this.player = player;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public Player getPlayer() {
        return player;
    }
}
