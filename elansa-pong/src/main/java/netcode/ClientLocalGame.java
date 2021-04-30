package netcode;

import engine.Collider;
import engine.Entity;
import engine.GameScene;
import game.GameSettings;
import game.Player;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.AudioClip;
import netcode.packets.*;
import netcode.state.ClientUpdateHandler;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ClientLocalGame implements GameScene {
    Long lastRecordedTime = null;

    private Player player;
    private String playerId;
    private int playerAssignment;

    private HashMap<String, Entity> entities = new HashMap<>();
    private long lastReceivedSequenceNumber = -1;
    private AtomicLong sequenceNumber = new AtomicLong(0);

    private GameClient client;
    public ClientLocalGame(GameClient client) {
        this.client = client;
        AudioClip bell = new AudioClip(
                Paths.get(GameSettings.BELL_AUDIO).toUri().toString()
        );
        client.setUpdateHandlerHook(new ClientUpdateHandler() {
            @Override
            public void receivedPlayerAssignment(PlayerAssignment assignment) {
                playerAssignment = assignment.getPlayerNumber();
                player = assignment.getPlayer();
                playerId = player.getId();
                if (playerAssignment == 0 || playerAssignment == 1) {
                    player.setDirectionKeyNegative(38);
                    player.setDirectionKeyPositive(40);
                    Platform.runLater(() -> {
                        client.getEliminationNotification().flashMessage(String.format("You are Player %d", assignment.getPlayerNumber() + 1));
                        client.getLivesBoard().sustainMessage("Press Spacebar To Get Ready.\nUse Up and Down Arrow Keys to Move.");
                    });
                } else {
                    player.setDirectionKeyNegative(37);
                    player.setDirectionKeyPositive(39);
                    Platform.runLater(() -> {
                        client.getEliminationNotification().flashMessage(String.format("You are Player %d", assignment.getPlayerNumber() + 1));
                        client.getLivesBoard().sustainMessage("Press Spacebar To Get Ready.\nUse Left and Right Arrow Keys to Move.");
                    });
                }
            }

            @Override
            public void receivedSynchronization(Synchronization synchronization) {
                if (synchronization.isCritical()) {
                    System.out.println("Received Critical Synchronization");
                    entities = new HashMap<>();
                    for (Entity entity : synchronization.getEntities()) {
                        // Player may be null if player has been eliminated and is now a spectator
                        if (!entity.getId().equals(playerId)) {
                            entities.put(entity.getId(), entity);
                        } else {
                            // Received a critical synchronization so make sure to synchronize the player's data
                            if (player != null && entity instanceof Player) {
                                player.setPosition(entity.getPosition());
                                player.setVelocity(entity.getVelocity());
                                player.setDirection(((Player) entity).getDirection());
                            }
                        }
                    }
                } else {
                    if (synchronization.getSequenceNumber() > lastReceivedSequenceNumber) {
                        lastReceivedSequenceNumber = synchronization.getSequenceNumber();
                        for (Entity entity : synchronization.getEntities()) {
                            // Player may be null if player has been eliminated and is now a spectator
                            if (!entity.getId().equals(playerId)) {
                                entities.put(entity.getId(), entity);
                            }
                        }
                    }
                }
            }

            @Override
            public void receivedPlayerElimination(PlayerEliminated playerEliminated) {
                if (playerEliminated.getEliminatedPlayer() == playerAssignment) {
                    Platform.runLater(() -> {
                        client.getEliminationNotification().flashMessage("You have been eliminated.");
                    });
                    player = null;
                } else {
                    Platform.runLater(() -> {
                        client.getEliminationNotification().flashMessage(
                                String.format("Player %d has been eliminated!", playerEliminated.getEliminatedPlayer() + 1
                        ));
                    });
                }
            }

            @Override
            public void receivedLivesUpdate(LivesUpdate livesUpdate) {
                StringBuilder livesMessage = new StringBuilder();
                for (int i = 1; i <= livesUpdate.getNewLives().length; i++) {
                    if (livesUpdate.getActivePlayers()[i-1]) {
                        livesMessage.append(String.format("Player %d's Lives: %d\n", i, livesUpdate.getNewLives()[i-1]));
                    }
                }
                Platform.runLater(() -> {
                    client.getLivesBoard().flashMessage(livesMessage.toString());
                });
                if (GameSettings.SOUND_EFFECTS_ON) {
                    bell.play();
                }
            }

            @Override
            public void receivedGameOver(GameOver gameOver) {
                System.out.println("Received game over");
                Platform.runLater(() -> {
                    client.getLivesBoard().sustainMessage(gameOver.getMessage());
                });
                client.close();
            }
        });

        Thread clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.establishConnection();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        clientThread.start();
    }

    @Override
    public void updateState(long currentTime) {
        double deltaTime = 0.0;
        if (lastRecordedTime != null) {
            deltaTime = (double) (currentTime - lastRecordedTime) / 1000000000.0;
        }
        lastRecordedTime = currentTime;

        // Update animations
        if (player != null) {
            player.setPosition(
                    player.getPosition().add(player.getVelocity().scale(deltaTime))
            );
        }
        for (Entity entity : entities.values()) {
            entity.setPosition(
                    entity.getPosition().add(entity.getVelocity().scale(deltaTime))
            );
        }

        // Handle Collisions
        ArrayList<Entity> entitiesList = new ArrayList<>(entities.values());
        if (player != null) {
            entitiesList.add(player);
        }
        for (int i = 0; i < entitiesList.size()-1; i++) {
            for (int j = i+1; j < entitiesList.size(); j++) {
                Entity entity1 = entitiesList.get(i);
                Entity entity2 = entitiesList.get(j);
                Collider[] colliders = entity1.collidesWith(entity2);
                if (colliders != null) {
                    entity1.onCollision(entity2, colliders[1]);
                    entity2.onCollision(entity1, colliders[0]);
                }
            }
        }
    }

    @Override
    public void onKeyPressed(KeyEvent e) {
        if (player != null) {
            player.setDirectionKeyPress(e.getCode().getCode());
            if (client.getUdpChannel().isActive()) {
                Serializer.sendPacketUdp(
                        client.getUdpChannel(),
                        client.getServerIp(),
                        client.getServerPortUdp(),
                        new PlayerInput(player.getDirection(), player.getPosition(), sequenceNumber.getAndIncrement())
                );
            }
        }
    }

    @Override
    public void onKeyReleased(KeyEvent e) {
        if (player != null) {
            player.setDirectionKeyRelease(e.getCode().getCode());
            if (client.getUdpChannel().isActive()) {
                Serializer.sendPacketUdp(
                        client.getUdpChannel(),
                        client.getServerIp(),
                        client.getServerPortUdp(),
                        new PlayerInput(player.getDirection(), player.getPosition(), sequenceNumber.getAndIncrement())
                );
            }

            if (e.getCode().getCode() == 32) {
                client.getTcpChannel().writeAndFlush(new Ready());
                Platform.runLater(() -> client.getLivesBoard().hideMessage());
            }
        }
    }

    @Override
    public void render(Canvas canvas) {
        GraphicsContext ctx = canvas.getGraphicsContext2D();
        ctx.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (Entity entity : entities.values()) {
            entity.render(canvas);
        }
        if (player != null) {
            player.render(canvas);
        }
    }
}