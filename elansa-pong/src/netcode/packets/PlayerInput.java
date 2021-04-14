package netcode.packets;

import engine.Vec2d;

public class PlayerInput implements Packet {
    private int direction;
    private Vec2d position;
    private long sequenceNumber;

    public PlayerInput(int direction, Vec2d position, long sequenceNumber) {
        this.direction = direction;
        this.position = position;
        this.sequenceNumber = sequenceNumber;
    }

    public int getDirection() {
        return direction;
    }

    public Vec2d getPosition() {
        return position;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }
}
