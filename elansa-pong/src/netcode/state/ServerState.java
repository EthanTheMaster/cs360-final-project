package netcode.state;

import engine.Entity;
import game.AbstractLocalGame;
import game.GameEventHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import netcode.packets.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

public class ServerState {
    private static final int CHUNK_SIZE = 5;
    private boolean gameStarted = false;

    private AbstractLocalGame localGame;

    private AtomicLong sequenceNumber = new AtomicLong(0);

    private ConcurrentHashMap<SocketAddress, ServerPlayerData> playerDataMap = new ConcurrentHashMap<>();
    private ConcurrentLinkedDeque<Integer> availableAssignments = new ConcurrentLinkedDeque<Integer>(Arrays.asList(0, 1, 2, 3));

    GameEventHandler localGameEventHandler = new GameEventHandler() {
        @Override
        public void onWinnerDetermined(int winner) {
            System.out.println("Winner found");
            sendGameOver(String.format("The Winner is Player %d!", winner + 1));
        }

        @Override
        public void onPlayerElimination(int eliminatedPlayer) {
            System.out.println("Player eliminated");
            sendSynchronization();
            for (ServerPlayerData playerData : playerDataMap.values()) {
                playerData.getTcpCtx().writeAndFlush(new PlayerEliminated(eliminatedPlayer));
            }
        }

        @Override
        public void onLifeChange(int[] newLives, boolean[] activePlayers) {
            System.out.println("Life changed");
            sendSynchronization();
            for (ServerPlayerData playerData : playerDataMap.values()) {
                playerData.getTcpCtx().writeAndFlush(new LivesUpdate(newLives, activePlayers));
            }
        }
    };

    public ServerState(AbstractLocalGame game) {
        localGame = game;
        game.setGameEventHandler(localGameEventHandler);
    }

    private void sendGameOver(String message) {
        for (ServerPlayerData playerData : playerDataMap.values()) {
            playerData.getTcpCtx().writeAndFlush(new GameOver(message));
        }
        resetServer();
    }

    private void resetServer() {
        System.out.println("Restarting server ...");
        gameStarted = false;
        localGame.resetGame();
        for (int i = 0; i < 4; i++) {
            localGame.getActivePlayers()[i] = false;
            localGame.getAutomatedPlayers()[i] = false;
        }
        sequenceNumber = new AtomicLong(0);
        playerDataMap.clear();
        availableAssignments = new ConcurrentLinkedDeque<Integer>(Arrays.asList(0, 1, 2, 3));

        disconnectAllClients();
    }

    /**
     * Sends to all client over TCP the entire board state
     */
    private void sendSynchronization() {
        ArrayList<Entity> entities = new ArrayList<>(localGame.getStaticEntities());
        entities.addAll(localGame.getDynamicEntities());
        for (int i = 0; i < localGame.getActivePlayers().length; i++) {
            if (localGame.getActivePlayers()[i]) {
                entities.add(localGame.getPlayers()[i]);
            }
        }
        for (ServerPlayerData playerData : playerDataMap.values()) {
            playerData.getTcpCtx().writeAndFlush(new Synchronization(entities, true, -1));
        }
    }

    public void updateLocalGame() {
        if (gameStarted) {
            localGame.updateState(System.nanoTime());
        }
    }

    public void broadcastGameState(Channel udpChannel) {
        if (gameStarted) {
            ArrayList<Entity>[] chunks = new ArrayList[CHUNK_SIZE];
            for (int i = 0; i < CHUNK_SIZE; i++) {
                chunks[i] = new ArrayList<>();
            }

            // Get items
            ArrayList<Entity> dynamicEntities = new ArrayList<>(localGame.getDynamicEntities());
            for (int i = 0; i < localGame.getActivePlayers().length; i++) {
                if (localGame.getActivePlayers()[i]) {
                    dynamicEntities.add(localGame.getPlayers()[i]);
                }
            }
            // Distribute into chunks
            for (int i = 0; i < dynamicEntities.size(); i++) {
                chunks[i % CHUNK_SIZE].add(dynamicEntities.get(i));
            }

            // Broadcast each chunk
            for (ArrayList<Entity> chunk : chunks) {
                long newSequenceNumber = sequenceNumber.getAndIncrement();
                for (ServerPlayerData playerData : playerDataMap.values()) {
                    InetSocketAddress address = (InetSocketAddress) playerData.getTcpCtx().channel().remoteAddress();
                    Serializer.sendPacketUdp(
                            udpChannel,
                            address.getHostName(),
                            playerData.getUdpPort(),
                            new Synchronization(chunk, false, newSequenceNumber)
                    );
                }
            }
        }
    }

