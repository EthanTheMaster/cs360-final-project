package netcode.packets;

import engine.Entity;

import java.util.ArrayList;

public class Synchronization implements Packet {
    private ArrayList<Entity> entities;
    private boolean critical;
    private long sequenceNumber;

    public Synchronization(ArrayList<Entity> entities, boolean critical, long sequenceNumber) {
        this.entities = entities;
        this.critical = critical;
        this.sequenceNumber = sequenceNumber;
    }

    public ArrayList<Entity> getEntities() {
        return entities;
    }

    public boolean isCritical() {
        return critical;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }
}
