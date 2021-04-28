package netcode.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import netcode.GameServer;
import netcode.packets.Packet;
import netcode.packets.PlayerInput;
import netcode.packets.Serializer;


public class ServerUdpHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private GameServer server;

    public ServerUdpHandler(GameServer server) {
        this.server = server;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {
        Packet packet = Serializer.decodeUdpDatagram(datagramPacket);
        if (packet instanceof PlayerInput) {
            server.getServerState().onPlayerInput(datagramPacket.sender(), (PlayerInput) packet);
        }
    }
}