    /**
     * Disconnects all the clients
     */
    private void disconnectAllClients() {
        for (ServerPlayerData playerData : playerDataMap.values()) {
            playerData.getTcpCtx().close();
        }
    }

    /**
     * Invoked when a client has connected
     * @param ctx the client's tcp connection
     * @param connect the client's connect packet
     */
    public void onPlayerConnect(ChannelHandlerContext ctx, Connect connect) {
        if (gameStarted) {
            ctx.writeAndFlush(new GameOver("A game is currently in progress. Please come back later."));
            ctx.close();
            return;
        }

        System.out.println(availableAssignments);
        int playerNumber = availableAssignments.pop();
        localGame.activatePlayer(playerNumber, false);
        playerDataMap.put(ctx.channel().remoteAddress(), new ServerPlayerData(connect.getUdpPort(), playerNumber, ctx));
        ctx.writeAndFlush(new PlayerAssignment(playerNumber, localGame.getPlayers()[playerNumber]));
        sendSynchronization();
    }

    /**
     * Invoked when a client has disconnected
     * @param ctx the client's tcp connection
     */
    public void onPlayerDisconnect(ChannelHandlerContext ctx) {
        ServerPlayerData playerData = playerDataMap.remove(ctx.channel().remoteAddress());
        if (playerData != null) {
            // Trigger the player elimination mechanism as active player just left
            if (gameStarted) {
                // Eliminated active players who disconnect
                if (localGame.getActivePlayers()[playerData.getPlayerNumber()]) {
                    localGame.deactivatePlayer(playerData.getPlayerNumber());
                    localGameEventHandler.onPlayerElimination(playerData.getPlayerNumber());
                }

                // Player wins by default
                if (playerDataMap.size() == 1) {
                    int winner = playerDataMap.values().stream().findFirst().get().getPlayerNumber();
                    localGameEventHandler.onWinnerDetermined(winner);
                }
            } else {
                // Give back the assignment assuming the game has not started
                availableAssignments.push(playerData.getPlayerNumber());
                localGame.deactivatePlayer(playerData.getPlayerNumber());
            }
            sendSynchronization();
        }

    }

    /**
     * Invoked when a player is ready to start the game
     * @param ctx
     */
    public void onPlayerReady(ChannelHandlerContext ctx) {
        playerDataMap.get(ctx.channel().remoteAddress()).setReady(true);

        boolean allReady = true;
        for (ServerPlayerData playerData : playerDataMap.values()) {
            if (!playerData.isReady()) {
                allReady = false;
                break;
            }
        }
        if (allReady && playerDataMap.size() > 1) {
            gameStarted = true;
            System.out.println("Game has started!");
        }
    }

    public void onPlayerInput(InetSocketAddress sender, PlayerInput packet) {
        for (SocketAddress tcpSocket : playerDataMap.keySet()) {
            InetSocketAddress tcpAddress = (InetSocketAddress) tcpSocket;
            ServerPlayerData playerData = playerDataMap.get(tcpSocket);

            // Find player data associated with packet received
            if (tcpAddress.getHostName().equals(sender.getHostName()) && playerData.getUdpPort() == sender.getPort()) {
                // Check sequence number
                if (playerData.getLastReceivedSequenceNumber() < packet.getSequenceNumber()) {
                    playerData.setLastReceivedSequenceNumber(packet.getSequenceNumber());
                    localGame.getPlayers()[playerData.getPlayerNumber()].setPosition(packet.getPosition());
                    localGame.getPlayers()[playerData.getPlayerNumber()].setDirection(packet.getDirection());
                }
               break;
            }
        }
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

}
