package netcode.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import netcode.GameServer;
import netcode.packets.Connect;
import netcode.packets.Ready;

public class ServerTcpHandler extends ChannelInboundHandlerAdapter {
    private GameServer server;

    public ServerTcpHandler(GameServer server) {
        this.server = server;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Client has just connected
        System.out.println("TCP: Client connected ...");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Client has just disconnected
        System.out.println("TCP: Client disconnected ...");
        server.getServerState().onPlayerDisconnect(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // Received a message from client
        System.out.println("TCP: Received message from client");
        if (msg instanceof Connect) {
            server.getServerState().onPlayerConnect(ctx, (Connect) msg);
        } else if (msg instanceof Ready) {
            server.getServerState().onPlayerReady(ctx);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // Exception was made ... close connection
        cause.printStackTrace();
        ctx.close();
    }
}
