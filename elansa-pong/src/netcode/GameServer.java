package netcode;

import game.AbstractLocalGame;
import game.map.BouncyBalls;
import game.map.Spin;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import netcode.handlers.ServerTcpHandler;
import netcode.handlers.ServerUdpHandler;
import netcode.packets.PacketDecoder;
import netcode.packets.PacketEncoder;
import netcode.state.ServerState;

import java.util.concurrent.TimeUnit;

public class GameServer {
    private String hostname;
    private int portTcp;
    private int portUdp;

    private ServerState serverState;

    public GameServer(String hostname, int portTcp, int portUdp, AbstractLocalGame game) {
        this.hostname = hostname;
        this.portTcp = portTcp;
        this.portUdp = portUdp;

        serverState = new ServerState(game);
    }

    public void launchServer() throws InterruptedException {
        GameServer gameServer = this;

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // Bootstrap UDP channel
            Bootstrap udp = new Bootstrap();
            udp.group(workerGroup)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel nioDatagramChannel) throws Exception {
                            ChannelPipeline pipeline = nioDatagramChannel.pipeline();
                            pipeline.addLast(new ServerUdpHandler(gameServer));
                        }
                    });
            Channel udpChannel = udp.bind(hostname, portUdp).sync().channel();
            // Broadcast the game
            udpChannel.eventLoop().scheduleAtFixedRate(() -> {
                serverState.broadcastGameState(udpChannel);
            }, 0, 16, TimeUnit.MILLISECONDS);
            udpChannel.eventLoop().scheduleAtFixedRate(() -> {
                serverState.updateLocalGame();
            }, 0, 1, TimeUnit.MILLISECONDS);
            System.out.printf("UDP Server: %s%n", udpChannel.localAddress());
            // Bootstrap TCP channel
            ServerBootstrap tcp = new ServerBootstrap();
            tcp.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new PacketDecoder());
                            pipeline.addLast(new PacketEncoder());
                            pipeline.addLast(new ServerTcpHandler(gameServer));
                        }
                    });
            Channel tcpChannel = tcp.bind(hostname, portTcp).sync().channel();
            System.out.printf("TCP Server: %s%n", tcpChannel.localAddress());

            udpChannel.closeFuture().sync();
            tcpChannel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public ServerState getServerState() {
        return serverState;
    }

    public static void main(String[] args) {
        try {
            new GameServer("127.0.0.1", 8080, 8081, new Spin()).launchServer();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
