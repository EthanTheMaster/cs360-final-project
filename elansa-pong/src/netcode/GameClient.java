package netcode;

import engine.GameLoop;
import game.GameEventHandler;
import game.ui.DisplayMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import netcode.handlers.ClientTcpHandler;
import netcode.handlers.ClientUdpHandler;
import netcode.packets.PacketDecoder;
import netcode.packets.PacketEncoder;
import netcode.state.ClientUpdateHandler;

public class GameClient {
    private String serverIp;
    private int serverPortTcp;
    private int serverPortUdp;

    private Channel udpChannel;
    private Channel tcpChannel;

    private ClientUpdateHandler updateHandlerHook;

    private DisplayMessage livesBoard;
    private DisplayMessage eliminationNotification;

    public GameClient(String serverIp, int serverPortTcp, int serverPortUdp) {
        this.serverIp = serverIp;
        this.serverPortTcp = serverPortTcp;
        this.serverPortUdp = serverPortUdp;
    }

    /**
     * Creates a TCP and UDP connection to the server
     * @throws InterruptedException
     */
    public void establishConnection() throws InterruptedException {
        GameClient client = this;

        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // Bootstrap UDP Channel
            Bootstrap udp = new Bootstrap();
            udp.group(workerGroup)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel nioDatagramChannel) throws Exception {
                            ChannelPipeline pipeline = nioDatagramChannel.pipeline();
                            pipeline.addLast(new ClientUdpHandler(client));
                        }
                    });

            udpChannel = udp.bind(0).sync().channel();
            System.out.println(udpChannel.localAddress());

            // Boostrap TCP Channel
            Bootstrap tcp = new Bootstrap();
            tcp.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new PacketDecoder());
                            pipeline.addLast(new PacketEncoder());
                            pipeline.addLast(new ClientTcpHandler(client));
                        }
                    });
            tcpChannel = tcp.connect(serverIp, serverPortTcp).sync().channel();
            System.out.println(tcpChannel.remoteAddress());

            udpChannel.closeFuture().sync();
            tcpChannel.closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public void close() {
        if (udpChannel != null) {
            udpChannel.close();
        }
        if (tcpChannel != null) {
            tcpChannel.close();
        }
    }

    /**
     * Creates user interface for client
     * @param stage the main JavaFX stage
     */
    public void launchClient(Stage stage) {
        livesBoard = new DisplayMessage(200, 2600);
        eliminationNotification = new DisplayMessage(200, 2600);

        ClientLocalGame game = new ClientLocalGame(this);
        Canvas canvas = game.generateRenderableComponent(500, 500);
        GameLoop loop = new GameLoop(game, canvas);
        loop.start();

        StackPane pane = new StackPane();
        pane.getChildren().add(canvas);

        pane.getChildren().add(livesBoard);
        pane.getChildren().add(eliminationNotification);

        StackPane.setAlignment(livesBoard, Pos.CENTER);
        StackPane.setAlignment(eliminationNotification, Pos.TOP_CENTER);

        Scene scene = new Scene(pane);

        stage.setOnCloseRequest(windowEvent -> {
            close();
        });
        stage.setResizable(false);
        stage.setTitle("Networked Game");
        stage.setScene(scene);
        stage.show();
    }

    public DisplayMessage getLivesBoard() {
        return livesBoard;
    }

    public DisplayMessage getEliminationNotification() {
        return eliminationNotification;
    }

    public String getServerIp() {
        return serverIp;
    }

    public int getServerPortTcp() {
        return serverPortTcp;
    }

    public int getServerPortUdp() {
        return serverPortUdp;
    }

    public Channel getUdpChannel() {
        return udpChannel;
    }

    public Channel getTcpChannel() {
        return tcpChannel;
    }

    public void setUpdateHandlerHook(ClientUpdateHandler updateHandlerHook) {
        this.updateHandlerHook = updateHandlerHook;
    }

    public ClientUpdateHandler getUpdateHandlerHook() {
        return updateHandlerHook;
    }
}
