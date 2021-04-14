package netcode.packets;

public class Connect implements Packet {
    private int udpPort;

    public Connect(int udpPort) {
        this.udpPort = udpPort;
    }

    public int getUdpPort() {
        return udpPort;
    }
}
