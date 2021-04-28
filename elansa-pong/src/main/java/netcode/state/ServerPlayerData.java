package netcode.state;

import io.netty.channel.ChannelHandlerContext;

public class ServerPlayerData {
    private boolean isReady = false;
    private int udpPort;
    private int playerNumber;
    private long lastReceivedSequenceNumber = -1;
    private ChannelHandlerContext tcpCtx;

    public ServerPlayerData(int udpPort, int playerNumber, ChannelHandlerContext tcpCtx) {
        this.udpPort = udpPort;
        this.playerNumber = playerNumber;
        this.tcpCtx = tcpCtx;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public boolean isReady() {
        return isReady;
    }

    public int getUdpPort() {
        return udpPort;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public long getLastReceivedSequenceNumber() {
        return lastReceivedSequenceNumber;
    }

    public ChannelHandlerContext getTcpCtx() {
        return tcpCtx;
    }

    public void setLastReceivedSequenceNumber(long lastReceivedSequenceNumber) {
        this.lastReceivedSequenceNumber = lastReceivedSequenceNumber;
    }
}
