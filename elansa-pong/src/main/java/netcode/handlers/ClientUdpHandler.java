package netcode.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import netcode.GameClient;
import netcode.packets.Packet;
import netcode.packets.Serializer;
import netcode.packets.Synchronization;

public class ClientUdpHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private GameClient client;
    public ClientUdpHandler(GameClient client) {
        this.client = client;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {
        Packet packet = Serializer.decodeUdpDatagram(datagramPacket);
        if (packet instanceof Synchronization) {
            client.getUpdateHandlerHook().receivedSynchronization((Synchronization) packet);
        }
    }
}
