package netcode.packets;

public class GameOver implements Packet {
    String message;

    public GameOver(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
