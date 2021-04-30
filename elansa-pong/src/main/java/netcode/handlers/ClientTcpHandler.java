package netcode.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import netcode.GameClient;
import netcode.packets.*;

import java.net.InetSocketAddress;

public class ClientTcpHandler extends ChannelInboundHandlerAdapter {
    private GameClient client;

    public ClientTcpHandler(GameClient client) {
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Connected with server
        System.out.println("TCP: Connected to server...");

        InetSocketAddress address = (InetSocketAddress) client.getUdpChannel().localAddress();
        ctx.writeAndFlush(new Connect(address.getPort()));

        // Prime udp channel
        Serializer.sendPacketUdp(
                client.getUdpChannel(),
                client.getServerIp(),
                client.getServerPortUdp(),
                new Connect(address.getPort())
        );
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Disconnected from server
        System.out.println("TCP: Disconnected from server ...");
        ctx.close();
        client.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // Received message from server
        System.out.println("TCP: Received message from server ...");
        if (msg instanceof PlayerAssignment) {
            client.getUpdateHandlerHook().receivedPlayerAssignment((PlayerAssignment) msg);
        } else if (msg instanceof Synchronization) {
            client.getUpdateHandlerHook().receivedSynchronization((Synchronization) msg);
        } else if (msg instanceof PlayerEliminated) {
            client.getUpdateHandlerHook().receivedPlayerElimination((PlayerEliminated) msg);
        } else if (msg instanceof LivesUpdate) {
            client.getUpdateHandlerHook().receivedLivesUpdate((LivesUpdate) msg);
        } else if (msg instanceof GameOver) {
            client.getUpdateHandlerHook().receivedGameOver((GameOver) msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // Exception was made ... close connection
        cause.printStackTrace();
        ctx.close();
    }
}
